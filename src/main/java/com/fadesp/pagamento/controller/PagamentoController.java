package com.fadesp.pagamento.controller;

import com.fadesp.pagamento.business.dto.in.PagamentoRequestDTO;
import com.fadesp.pagamento.business.dto.out.PagamentoResponseDTO;
import com.fadesp.pagamento.business.service.PagamentoService;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos", description = "Endpoints para criação, consulta, atualização e exclusão lógica de pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }


    @PostMapping
    @Operation(summary = "Criar pagamento", description = "Registra um novo pagamento no sistema.")
    public ResponseEntity<PagamentoResponseDTO> criarPagamento(@Valid @RequestBody PagamentoRequestDTO dto) {
        PagamentoResponseDTO response = pagamentoService.realizarPagamento(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/pagamentos/" + response.id())
                .body(response);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID")
    public ResponseEntity<PagamentoResponseDTO> buscarPagamentoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.buscarPagamentoPorId(id));
    }



    @GetMapping
    @Operation(summary = "Listar pagamentos (com filtros opcionais e paginação)")
    public ResponseEntity<Page<PagamentoResponseDTO>> listarPagamentos(
            @RequestParam(required = false) Integer codigoDebito,
            @RequestParam(required = false) String cpfCnpjPagador,
            @RequestParam(required = false) StatusPagamentoEnum status,
            @ParameterObject Pageable pageable
    ) {
        Page<PagamentoResponseDTO> page = pagamentoService.listarComFiltros(codigoDebito, cpfCnpjPagador, status, pageable);
        return ResponseEntity.ok(page);
    }



    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pagamento")
    public ResponseEntity<PagamentoResponseDTO> atualizarStatusPagamento(
            @PathVariable Long id,
            @RequestParam("novoStatus") StatusPagamentoEnum novoStatus) {
        return ResponseEntity.ok(pagamentoService.atualizarStatusPagamento(id, novoStatus));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Exclusão lógica do pagamento (apenas se PENDENTE)")
    public ResponseEntity<Void> excluirPagamentoPendente(@PathVariable Long id) {
        pagamentoService.excluirPagamentoPendente(id);
        return ResponseEntity.noContent().build();
    }
}

