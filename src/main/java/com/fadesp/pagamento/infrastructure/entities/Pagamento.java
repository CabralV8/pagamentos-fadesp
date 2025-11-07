package com.fadesp.pagamento.infrastructure.entities;

import com.fadesp.pagamento.infrastructure.enums.MetodoPagamentoEnum;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "pagamento",
        indexes = {
                @Index(name = "idx_pagamento_codigo_debito", columnList = "codigo_debito"),
                @Index(name = "idx_pagamento_cpf_cnpj", columnList = "cpf_cnpj_pagador"),
                @Index(name = "idx_pagamento_status", columnList = "status")
        })
public class Pagamento implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(name = "codigo_debito", unique = true, nullable = false, updatable = false)
    private Integer codigoDebito;

    @NotBlank
    @Size(min = 11, max = 14)
    @Column(name = "cpf_cnpj_pagador", length = 14, nullable = false)
    private String cpfCnpjPagador;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MetodoPagamentoEnum metodoPagamentoEnum;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 15, fraction = 2)
    @Column(name = "valor_transacao", nullable = false, precision = 17, scale = 2)
    private BigDecimal valorTransacao;

    @Size(max = 20)
    @Column(name = "numero_cartao", length = 20)
    private String numeroCartao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private StatusPagamentoEnum status = StatusPagamentoEnum.PENDENTE;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = Boolean.TRUE;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull @Positive Integer getCodigoDebito() {
        return codigoDebito;
    }

    public void setCodigoDebito(@NotNull @Positive Integer codigoDebito) {
        this.codigoDebito = codigoDebito;
    }

    public @NotBlank @Size(min = 11, max = 14) String getCpfCnpjPagador() {
        return cpfCnpjPagador;
    }

    public void setCpfCnpjPagador(@NotBlank @Size(min = 11, max = 14) String cpfCnpjPagador) {
        this.cpfCnpjPagador = cpfCnpjPagador;
    }

    public MetodoPagamentoEnum getMetodoPagamentoEnum() {
        return metodoPagamentoEnum;
    }

    public void setMetodoPagamentoEnum(MetodoPagamentoEnum metodoPagamentoEnum) {
        this.metodoPagamentoEnum = metodoPagamentoEnum;
    }

    public @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal getValorTransacao() {
        return valorTransacao;
    }

    public void setValorTransacao(BigDecimal valorTransacao) {
        if (valorTransacao != null) {
            this.valorTransacao = valorTransacao.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.valorTransacao = null;
        }
    }

    public @Size(max = 20) String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(@Size(max = 20) String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }

    public StatusPagamentoEnum getStatus() {
        return status;
    }

    public void setStatus(StatusPagamentoEnum status) {
        this.status = status;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return ativo == pagamento.ativo
                && Objects.equals(id, pagamento.id)
                && Objects.equals(codigoDebito, pagamento.codigoDebito)
                && Objects.equals(cpfCnpjPagador, pagamento.cpfCnpjPagador)
                && metodoPagamentoEnum == pagamento.metodoPagamentoEnum
                && Objects.equals(valorTransacao, pagamento.valorTransacao)
                && Objects.equals(numeroCartao, pagamento.numeroCartao)
                && status == pagamento.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                codigoDebito,
                cpfCnpjPagador,
                metodoPagamentoEnum,
                valorTransacao,
                numeroCartao,
                status,
                ativo);
    }
}
