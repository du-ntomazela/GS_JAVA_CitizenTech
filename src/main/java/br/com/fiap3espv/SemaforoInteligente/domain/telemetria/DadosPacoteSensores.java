package br.com.fiap3espv.SemaforoInteligente.domain.telemetria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pacote resumido enviado pelo dispositivo de borda a cada poucos segundos
 * (topico MQTT cidade/{cruzamento}/sensores). Apenas numeros: o video nunca sai do cruzamento.
 */
public record DadosPacoteSensores(
        LocalDateTime timestamp,

        @NotEmpty
        @Size(max = 4)
        @Valid
        List<DadosLeituraVia> vias) {
}
