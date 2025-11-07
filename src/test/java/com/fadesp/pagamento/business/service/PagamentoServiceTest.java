package com.fadesp.pagamento.business.service;

import com.fadesp.pagamento.business.dto.in.PagamentoRequestDTO;
import com.fadesp.pagamento.business.dto.out.PagamentoResponseDTO;
import com.fadesp.pagamento.infrastructure.entities.Pagamento;
import com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import com.fadesp.pagamento.infrastructure.exceptions.BusinessException;
import com.fadesp.pagamento.infrastructure.exceptions.ConflictException;
import com.fadesp.pagamento.infrastructure.exceptions.NotFoundException;
import com.fadesp.pagamento.infrastructure.repository.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    private PagamentoRequestDTO requestValido;
    private Pagamento pagamentoPendente;

    @BeforeEach
    void setUp() {
        // (codigoDebito, cpfCnpj, metodoPagamento, numeroCartao, valor)
        requestValido = new PagamentoRequestDTO(
                123,
                "12345678909",
                MetodoPagamentoEnum.CARTAO_CREDITO,
                "123456789012",
                new BigDecimal("100.00")
        );

        pagamentoPendente = new Pagamento();
        pagamentoPendente.setId(1L);
        pagamentoPendente.setCodigoDebito(123);
        pagamentoPendente.setCpfCnpjPagador("12345678909");
        pagamentoPendente.setMetodoPagamentoEnum(MetodoPagamentoEnum.CARTAO_CREDITO);
        pagamentoPendente.setNumeroCartao("123456789012");
        pagamentoPendente.setValorTransacao(new BigDecimal("100.00"));
        pagamentoPendente.setAtivo(true);
        pagamentoPendente.setStatus(StatusPagamentoEnum.PENDENTE);
    }

    @Test
    void realizarPagamento_deveCriarPagamentoComSucesso_quandoValido() {
        when(pagamentoRepository.existsByCodigoDebito(anyInt())).thenReturn(false);
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamentoPendente);

        PagamentoResponseDTO response = pagamentoService.realizarPagamento(requestValido);

        assertNotNull(response);
        assertEquals(123, response.codigoDebito());
        assertEquals(StatusPagamentoEnum.PENDENTE, response.status());
        verify(pagamentoRepository, times(1)).save(any(Pagamento.class));
    }

    @Test
    void realizarPagamento_deveLancarConflict_quandoCodigoDebitoDuplicado() {
        when(pagamentoRepository.existsByCodigoDebito(anyInt())).thenReturn(true);

        assertThrows(ConflictException.class, () -> pagamentoService.realizarPagamento(requestValido));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void realizarPagamento_deveLancarBusiness_quandoValorInvalido() {
        PagamentoRequestDTO invalido = new PagamentoRequestDTO(
                123,
                "12345678909",
                MetodoPagamentoEnum.PIX,
                null,
                new BigDecimal("0.00")
        );

        assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(invalido));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void realizarPagamento_deveLancarConflict_quandoDataIntegrityViolation() {
        when(pagamentoRepository.existsByCodigoDebito(anyInt())).thenReturn(false);
        when(pagamentoRepository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(ConflictException.class, () -> pagamentoService.realizarPagamento(requestValido));
    }

    @Test
    void buscarPagamentoPorId_deveRetornar_quandoExiste() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPendente));

        PagamentoResponseDTO resp = pagamentoService.buscarPagamentoPorId(1L);

        assertNotNull(resp);
        assertEquals(123, resp.codigoDebito());
        verify(pagamentoRepository).findById(1L);
    }

    @Test
    void buscarPagamentoPorId_deveLancarNotFound_quandoNaoExiste() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pagamentoService.buscarPagamentoPorId(99L));
    }

    @Test
    void listarTodos_deveRetornarPagina_quandoNaoVazio() {
        Page<Pagamento> page = new PageImpl<>(List.of(pagamentoPendente));
        when(pagamentoRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<PagamentoResponseDTO> result = pagamentoService.listarTodos(PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listarTodos_deveLancarNotFound_quandoVazio() {
        when(pagamentoRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        assertThrows(NotFoundException.class, () -> pagamentoService.listarTodos(PageRequest.of(0, 10)));
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
    void atualizarStatus_dePendenteParaSucesso_deveAtualizar() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPendente));

        Pagamento salvo = clonePagamento(pagamentoPendente);
        salvo.setStatus(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
        when(pagamentoRepository.save(any())).thenReturn(salvo);

        PagamentoResponseDTO r =
                pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);

        assertEquals(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO, r.status());
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    void atualizarStatus_deveLancarBusiness_quandoSucessoJaProcessado() {
        pagamentoPendente.setStatus(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPendente));

        assertThrows(BusinessException.class,
                () -> pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PENDENTE));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void atualizarStatus_deveLancarNotFound_quandoNaoEncontrado() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PENDENTE));
    }

    // helper
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
