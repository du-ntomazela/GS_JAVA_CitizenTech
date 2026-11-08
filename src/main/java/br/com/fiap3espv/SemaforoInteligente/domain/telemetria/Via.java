package br.com.fiap3espv.SemaforoInteligente.domain.telemetria;

import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Eixo;

public enum Via {
    NORTE(Eixo.NORTE_SUL),
    SUL(Eixo.NORTE_SUL),
    LESTE(Eixo.LESTE_OESTE),
    OESTE(Eixo.LESTE_OESTE);

    private final Eixo eixo;

    Via(Eixo eixo) {
        this.eixo = eixo;
    }

    public Eixo getEixo() {
        return eixo;
    }
}
