package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CruzamentoRepository extends JpaRepository<Cruzamento, Long> {
    Page<Cruzamento> findAllByAtivoTrueOrderByCodigoAsc(Pageable pageable);

    Page<Cruzamento> findAllByAtivoTrueAndStatusConexaoOrderByCodigoAsc(StatusConexao status, Pageable pageable);

    List<Cruzamento> findAllByAtivoTrue();

    Optional<Cruzamento> findByCodigoAndAtivoTrue(String codigo);

    boolean existsByCodigo(String codigo);

    @Query("""
        select c from Cruzamento c
        where
        c.ativo = true
        and
        c.statusConexao = br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.StatusConexao.ONLINE
        and
        (c.ultimaComunicacao is null or c.ultimaComunicacao < :limite)
    """)
    List<Cruzamento> findOnlineSemComunicacaoDesde(LocalDateTime limite);
}
