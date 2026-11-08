package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosAberturaEmergencia(
        @NotNull
        @JsonAlias("id_ambulancia")
        Long idAmbulancia,

        @NotNull
        @Valid
        DadosCoordenada origem,

        @NotNull
        @Valid
        DadosCoordenada destino,

        @NotBlank
        @JsonAlias("descricao_destino")
        String descricaoDestino) {
}
