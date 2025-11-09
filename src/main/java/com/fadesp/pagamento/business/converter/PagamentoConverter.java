package com.fadesp.pagamento.business.converter;

import com.fadesp.pagamento.business.dto.in.PagamentoRequestDTO;
import com.fadesp.pagamento.business.dto.out.PagamentoResponseDTO;
import com.fadesp.pagamento.infrastructure.entities.Pagamento;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;

public class PagamentoConverter {

    public static Pagamento toEntity(PagamentoRequestDTO dto){
        Pagamento pagamento = new Pagamento();
        pagamento.setCodigoDebito(dto.codigoDebito());
        pagamento.setCpfCnpjPagador(dto.cpfCnpj().replaceAll("\\D", ""));
        pagamento.setMetodoPagamentoEnum(dto.metodoPagamento());
        pagamento.setNumeroCartao(dto.numeroCartao());
        pagamento.setValorTransacao(dto.valor());
        pagamento.setStatus(StatusPagamentoEnum.PENDENTE);
        pagamento.setAtivo(true);
        return pagamento;
    }

    public static PagamentoResponseDTO toResponse(Pagamento pagamento){
        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getCodigoDebito(),
                pagamento.getCpfCnpjPagador(),
                pagamento.getMetodoPagamentoEnum(),
                pagamento.getValorTransacao(),
                pagamento.getStatus(),
                pagamento.getAtivo()
        );
    }
}
