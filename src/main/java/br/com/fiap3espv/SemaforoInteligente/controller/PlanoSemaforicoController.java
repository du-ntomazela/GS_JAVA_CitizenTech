package br.com.fiap3espv.SemaforoInteligente.controller;

import br.com.fiap3espv.SemaforoInteligente.domain.plano.DadosDetalhamentoPlano;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.DadosPlanoManual;
import br.com.fiap3espv.SemaforoInteligente.service.PlanoSemaforicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cruzamento/{id}/plano")
@RequiredArgsConstructor
public class PlanoSemaforicoController {
    private final PlanoSemaforicoService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<DadosDetalhamentoPlano> planoAtual(@PathVariable Long id) {
        return ResponseEntity.ok(service.planoAtual(id));
    }

    @GetMapping("/historico")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Page<DadosDetalhamentoPlano>> historico(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.historico(id, pageable));
    }

    /** Intervencao manual da operadora de transito. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<DadosDetalhamentoPlano> aplicarPlanoManual(
            @PathVariable Long id,
            @RequestBody @Valid DadosPlanoManual dados) {
        return ResponseEntity.ok(service.aplicarPlanoManual(id, dados));
    }
}
