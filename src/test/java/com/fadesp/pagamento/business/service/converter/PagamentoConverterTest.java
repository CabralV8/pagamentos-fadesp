package com.fadesp.pagamento.business.service.converter;

import com.fadesp.pagamento.business.converter.PagamentoConverter;
import com.fadesp.pagamento.business.dto.in.PagamentoRequestDTO;
import com.fadesp.pagamento.business.dto.out.PagamentoResponseDTO;
import com.fadesp.pagamento.infrastructure.entities.Pagamento;
import com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoConverterTest {

    @Test
    void toEntity_devePreencherCamposENormalizarDocumento() {

        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                321,
                "123.456.789-09",
                MetodoPagamentoEnum.CARTAO_CREDITO,
                "5555444433331111",
                new BigDecimal("100.10")
        );

        Pagamento entity = PagamentoConverter.toEntity(dto);


        assertNotNull(entity);
        assertEquals(321, entity.getCodigoDebito());

        assertEquals("12345678909", entity.getCpfCnpjPagador());
        assertEquals(MetodoPagamentoEnum.CARTAO_CREDITO, entity.getMetodoPagamentoEnum());
        assertEquals("5555444433331111", entity.getNumeroCartao());
        assertEquals(new BigDecimal("100.10"), entity.getValorTransacao());
        assertEquals(StatusPagamentoEnum.PENDENTE, entity.getStatus());
        assertTrue(entity.getAtivo(), "Pagamento novo deve iniciar ativo=true");
    }

    @Test
    void toEntity_pixSemCartao_deveManterNumeroCartaoNull() {
        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                999,
                "11222333000181",
                MetodoPagamentoEnum.PIX,
                null,
                new BigDecimal("250.00")
        );

        Pagamento entity = PagamentoConverter.toEntity(dto);

        assertNull(entity.getNumeroCartao());
        assertEquals(MetodoPagamentoEnum.PIX, entity.getMetodoPagamentoEnum());
        assertEquals("11222333000181", entity.getCpfCnpjPagador());
        assertEquals(new BigDecimal("250.00"), entity.getValorTransacao());
        assertEquals(StatusPagamentoEnum.PENDENTE, entity.getStatus());
        assertTrue(entity.getAtivo());
    }

    @Test
    void toResponse_deveMapearTodosOsCampos() {

        Pagamento p = new Pagamento();
        p.setId(7L);
        p.setCodigoDebito(777);
        p.setCpfCnpjPagador("12345678909");
        p.setMetodoPagamentoEnum(MetodoPagamentoEnum.CARTAO_DEBITO);
        p.setNumeroCartao("4444333322221111");
        p.setValorTransacao(new BigDecimal("73.45"));
        p.setStatus(StatusPagamentoEnum.PROCESSADO_COM_FALHA);
        p.setAtivo(true);


        PagamentoResponseDTO dto = PagamentoConverter.toResponse(p);


        assertNotNull(dto);
        assertEquals(7L, dto.id());
        assertEquals(777, dto.codigoDebito());
        assertEquals("12345678909", dto.cpfCnpjPagador());
        assertEquals(MetodoPagamentoEnum.CARTAO_DEBITO, dto.metodoPagamento());
        assertEquals(new BigDecimal("73.45"), dto.valor());
        assertEquals(StatusPagamentoEnum.PROCESSADO_COM_FALHA, dto.status());
        assertTrue(dto.ativo());
    }
}
