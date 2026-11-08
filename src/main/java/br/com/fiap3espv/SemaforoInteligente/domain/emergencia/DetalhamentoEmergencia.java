package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record DetalhamentoEmergencia(
        Long id,
        String ambulancia,
        String socorrista,
        StatusEmergencia status,

        @JsonProperty("descricao_destino")
        String descricaoDestino,

        DadosCoordenada origem,
        DadosCoordenada destino,

        @JsonProperty("ultima_posicao")
        DadosCoordenada ultimaPosicao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        @JsonProperty("data_inicio")
        LocalDateTime dataInicio,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        @JsonProperty("data_fim")
        LocalDateTime dataFim,

        @JsonProperty("tempo_viagem_segundos")
        Long tempoViagemSegundos,

        @JsonProperty("semaforos_liberados")
        long semaforosLiberados,

        List<DadosCruzamentoRota> rota) {
    public DetalhamentoEmergencia(Emergencia emergencia) {
        this(
                emergencia.getId(),
                emergencia.getAmbulancia().getCodigo(),
                emergencia.getSocorrista(),
                emergencia.getStatus(),
                emergencia.getDescricaoDestino(),
                new DadosCoordenada(emergencia.getOrigem()),
                new DadosCoordenada(emergencia.getDestino()),
                emergencia.getUltimaPosicao() != null && emergencia.getUltimaPosicao().getLatitude() != null
                        ? new DadosCoordenada(emergencia.getUltimaPosicao()) : null,
                emergencia.getDataInicio(),
                emergencia.getDataFim(),
                emergencia.getDataFim() != null
                        ? Duration.between(emergencia.getDataInicio(), emergencia.getDataFim()).toSeconds() : null,
                emergencia.getSemaforosLiberados(),
                emergencia.getRota().stream().map(DadosCruzamentoRota::new).toList()
        );
    }
}
