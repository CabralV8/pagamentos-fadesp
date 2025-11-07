package com.fadesp.pagamento.infrastructure.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public enum StatusPagamentoEnum {

    PENDENTE("Pendente de processamento"),
    PROCESSADO_COM_FALHA("Processado com falha"),
    PROCESSADO_COM_SUCESSO("Processado com sucesso");

    private final String descricao;

    StatusPagamentoEnum(String descricao){
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static StatusPagamentoEnum fromString(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return StatusPagamentoEnum.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status de pagamento inválido: " + value
                    + ". Valores aceitos: " + Arrays.toString(values()));
        }
    }
}
