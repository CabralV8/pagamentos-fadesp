package com.fadesp.pagamento.infrastructure.repository;

import com.fadesp.pagamento.infrastructure.entities.Pagamento;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {


    @Query("""
        SELECT p
          FROM Pagamento p
         WHERE (:codigoDebito IS NULL OR p.codigoDebito = :codigoDebito)
           AND (:cpfCnpjPagador IS NULL OR p.cpfCnpjPagador = :cpfCnpjPagador)
           AND (:status IS NULL OR p.status = :status)
           AND p.ativo = TRUE
        """)
    Page<Pagamento> buscarComFiltros(
            @Param("codigoDebito") Integer codigoDebito,
            @Param("cpfCnpjPagador") String cpfCnpjPagador,
            @Param("status") StatusPagamentoEnum status,
            Pageable pageable
    );
}
