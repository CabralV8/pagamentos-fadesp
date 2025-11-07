package com.fadesp.pagamento.business.dto;

import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import jakarta.validation.constraints.NotNull;

public record AtualizarPagamentoDTO(
        @NotNull(message = "O campo 'id' é obrigatório. *")
        Long id,
        @NotNull(message = "O campo 'status' é obrigatório. *")
        StatusPagamentoEnum status
) {
}