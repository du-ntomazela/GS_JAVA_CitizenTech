package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoCruzamento(
        @NotNull
        Long id,

        String nome,
        DadosCoordenada localizacao) {
}
