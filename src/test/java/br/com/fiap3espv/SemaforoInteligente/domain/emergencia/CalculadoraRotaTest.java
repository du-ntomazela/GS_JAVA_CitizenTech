package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.DadosCadastroCruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.DadosCoordenada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CalculadoraRotaTest {
    private final CalculadoraRota calculadora = new CalculadoraRota(150);

    public static Cruzamento cruzamento(String codigo, double lat, double lon) {
        return new Cruzamento(new DadosCadastroCruzamento(codigo, codigo, new DadosCoordenada(lat, lon), null));
    }

    @Test
    @DisplayName("Seleciona apenas os cruzamentos do corredor, na ordem em que a ambulância passa")
    void selecionaCruzamentosDoCorredorEmOrdem() {
        Coordenada origem = new Coordenada(-23.5558, -46.6620);
        Coordenada destino = new Coordenada(-23.5713, -46.6447);

        Cruzamento brigadeiro = cruzamento("brigadeiro", -23.566960, -46.649544);
        Cruzamento augusta = cruzamento("augusta", -23.557970, -46.659578);
        Cruzamento avBrasil = cruzamento("av-brasil", -23.570300, -46.672500);
        Cruzamento campinas = cruzamento("campinas", -23.562775, -46.654215);

        List<CalculadoraRota.CruzamentoNaRota> rota = calculadora.calcular(origem, destino, List.of(brigadeiro, augusta, avBrasil, campinas));

        assertThat(rota).extracting(r -> r.cruzamento().getCodigo())
                .containsExactly("augusta", "campinas", "brigadeiro");
    }

    @Test
    @DisplayName("Cruzamento atrás da origem não entra na rota")
    void ignoraCruzamentoAntesDaOrigem() {
        Coordenada origem = new Coordenada(-23.5600, -46.6570);
        Coordenada destino = new Coordenada(-23.5713, -46.6447);
        Cruzamento consolacao = cruzamento("consolacao", -23.5558, -46.6620);

        assertThat(calculadora.calcular(origem, destino, List.of(consolacao))).isEmpty();
    }
}
