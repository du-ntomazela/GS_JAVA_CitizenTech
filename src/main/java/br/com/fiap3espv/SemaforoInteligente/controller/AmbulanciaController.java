package br.com.fiap3espv.SemaforoInteligente.controller;

import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.*;
import br.com.fiap3espv.SemaforoInteligente.service.AmbulanciaService;
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
@RequestMapping("/ambulancia")
@RequiredArgsConstructor
public class AmbulanciaController {
    private final AmbulanciaService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DadosDetalhamentoAmbulancia> cadastrarAmbulancia(
            @RequestBody @Valid DadosCadastroAmbulancia dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoAmbulancia dto = service.cadastrarAmbulancia(dados);
        URI uri = uriBuilder.path("/ambulancia/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'SOCORRISTA')")
    public ResponseEntity<Page<DadosListagemAmbulancias>> listarAmbulancias(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listarAmbulancias(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'SOCORRISTA')")
    public ResponseEntity<DadosDetalhamentoAmbulancia> detalharAmbulancia(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharAmbulancia(id));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DadosDetalhamentoAmbulancia> atualizarAmbulancia(@RequestBody @Valid DadosAtualizacaoAmbulancia dados) {
        return ResponseEntity.ok(service.atualizarAmbulancia(dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirAmbulancia(@PathVariable Long id) {
        service.excluirAmbulancia(id);
        return ResponseEntity.noContent().build();
    }
}
