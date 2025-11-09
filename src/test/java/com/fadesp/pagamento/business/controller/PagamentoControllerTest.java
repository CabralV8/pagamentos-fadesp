package com.fadesp.pagamento.business.controller;

import com.fadesp.pagamento.controller.PagamentoController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fadesp.pagamento.business.dto.in.PagamentoRequestDTO;
import com.fadesp.pagamento.business.dto.out.PagamentoResponseDTO;
import com.fadesp.pagamento.business.service.PagamentoService;
import com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagamentoController.class)
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagamentoService pagamentoService;


    private PagamentoRequestDTO novoPagamentoRequest() {
        return new PagamentoRequestDTO(
                2001,
                "52998224725",
                MetodoPagamentoEnum.PIX,
                null,
                new BigDecimal("150.00")
        );
    }

    private PagamentoResponseDTO resposta(Long id, StatusPagamentoEnum status) {
        return new PagamentoResponseDTO(
                id,
                2001,
                "52998224725",
                MetodoPagamentoEnum.PIX,
                new BigDecimal("150.00"),
                status,
                true
        );
    }


    @Test
    @DisplayName("POST /api/pagamentos → 201 Created + Location")
    void criarPagamento_deveRetornar201() throws Exception {
        var req = novoPagamentoRequest();
        var resp = resposta(1L, StatusPagamentoEnum.PENDENTE);

        given(pagamentoService.realizarPagamento(any(PagamentoRequestDTO.class)))
                .willReturn(resp);

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pagamentos/1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PENDENTE")));

        verify(pagamentoService, times(1)).realizarPagamento(any(PagamentoRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/pagamentos/{id} → 200 OK")
    void buscarPorId_deveRetornar200() throws Exception {
        given(pagamentoService.buscarPagamentoPorId(1L))
                .willReturn(resposta(1L, StatusPagamentoEnum.PENDENTE));

        mockMvc.perform(get("/api/pagamentos/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.codigoDebito", is(2001)))
                .andExpect(jsonPath("$.cpfCnpjPagador", is("52998224725")))
                .andExpect(jsonPath("$.status", is("PENDENTE")));

        verify(pagamentoService, times(1)).buscarPagamentoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/pagamentos (filtros+página) → 200 OK")
    void listarComFiltros_deveRetornarPagina() throws Exception {
        var item = resposta(7L, StatusPagamentoEnum.PENDENTE);
        Page<PagamentoResponseDTO> page = new PageImpl<>(List.of(item));

        given(pagamentoService.listarComFiltros(
                any(), any(), any(), ArgumentMatchers.any()))
                .willReturn(page);

        mockMvc.perform(get("/api/pagamentos")
                        .param("codigoDebito", "2001")
                        .param("cpfCnpjPagador", "52998224725")
                        .param("status", "PENDENTE")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(7)))
                .andExpect(jsonPath("$.content[0].status", is("PENDENTE")));

        verify(pagamentoService, times(1))
                .listarComFiltros(eq(2001), eq("52998224725"),
                        eq(StatusPagamentoEnum.PENDENTE), any());
    }

    @Test
    @DisplayName("PATCH /api/pagamentos/{id}/status → 200 OK")
    void atualizarStatus_deveRetornar200() throws Exception {
        given(pagamentoService.atualizarStatusPagamento(1L, StatusPagamentoEnum.PROCESSADO_COM_SUCESSO))
                .willReturn(resposta(1L, StatusPagamentoEnum.PROCESSADO_COM_SUCESSO));

        mockMvc.perform(patch("/api/pagamentos/{id}/status", 1L)
                        .param("novoStatus", "PROCESSADO_COM_SUCESSO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PROCESSADO_COM_SUCESSO")));

        verify(pagamentoService, times(1))
                .atualizarStatusPagamento(1L, StatusPagamentoEnum.PROCESSADO_COM_SUCESSO);
    }

    @Test
    @DisplayName("DELETE /api/pagamentos/{id} → 204 No Content")
    void excluir_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/pagamentos/{id}", 5L))
                .andExpect(status().isNoContent());

        verify(pagamentoService, times(1)).excluirPagamentoPendente(5L);
    }

    @Test
    @DisplayName("POST com corpo inválido → 422 Unprocessable Entity (exemplo)")
    void criarPagamento_invalido_deveRetornar422() throws Exception {
        String bodyInvalido = """
                {
                  "codigoDebito": 2001,
                  "cpfCnpj": "",
                  "valor": 10.00
                }
                """;

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isUnprocessableEntity());
        verify(pagamentoService, times(0)).realizarPagamento(any());
    }
}