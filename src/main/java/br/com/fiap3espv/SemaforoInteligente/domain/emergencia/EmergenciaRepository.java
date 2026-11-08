package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergenciaRepository extends JpaRepository<Emergencia, Long> {
    boolean existsByAmbulanciaIdAndStatus(Long idAmbulancia, StatusEmergencia status);

    Page<Emergencia> findAllByStatusOrderByDataInicioDesc(StatusEmergencia status, Pageable pageable);

    Page<Emergencia> findAllByOrderByDataInicioDesc(Pageable pageable);
}
