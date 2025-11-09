package com.fadesp.pagamento.business.service;

import com.fadesp.pagamento.business.converter.PagamentoConverter;
import com.fadesp.pagamento.business.dto.in.PagamentoRequestDTO;
import com.fadesp.pagamento.business.dto.out.PagamentoResponseDTO;
import com.fadesp.pagamento.infrastructure.entities.Pagamento;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import com.fadesp.pagamento.infrastructure.exceptions.BusinessException;
import com.fadesp.pagamento.infrastructure.exceptions.ConflictException;
import com.fadesp.pagamento.infrastructure.exceptions.NotFoundException;
import com.fadesp.pagamento.infrastructure.repository.PagamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PagamentoService {


    private final PagamentoRepository pagamentoRepository;
    private static final BigDecimal VALOR_MINIMO = new BigDecimal("0.01");
    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    public PagamentoService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional
    public PagamentoResponseDTO realizarPagamento(PagamentoRequestDTO requestDTO) {
        log.info("Iniciando criação de pagamento para código de débito {}", requestDTO.codigoDebito());
        validarPagamento(requestDTO);

        Pagamento pagamento = PagamentoConverter.toEntity(requestDTO);
        try {
            Pagamento salvo = pagamentoRepository.save(pagamento);
            return PagamentoConverter.toResponse(salvo);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Código de débito já utilizado: " + requestDTO.codigoDebito(), e);
        }
    }


    @Transactional(readOnly = true)
    public PagamentoResponseDTO buscarPagamentoPorId(Long id) {
        log.info("Buscando pagamento por id {}", id);
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado: id=" + id));
        return PagamentoConverter.toResponse(pagamento);
    }

    @Transactional(readOnly = true)
    public Page<PagamentoResponseDTO> listarTodos(Pageable pageable) {
        log.info("Listando todos os pagamentos com paginação");

        Page<Pagamento> page = pagamentoRepository.findAll(pageable);

        if (page.isEmpty()) {
            throw new NotFoundException("Nenhum pagamento encontrado no sistema.");
        }

        return page.map(PagamentoConverter::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PagamentoResponseDTO> listarComFiltros(
            Integer codigoDebito,
            String cpfCnpjPagador,
            StatusPagamentoEnum status,
            Pageable pageable
    ) {
        log.info("Listando com filtros: codigoDebito={}, cpfCnpjPagador={}, status={}",
                codigoDebito, cpfCnpjPagador, status);
        String doc = (cpfCnpjPagador != null && !cpfCnpjPagador.isBlank())
                ? cpfCnpjPagador.replaceAll("\\D", "")
                : null;

        Page<Pagamento> page = pagamentoRepository.buscarComFiltros(codigoDebito, doc, status, pageable);

        if (page.isEmpty()) {
            throw new NotFoundException("Nenhum pagamento encontrado com os filtros informados.");
        }

        return page.map(PagamentoConverter::toResponse);
    }

    @Transactional
    public void excluirPagamentoPendente(Long id) {
        log.info("Iniciando exclusão (soft delete) do pagamento id={}", id);

        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado: id=" + id));

        if (Boolean.FALSE.equals(pagamento.getAtivo())) {
            throw new BusinessException("Pagamento já está inativo. id=" + id);
        }

        if (pagamento.getStatus() != StatusPagamentoEnum.PENDENTE) {
            throw new BusinessException("Pagamento não pode ser excluído, pois já foi processado. id=" + id);
        }

        pagamento.setAtivo(false);
        pagamentoRepository.save(pagamento);
        log.info("Pagamento id={} inativado com sucesso", id);
    }

    @Transactional
    public PagamentoResponseDTO atualizarStatusPagamento(Long id, StatusPagamentoEnum novoStatus) {
        log.info("Atualizando status do pagamento id={} para {}", id, novoStatus);

        if (novoStatus == null) {
            throw new BusinessException("Novo status não informado.");
        }

        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado: id=" + id));

        if (Boolean.FALSE.equals(pagamento.getAtivo())) {
            throw new BusinessException("Pagamento inativo não pode ter status alterado. id=" + id);
        }

        StatusPagamentoEnum atual = pagamento.getStatus();

        switch (atual) {
            case PENDENTE -> {
                if (novoStatus == StatusPagamentoEnum.PENDENTE
                        || novoStatus == StatusPagamentoEnum.PROCESSADO_COM_SUCESSO
                        || novoStatus == StatusPagamentoEnum.PROCESSADO_COM_FALHA) {
                    pagamento.setStatus(novoStatus);
                } else {
                    throw new BusinessException("Transição inválida de PENDENTE para " + novoStatus + ".");
                }
            }
            case PROCESSADO_COM_FALHA -> {
                if (novoStatus != StatusPagamentoEnum.PENDENTE) {
                    throw new BusinessException("Pagamento com FALHA só pode ser alterado para PENDENTE.");
                }
                pagamento.setStatus(StatusPagamentoEnum.PENDENTE);
            }
            case PROCESSADO_COM_SUCESSO -> {
                throw new BusinessException("Pagamento já processado com SUCESSO não pode ter status alterado.");
            }
        }

        Pagamento atualizado = pagamentoRepository.save(pagamento);
        log.info("Status do pagamento id={} atualizado de {} para {}", id, atual, atualizado.getStatus());
        return PagamentoConverter.toResponse(atualizado);
    }

    private void validarPagamento(PagamentoRequestDTO requestDTO) {
        log.info("Validando dados do pagamento...");

        if (requestDTO.codigoDebito() == null || requestDTO.codigoDebito() <= 0)
            throw new BusinessException("Código de débito inválido ou não informado.");

        String cpfCnpj = requestDTO.cpfCnpj().replaceAll("\\D", "");
        if (cpfCnpj.isEmpty())
            throw new BusinessException("CPF/CNPJ inválido ou não informado.");

        if (requestDTO.metodoPagamento() == null)
            throw new BusinessException("Método de pagamento não informado.");

        switch (requestDTO.metodoPagamento()) {
            case CARTAO_CREDITO, CARTAO_DEBITO -> {
                String numero = requestDTO.numeroCartao();
                if (numero == null || numero.isBlank())
                    throw new BusinessException("O número do cartão é obrigatório para pagamentos com cartão.");
                if (!numero.matches("\\d{12,19}"))
                    throw new BusinessException("Número do cartão inválido. Deve conter entre 12 e 19 dígitos.");
            }
            case PIX, BOLETO -> {
                if (requestDTO.numeroCartao() != null && !requestDTO.numeroCartao().isBlank())
                    throw new BusinessException("Número do cartão não deve ser informado para PIX ou BOLETO.");
            }
        }

        if (requestDTO.valor() == null || requestDTO.valor().compareTo(VALOR_MINIMO) < 0)
            throw new BusinessException("O valor do pagamento deve ser maior ou igual a 0.01.");

        if (requestDTO.valor().scale() > 2)
            throw new BusinessException("O valor do pagamento deve ter no máximo duas casas decimais.");

        log.info("Validação concluída com sucesso para código de débito {}", requestDTO.codigoDebito());
    }
}
