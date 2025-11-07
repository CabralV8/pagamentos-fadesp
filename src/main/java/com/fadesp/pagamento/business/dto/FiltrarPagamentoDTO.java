package com.fadesp.pagamento.business.dto;

import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;

public record FiltrarPagamentoDTO(Integer codigoDebito,
                                  String cpfCnpjPagador,
                                  StatusPagamentoEnum status) {
}
