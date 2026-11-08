package br.com.fiap3espv.SemaforoInteligente.controller;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.*;
import br.com.fiap3espv.SemaforoInteligente.service.CruzamentoService;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/cruzamento")
@RequiredArgsConstructor
public class CruzamentoController {
    private final CruzamentoService service;
    private final PlanoSemaforicoService planoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DadosDetalhamentoCruzamento> cadastrarCruzamento(
            @RequestBody @Valid DadosCadastroCruzamento dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoCruzamento dto = service.cadastrarCruzamento(dados);
        URI uri = uriBuilder.path("/cruzamento/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    /** Usado pelo mapa ao vivo do painel da central (filtro opcional por status). */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'SOCORRISTA')")
    public ResponseEntity<Page<DadosListagemCruzamentos>> listarCruzamentos(
            @RequestParam(required = false) StatusConexao status,
            @ParameterObject @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(service.listarCruzamentos(status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'SOCORRISTA')")
    public ResponseEntity<DadosDetalhamentoCruzamento> detalharCruzamento(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharCruzamento(id));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DadosDetalhamentoCruzamento> atualizarCruzamento(@RequestBody @Valid DadosAtualizacaoCruzamento dados) {
        return ResponseEntity.ok(service.atualizarCruzamento(dados));
    }

    @PatchMapping("/{id}/modo")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<DadosDetalhamentoCruzamento> alterarModo(
            @PathVariable Long id,
            @RequestBody @Valid DadosAlteracaoModo dados) {
        return ResponseEntity.ok(planoService.alterarModo(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirCruzamento(@PathVariable Long id) {
        service.excluirCruzamento(id);
        return ResponseEntity.noContent().build();
    }
}
