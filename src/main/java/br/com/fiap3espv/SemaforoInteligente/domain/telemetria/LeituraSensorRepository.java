package br.com.fiap3espv.SemaforoInteligente.domain.telemetria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {
    Page<LeituraSensor> findAllByCruzamentoIdOrderByDataHoraDesc(Long idCruzamento, Pageable pageable);
}
