package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Eixo;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Geolocalizacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Seleciona os cruzamentos que ficam no corredor entre a origem e o destino da ambulancia,
 * na ordem em que serao atravessados. Simplificacao da demo: o trajeto e tratado como um
 * corredor em linha reta; em producao a rota viria de um servico de roteamento (OSM/Google).
 */
@Component
public class CalculadoraRota {
    private final double larguraCorredorMetros;

    public CalculadoraRota(@Value("${emergencia.largura-corredor-metros:150}") double larguraCorredorMetros) {
        this.larguraCorredorMetros = larguraCorredorMetros;
    }

    public List<CruzamentoNaRota> calcular(Coordenada origem, Coordenada destino, List<Cruzamento> candidatos) {
        Eixo eixo = Geolocalizacao.eixoDeDeslocamento(origem, destino);

        return candidatos.stream()
                .map(c -> new CruzamentoNaRota(c, Geolocalizacao.projetar(origem, destino, c.getLocalizacao()), eixo))
                .filter(c -> c.projecao().fracao() >= 0 && c.projecao().fracao() <= 1)
                .filter(c -> c.projecao().desvio() <= larguraCorredorMetros)
                .sorted(Comparator.comparingDouble(c -> c.projecao().distanciaNaRota()))
                .toList();
    }

    public record CruzamentoNaRota(Cruzamento cruzamento, Geolocalizacao.ProjecaoNaRota projecao, Eixo eixo) {
    }
}
