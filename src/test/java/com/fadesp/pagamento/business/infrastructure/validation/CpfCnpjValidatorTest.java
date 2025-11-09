package com.fadesp.pagamento.business.infrastructure.validation;

import com.fadesp.pagamento.business.dto.in.PagamentoRequestDTO;
import com.fadesp.pagamento.business.dto.out.PagamentoResponseDTO;
import com.fadesp.pagamento.business.service.PagamentoService;
import com.fadesp.pagamento.infrastructure.entities.Pagamento;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import com.fadesp.pagamento.infrastructure.exceptions.BusinessException;
import com.fadesp.pagamento.infrastructure.exceptions.NotFoundException;
import com.fadesp.pagamento.infrastructure.repository.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CpfCnpjValidatorTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    private PagamentoRequestDTO requestValido;
    private Pagamento pagamentoPendente;

    @BeforeEach
    void setUp() {
        requestValido = new PagamentoRequestDTO(
                123,
                "12345678909",
                com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum.CARTAO_CREDITO,
                "1234567890123456",
                new BigDecimal("100.00")
        );

        pagamentoPendente = new Pagamento();
        pagamentoPendente.setId(1L);
        pagamentoPendente.setCodigoDebito(123);
        pagamentoPendente.setCpfCnpjPagador("12345678909");
        pagamentoPendente.setValorTransacao(new BigDecimal("100.00"));
        pagamentoPendente.setAtivo(true);
        pagamentoPendente.setStatus(com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum.PENDENTE);
    }




    @Test
    void realizarPagamento_deveLancarBusinessException_quandoValorInvalido() {
        PagamentoRequestDTO invalido = new PagamentoRequestDTO(
                123,
                "12345678909",
                com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum.PIX,
                null,
                new BigDecimal("0.00")
        );
    }


    @Test
    void buscarPagamentoPorId_deveRetornarPagamento_quandoExiste() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPendente));

        PagamentoResponseDTO response = pagamentoService.buscarPagamentoPorId(1L);

        assertNotNull(response);
        assertEquals(pagamentoPendente.getCodigoDebito(), response.codigoDebito());
        verify(pagamentoRepository).findById(1L);
    }

    @Test
    void buscarPagamentoPorId_deveLancarNotFound_quandoNaoExiste() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pagamentoService.buscarPagamentoPorId(99L));
    }

    @Test
    void listarTodos_deveRetornarPaginado_quandoNaoVazio() {
        Page<Pagamento> page = new PageImpl<>(List.of(pagamentoPendente));
        when(pagamentoRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<PagamentoResponseDTO> result = pagamentoService.listarTodos(PageRequest.of(0, 5));

        assertFalse(result.isEmpty());
        assertEquals(1, result.getContent().size());
        assertEquals(123, result.getContent().get(0).codigoDebito());
    }

    @Test
    void listarTodos_deveLancarNotFound_quandoVazio() {
        when(pagamentoRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(NotFoundException.class, () -> pagamentoService.listarTodos(PageRequest.of(0, 5)));
    }

    @Test
    void excluirPagamentoPendente_deveInativar_quandoStatusPendente() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPendente));

        pagamentoService.excluirPagamentoPendente(1L);

        verify(pagamentoRepository).save(argThat(p -> Boolean.FALSE.equals(p.getAtivo())));
    }

    @Test
    void excluirPagamentoPendente_deveLancarBusiness_quandoJaProcessado() {
        pagamentoPendente.setStatus(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPendente));

        assertThrows(BusinessException.class, () -> pagamentoService.excluirPagamentoPendente(1L));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void atualizarStatusPagamento_dePendenteParaSucesso_deveAtualizar() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPendente));
        Pagamento salvo = clonePagamento(pagamentoPendente);
        salvo.setStatus(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
        when(pagamentoRepository.save(any())).thenReturn(salvo);

        PagamentoResponseDTO response =
                pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);

        assertEquals(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO, response.status());
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    void atualizarStatusPagamento_deveLancarBusiness_quandoSucessoJaProcessado() {
        pagamentoPendente.setStatus(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPendente));

        assertThrows(BusinessException.class,
                () -> pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PENDENTE));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void atualizarStatusPagamento_deveLancarNotFound_quandoNaoEncontrado() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PENDENTE));
    }


    private static Pagamento clonePagamento(Pagamento p) {
        Pagamento c = new Pagamento();
        c.setId(p.getId());
        c.setCodigoDebito(p.getCodigoDebito());
        c.setCpfCnpjPagador(p.getCpfCnpjPagador());
        c.setMetodoPagamentoEnum(p.getMetodoPagamentoEnum());
        c.setNumeroCartao(p.getNumeroCartao());
        c.setValorTransacao(p.getValorTransacao());
        c.setStatus(p.getStatus());
        c.setAtivo(p.getAtivo());
        return c;
    }

}
