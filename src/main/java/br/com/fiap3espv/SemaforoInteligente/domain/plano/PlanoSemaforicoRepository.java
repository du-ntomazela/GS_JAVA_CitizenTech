package br.com.fiap3espv.SemaforoInteligente.domain.plano;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanoSemaforicoRepository extends JpaRepository<PlanoSemaforico, Long> {
    Optional<PlanoSemaforico> findFirstByCruzamentoIdOrderByDataHoraDescIdDesc(Long idCruzamento);

    Page<PlanoSemaforico> findAllByCruzamentoIdOrderByDataHoraDescIdDesc(Long idCruzamento, Pageable pageable);
}
