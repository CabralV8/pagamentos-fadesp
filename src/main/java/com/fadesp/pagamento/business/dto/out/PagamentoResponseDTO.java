package com.fadesp.pagamento.business.dto.out;

import com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;

@Schema(name = "PagamentoResponse")
public record PagamentoResponseDTO(

        @Schema(description = "Identificador único do pagamento", example = "1")
        Long id,

        @Schema(description = "Código do débito associado ao pagamento", example = "123456")
        Integer codigoDebito,

        @Schema(description = "CPF ou CNPJ do pagador, sem máscara", example = "12345678901")
        String cpfCnpjPagador,

        @Schema(description = "Método de pagamento utilizado", example = "PIX")
        MetodoPagamentoEnum metodoPagamento,

        @Schema(description = "Valor total da transação", example = "250.00")
        BigDecimal valor,

        @Schema(description = "Status atual do pagamento", example = "PENDENTE")
        StatusPagamentoEnum status,

        @Schema(description = "Indica se o pagamento está ativo (true) ou inativado (false)", example = "true")
        Boolean ativo

) {}