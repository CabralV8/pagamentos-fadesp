package com.fadesp.pagamento.business.controller;

import com.fadesp.pagamento.controller.PagamentoController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fadesp.pagamento.business.dto.in.PagamentoRequestDTO;
import com.fadesp.pagamento.business.dto.out.PagamentoResponseDTO;
import com.fadesp.pagamento.business.service.PagamentoService;
import com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@WebMvcTest(PagamentoController.class)
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PagamentoService pagamentoService;

    @Autowired
    private ObjectMapper objectMapper;

    private PagamentoRequestDTO requestDTO;
    private PagamentoResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        requestDTO = new PagamentoRequestDTO(
                101,
                "12345678909",
                MetodoPagamentoEnum.CARTAO_CREDITO,
                "5555444433331111",
                new BigDecimal("150.00")
        );

        responseDTO = new PagamentoResponseDTO(
                1L,
                101,
                "12345678909",
                MetodoPagamentoEnum.CARTAO_CREDITO,
                new BigDecimal("150.00"),
                StatusPagamentoEnum.PENDENTE,
                true
        );
    }

    @Test
    void deveCriarPagamentoComSucesso() throws Exception {
        when(pagamentoService.realizarPagamento(any(PagamentoRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/pagamentos/1")))
                .andExpect(jsonPath("$.codigoDebito", is(101)))
                .andExpect(jsonPath("$.status", is("PENDENTE")));

        verify(pagamentoService).realizarPagamento(any(PagamentoRequestDTO.class));
    }

    @Test
    void deveBuscarPagamentoPorId() throws Exception {
        when(pagamentoService.buscarPagamentoPorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/pagamentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoDebito", is(101)))
                .andExpect(jsonPath("$.cpfCnpjPagador", is("12345678909")));

        verify(pagamentoService).buscarPagamentoPorId(1L);
    }

    @Test
    void deveListarTodosPagamentosPaginados() throws Exception {
        Page<PagamentoResponseDTO> page = new PageImpl<>(List.of(responseDTO));
        when(pagamentoService.listarTodos(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/pagamentos")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].codigoDebito", is(101)));

        verify(pagamentoService).listarTodos(any(PageRequest.class));
    }

    @Test
    void deveListarPorCpfCnpj() throws Exception {
        when(pagamentoService.buscarPorCpfCnpj("12345678909")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/pagamentos/cpf-cnpj/12345678909"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoDebito", is(101)));

        verify(pagamentoService).buscarPorCpfCnpj("12345678909");
    }

    @Test
    void deveAtualizarStatusComSucesso() throws Exception {
        PagamentoResponseDTO atualizado = new PagamentoResponseDTO(
                1L,
                101,
                "12345678909",
                MetodoPagamentoEnum.CARTAO_CREDITO,
                new BigDecimal("150.00"),
                StatusPagamentoEnum.PROCESSADO_COM_SUCESSO,
                true
        );

        when(pagamentoService.atualizarStatusPagamento(eq(1L), eq(StatusPagamentoEnum.PROCESSADO_COM_SUCESSO)))
                .thenReturn(atualizado);

        mockMvc.perform(patch("/api/pagamentos/1/status")
                        .param("novoStatus", "PROCESSADO_COM_SUCESSO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PROCESSADO_COM_SUCESSO")));

        verify(pagamentoService).atualizarStatusPagamento(1L, StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
    }

    @Test
    void deveExcluirPagamentoComSucesso() throws Exception {
        doNothing().when(pagamentoService).excluirPagamentoPendente(1L);

        mockMvc.perform(delete("/api/pagamentos/1"))
                .andExpect(status().isNoContent());

        verify(pagamentoService).excluirPagamentoPendente(1L);
    }
}
