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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    private PagamentoRequestDTO dtoPixValido;
    private Pagamento entidadePendente;

    @BeforeEach
    void setUp() {
        dtoPixValido = new PagamentoRequestDTO(
                2001,
                "529.982.247-25",
                MetodoPagamentoEnum.PIX,
                null,
                new BigDecimal("150.00")
        );

        entidadePendente = new Pagamento();
        entidadePendente.setId(1L);
        entidadePendente.setCodigoDebito(2001);
        entidadePendente.setCpfCnpjPagador("52998224725");
        entidadePendente.setMetodoPagamentoEnum(MetodoPagamentoEnum.PIX);
        entidadePendente.setNumeroCartao(null);
        entidadePendente.setValorTransacao(new BigDecimal("150.00"));
        entidadePendente.setStatus(StatusPagamentoEnum.PENDENTE);
        entidadePendente.setAtivo(true);
    }



    @Test
    @DisplayName("realizarPagamento - deve salvar e retornar response")
    void realizarPagamento_sucesso() {
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> {
            Pagamento p = inv.getArgument(0, Pagamento.class);
            p.setId(1L);
            return p;
        });

        PagamentoResponseDTO resp = pagamentoService.realizarPagamento(dtoPixValido);

        assertNotNull(resp);
        assertEquals(1L, resp.id());
        assertEquals(2001, resp.codigoDebito());
        assertEquals("52998224725", resp.cpfCnpjPagador());
        assertEquals(MetodoPagamentoEnum.PIX, resp.metodoPagamento());
        assertEquals(new BigDecimal("150.00"), resp.valor());
        assertEquals(StatusPagamentoEnum.PENDENTE, resp.status());


        ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(captor.capture());
        assertEquals("52998224725", captor.getValue().getCpfCnpjPagador());
    }

    @Test
    @DisplayName("realizarPagamento - deve lançar ConflictException quando unique-key violada")
    void realizarPagamento_conflito() {
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenThrow(new DataIntegrityViolationException("uk_codigo_debito"));

        assertThrows(ConflictException.class, () -> pagamentoService.realizarPagamento(dtoPixValido));
    }

    @Nested
    @DisplayName("realizarPagamento - validações")
    class ValidacoesCriacao {
        @Test
        void codigoDebitoInvalido() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(0, "123", MetodoPagamentoEnum.PIX, null, new BigDecimal("1.00"));
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }

        @Test
        void cpfCnpjVazio() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(1, "", MetodoPagamentoEnum.PIX, null, new BigDecimal("1.00"));
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }

        @Test
        void metodoPagamentoNulo() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(1, "12345678901", null, null, new BigDecimal("1.00"));
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }

        @Test
        void cartaoSemNumero() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(1, "12345678901", MetodoPagamentoEnum.CARTAO_CREDITO, null, new BigDecimal("1.00"));
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }

        @Test
        void cartaoNumeroInvalido() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(1, "12345678901", MetodoPagamentoEnum.CARTAO_DEBITO, "12345", new BigDecimal("1.00"));
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }

        @Test
        void pixComNumeroCartaoInformado() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(1, "12345678901", MetodoPagamentoEnum.PIX, "5555", new BigDecimal("1.00"));
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }

        @Test
        void valorNulo() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(1, "12345678901", MetodoPagamentoEnum.PIX, null, null);
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }

        @Test
        void valorMenorQueMinimo() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(1, "12345678901", MetodoPagamentoEnum.PIX, null, new BigDecimal("0.00"));
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }

        @Test
        void valorComMaisDeDuasCasas() {
            PagamentoRequestDTO dto = new PagamentoRequestDTO(1, "12345678901", MetodoPagamentoEnum.PIX, null, new BigDecimal("1.001"));
            assertThrows(BusinessException.class, () -> pagamentoService.realizarPagamento(dto));
        }
    }



    @Test
    void buscarPagamentoPorId_sucesso() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(entidadePendente));

        PagamentoResponseDTO resp = pagamentoService.buscarPagamentoPorId(1L);

        assertEquals(1L, resp.id());
        assertEquals(StatusPagamentoEnum.PENDENTE, resp.status());
    }

    @Test
    void buscarPagamentoPorId_notFound() {
        when(pagamentoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> pagamentoService.buscarPagamentoPorId(99L));
    }



    @Test
    void listarTodos_sucesso() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<Pagamento> page = new PageImpl<>(List.of(entidadePendente), pageable, 1);
        when(pagamentoRepository.findAll(pageable)).thenReturn(page);

        Page<PagamentoResponseDTO> resp = pagamentoService.listarTodos(pageable);

        assertEquals(1, resp.getTotalElements());
        assertEquals(1L, resp.getContent().get(0).id());
    }

    @Test
    void listarTodos_vazio_deveLancarNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pagamentoRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        assertThrows(NotFoundException.class, () -> pagamentoService.listarTodos(pageable));
    }



    @Test
    void listarComFiltros_sucesso_normalizaCpf() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Pagamento> page = new PageImpl<>(List.of(entidadePendente));
        when(pagamentoRepository.buscarComFiltros(eq(2001), eq("52998224725"),
                eq(StatusPagamentoEnum.PENDENTE), eq(pageable))).thenReturn(page);

        Page<PagamentoResponseDTO> resp = pagamentoService.listarComFiltros(
                2001, "529.982.247-25", StatusPagamentoEnum.PENDENTE, pageable);

        assertEquals(1, resp.getTotalElements());
        assertEquals("52998224725", resp.getContent().get(0).cpfCnpjPagador());
    }

    @Test
    void listarComFiltros_vazio_deveLancarNotFound() {
        Pageable pageable = PageRequest.of(0, 5);
        when(pagamentoRepository.buscarComFiltros(any(), any(), any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        assertThrows(NotFoundException.class,
                () -> pagamentoService.listarComFiltros(null, null, null, pageable));
    }



    @Test
    void excluirPagamentoPendente_sucesso() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(entidadePendente));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        pagamentoService.excluirPagamentoPendente(1L);

        ArgumentCaptor<Pagamento> captor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository).save(captor.capture());
        assertFalse(captor.getValue().getAtivo());
    }

    @Test
    void excluirPagamentoPendente_notFound() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> pagamentoService.excluirPagamentoPendente(1L));
    }

    @Test
    void excluirPagamentoPendente_inativo_deveLancarBusiness() {
        Pagamento p = clone(entidadePendente);
        p.setAtivo(false);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(BusinessException.class, () -> pagamentoService.excluirPagamentoPendente(1L));
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void excluirPagamentoPendente_statusNaoPendente_deveLancarBusiness() {
        Pagamento p = clone(entidadePendente);
        p.setStatus(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(BusinessException.class, () -> pagamentoService.excluirPagamentoPendente(1L));
        verify(pagamentoRepository, never()).save(any());
    }



    @Test
    void atualizarStatusPagamento_novoStatusNulo() {
        assertThrows(BusinessException.class, () -> pagamentoService.atualizarStatusPagamento(1L, null));
    }

    @Test
    void atualizarStatusPagamento_notFound() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PENDENTE));
    }

    @Test
    void atualizarStatusPagamento_inativo_deveLancarBusiness() {
        Pagamento p = clone(entidadePendente);
        p.setAtivo(false);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(BusinessException.class,
                () -> pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PENDENTE));
    }

    @Test
    void atualizarStatusPagamento_pendenteParaSucesso() {
        Pagamento p = clone(entidadePendente);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        Pagamento salvo = clone(p); salvo.setStatus(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
        when(pagamentoRepository.save(any())).thenReturn(salvo);

        PagamentoResponseDTO resp =
                pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);

        assertEquals(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO, resp.status());
    }

    @Test
    void atualizarStatusPagamento_falhaParaPendente_ok() {

        Pagamento pFalha = clone(entidadePendente);
        pFalha.setStatus(StatusPagamentoEnum.PROCESSADO_COM_FALHA);

        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pFalha));

        Pagamento salvo = clone(pFalha);
        salvo.setStatus(StatusPagamentoEnum.PENDENTE);
        when(pagamentoRepository.save(any())).thenReturn(salvo);

        PagamentoResponseDTO resp =
                pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PENDENTE);

        assertEquals(StatusPagamentoEnum.PENDENTE, resp.status());
    }

    @Test
    void atualizarStatusPagamento_falhaParaSucesso_deveLancarBusiness() {

        Pagamento pFalha = clone(entidadePendente);
        pFalha.setStatus(StatusPagamentoEnum.PROCESSADO_COM_FALHA);

        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pFalha));

        assertThrows(BusinessException.class, () ->
                pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PROCESSADO_COM_SUCESSO));

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void atualizarStatusPagamento_sucessoNaoPermiteAlterar() {
        Pagamento p = clone(entidadePendente);
        p.setStatus(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(BusinessException.class,
                () -> pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PENDENTE));
    }



    private static Pagamento clone(Pagamento o) {
        Pagamento c = new Pagamento();
        c.setId(o.getId());
        c.setCodigoDebito(o.getCodigoDebito());
        c.setCpfCnpjPagador(o.getCpfCnpjPagador());
        c.setMetodoPagamentoEnum(o.getMetodoPagamentoEnum());
        c.setNumeroCartao(o.getNumeroCartao());
        c.setValorTransacao(o.getValorTransacao());
        c.setStatus(o.getStatus());
        c.setAtivo(o.getAtivo());
        return c;
    }
}
