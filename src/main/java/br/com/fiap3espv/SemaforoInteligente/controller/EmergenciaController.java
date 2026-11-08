package br.com.fiap3espv.SemaforoInteligente.controller;

import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.CentralDeEmergencias;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.DadosAberturaEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.DetalhamentoEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.StatusEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.usuario.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/emergencia")
@RequiredArgsConstructor
public class EmergenciaController {
    private final CentralDeEmergencias central;

    /** 1. App abre a emergencia: o backend calcula a rota e devolve os cruzamentos. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCORRISTA')")
    public ResponseEntity<DetalhamentoEmergencia> abrirEmergencia(
            @RequestBody @Valid DadosAberturaEmergencia dados,
            @AuthenticationPrincipal Usuario usuario,
            UriComponentsBuilder uriBuilder) {
        DetalhamentoEmergencia dto = central.abrir(dados, usuario);
        URI uri = uriBuilder.path("/emergencia/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    /** 2. Durante o trajeto: GPS a cada 1-2 s; a resposta traz os cruzamentos ja liberados. */
    @PostMapping("/{id}/posicao")
    @PreAuthorize("hasAnyRole('ADMIN', 'SOCORRISTA')")
    public ResponseEntity<DetalhamentoEmergencia> atualizarPosicao(
            @PathVariable Long id,
            @RequestBody @Valid DadosCoordenada dados,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(central.atualizarPosicao(id, dados, usuario));
    }

    /** 4. Encerrar: libera todos os cruzamentos e grava o historico. */
    @PostMapping("/{id}/encerramento")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'SOCORRISTA')")
    public ResponseEntity<DetalhamentoEmergencia> encerrarEmergencia(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(central.encerrar(id, usuario));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'SOCORRISTA')")
    public ResponseEntity<DetalhamentoEmergencia> detalharEmergencia(@PathVariable Long id) {
        return ResponseEntity.ok(central.detalhar(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Page<DetalhamentoEmergencia>> listarEmergencias(
            @RequestParam(required = false) StatusEmergencia status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(central.listar(status, pageable));
    }
}
