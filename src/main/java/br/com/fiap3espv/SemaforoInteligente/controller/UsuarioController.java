package br.com.fiap3espv.SemaforoInteligente.controller;

import br.com.fiap3espv.SemaforoInteligente.domain.usuario.DadosCadastroUsuario;
import br.com.fiap3espv.SemaforoInteligente.domain.usuario.DadosDetalhamentoUsuario;
import br.com.fiap3espv.SemaforoInteligente.service.UsuarioService;
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
@RequestMapping("/usuario")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {
    private final UsuarioService service;

    @PostMapping
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrarUsuario(
            @RequestBody @Valid DadosCadastroUsuario dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoUsuario dto = service.cadastrarUsuario(dados);
        URI uri = uriBuilder.path("/usuario/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    public ResponseEntity<Page<DadosDetalhamentoUsuario>> listarUsuarios(
            @ParameterObject @PageableDefault(size = 10, sort = "login") Pageable pageable) {
        return ResponseEntity.ok(service.listarUsuarios(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Long id) {
        service.excluirUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
