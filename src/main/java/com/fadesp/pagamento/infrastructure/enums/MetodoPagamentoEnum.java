package com.fadesp.pagamento.infrastructure.enums;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum MetodoPagamentoEnum {
    BOLETO,
    PIX,
    CARTAO_DEBITO,
    CARTAO_CREDITO;

    @JsonCreator
    public static MetodoPagamentoEnum fromString(String metodo) {
        for (MetodoPagamentoEnum m : values()) {
            if (m.name().equalsIgnoreCase(metodo)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Método Inválido: " + metodo);
    }

    public String getNome() {
        return name();
    }
}
