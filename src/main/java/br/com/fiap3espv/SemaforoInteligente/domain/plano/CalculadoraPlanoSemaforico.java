package br.com.fiap3espv.SemaforoInteligente.domain.plano;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Eixo;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosLeituraVia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Primeira versao do "cerebro": regra simples que divide o tempo de verde do ciclo
 * proporcionalmente a demanda (fila + fluxo) de cada eixo, respeitando um verde minimo.
 * Evolucao prevista: substituir por um agente de aprendizado por reforco treinado no SUMO.
 */
@Component
public class CalculadoraPlanoSemaforico {
    private final int duracaoCiclo;
    private final int verdeMinimo;
    private final int amarelo;

    public CalculadoraPlanoSemaforico(
            @Value("${semaforo.ciclo.duracao-segundos:120}") int duracaoCiclo,
            @Value("${semaforo.ciclo.verde-minimo-segundos:15}") int verdeMinimo,
            @Value("${semaforo.ciclo.amarelo-segundos:3}") int amarelo) {
        this.duracaoCiclo = duracaoCiclo;
        this.verdeMinimo = verdeMinimo;
        this.amarelo = amarelo;
    }

    public PlanoSemaforico calcular(Cruzamento cruzamento, List<DadosLeituraVia> leituras) {
        double demandaNorteSul = demanda(leituras, Eixo.NORTE_SUL);
        double demandaLesteOeste = demanda(leituras, Eixo.LESTE_OESTE);

        int verdeDisponivel = duracaoCiclo - 2 * amarelo;
        int verdeNorteSul;
        if (demandaNorteSul + demandaLesteOeste == 0) {
            verdeNorteSul = verdeDisponivel / 2;
        } else {
            verdeNorteSul = (int) Math.round(verdeDisponivel * demandaNorteSul / (demandaNorteSul + demandaLesteOeste));
        }
        verdeNorteSul = Math.max(verdeMinimo, Math.min(verdeDisponivel - verdeMinimo, verdeNorteSul));
        int verdeLesteOeste = verdeDisponivel - verdeNorteSul;

        String observacao = String.format("Demanda N-S %.1f x L-O %.1f", demandaNorteSul, demandaLesteOeste);
        return new PlanoSemaforico(cruzamento, verdeNorteSul, verdeLesteOeste, amarelo, OrigemPlano.ALGORITMO, observacao);
    }

    public PlanoSemaforico cicloFixo(Cruzamento cruzamento) {
        int verde = (duracaoCiclo - 2 * amarelo) / 2;
        return new PlanoSemaforico(cruzamento, verde, verde, amarelo, OrigemPlano.CICLO_FIXO, "Ciclo fixo tradicional");
    }

    public PlanoSemaforico manual(Cruzamento cruzamento, DadosPlanoManual dados) {
        return new PlanoSemaforico(cruzamento, dados.verdeNorteSul(), dados.verdeLesteOeste(), amarelo,
                OrigemPlano.MANUAL, dados.observacao());
    }

    public int getAmarelo() {
        return amarelo;
    }

    /** A fila pesa mais do que o fluxo: carros parados sao o que gera espera. */
    private double demanda(List<DadosLeituraVia> leituras, Eixo eixo) {
        return leituras.stream()
                .filter(l -> l.via().getEixo() == eixo)
                .mapToDouble(l -> l.fila() + 0.5 * l.fluxoPorMinuto())
                .sum();
    }
}
