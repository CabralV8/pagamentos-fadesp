package com.fadesp.pagamento.infrastructure.repository;

import com.fadesp.pagamento.infrastructure.entities.Pagamento;
import com.fadesp.pagamento.infrastructure.enums.StatusPagamentoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    // filtros necessários
    List<Pagamento> findByCodigoDebito(Integer codigoDebito);

    List<Pagamento> findByCpfCnpjPagador(String cpfCnpjPagador);

    List<Pagamento> findByStatus(StatusPagamentoEnum status);

    boolean existsByCodigoDebito(Integer codigoDebito);

    List<Pagamento> findByCodigoDebitoAndCpfCnpjPagador(Integer codigoDebito, String cpfCnpjPagador);

    List<Pagamento> findByCpfCnpjPagadorAndStatus(String cpfCnpjPagador, StatusPagamentoEnum status);
}
