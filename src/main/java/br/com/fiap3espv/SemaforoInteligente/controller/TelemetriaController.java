package br.com.fiap3espv.SemaforoInteligente.controller;

import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosDetalhamentoLeitura;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosPacoteSensores;
import br.com.fiap3espv.SemaforoInteligente.service.DadosProcessamentoTelemetria;
import br.com.fiap3espv.SemaforoInteligente.service.TelemetriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * O caminho principal da telemetria e o MQTT (cidade/{cruzamento}/sensores).
 * Este endpoint REST faz o mesmo processamento e serve para testes e para a demonstracao sem broker.
 */
@RestController
@RequestMapping("/cruzamento/{id}/leitura")
@RequiredArgsConstructor
public class TelemetriaController {
    private final TelemetriaService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<DadosProcessamentoTelemetria> registrarLeitura(
            @PathVariable Long id,
            @RequestBody @Valid DadosPacoteSensores dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarPacote(id, dados));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<Page<DadosDetalhamentoLeitura>> listarLeituras(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.listarLeituras(id, pageable));
    }
}
