package br.com.fiap3espv.SemaforoInteligente.domain.ambulancia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AmbulanciaRepository extends JpaRepository<Ambulancia, Long> {
    Page<Ambulancia> findAllByAtivoTrueOrderByCodigoAsc(Pageable pageable);

    boolean existsByCodigoOrPlaca(String codigo, String placa);

    @Query("""
        select a.ativo
        from Ambulancia a
        where
        a.id = :id
    """)
    boolean findAtivoById(Long id);
}
