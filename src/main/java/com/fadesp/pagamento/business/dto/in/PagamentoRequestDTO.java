package com.fadesp.pagamento.business.dto.in;

import com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum;
import com.fadesp.pagamento.infrastructure.validation.CpfCnpj;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(name = "PagamentoRequest")
public record PagamentoRequestDTO(

        @NotNull(message = "O código do débito é obrigatório")
        @Positive(message = "O código do débito deve ser positivo")
        @Schema(description = "Código do débito associado ao pagamento", example = "123456")
        Integer codigoDebito,

        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        @CpfCnpj
        @Schema(description = "CPF ou CNPJ do pagador, sem máscara", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
        String cpfCnpj,

        @NotNull(message = "O método de pagamento é obrigatório")
        @Schema(description = "Método de pagamento utilizado", example = "PIX", requiredMode = Schema.RequiredMode.REQUIRED)
        MetodoPagamentoEnum metodoPagamento,

        @Schema(description = "Número do cartão (obrigatório apenas para pagamentos com cartão)", example = "5555444433331111")
        @Size(max = 20, message = "O número do cartão deve ter no máximo 20 caracteres")
        String numeroCartao,

        @NotNull(message = "O valor do pagamento é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 15, fraction = 2, message = "O valor deve ter no máximo 15 dígitos inteiros e 2 decimais")
        @Schema(description = "Valor da transação", example = "250.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal valor
) {
}
