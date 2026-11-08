package br.com.fiap3espv.SemaforoInteligente.domain.localizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeolocalizacaoTest {
    private final Coordenada consolacao = new Coordenada(-23.5558, -46.6620);
    private final Coordenada paraiso = new Coordenada(-23.5713, -46.6447);

    @Test
    @DisplayName("Distância entre Consolação e Paraíso na Av. Paulista deve ser de aproximadamente 2,5 km")
    void distanciaHaversine() {
        double distancia = Geolocalizacao.distanciaMetros(consolacao, paraiso);
        assertThat(distancia).isCloseTo(2480, within(60.0));
    }

    @Test
    @DisplayName("Distância de um ponto para ele mesmo é zero")
    void distanciaZero() {
        assertThat(Geolocalizacao.distanciaMetros(consolacao, consolacao)).isZero();
    }

    @Test
    @DisplayName("Ponto no meio do trajeto fica na metade da rota e sem desvio")
    void projecaoNoMeioDaRota() {
        Coordenada meio = new Coordenada((-23.5558 - 23.5713) / 2, (-46.6620 - 46.6447) / 2);

        Geolocalizacao.ProjecaoNaRota projecao = Geolocalizacao.projetar(consolacao, paraiso, meio);

        assertThat(projecao.fracao()).isCloseTo(0.5, within(0.01));
        assertThat(projecao.desvio()).isLessThan(5);
    }

    @Test
    @DisplayName("Ponto afastado do trajeto tem desvio grande")
    void projecaoForaDaRota() {
        Coordenada avBrasil = new Coordenada(-23.5703, -46.6725);

        Geolocalizacao.ProjecaoNaRota projecao = Geolocalizacao.projetar(consolacao, paraiso, avBrasil);

        assertThat(projecao.desvio()).isGreaterThan(1000);
    }

    @Test
    @DisplayName("Deslocamento predominante para o sul usa o eixo NORTE_SUL")
    void eixoNorteSul() {
        Coordenada sul = new Coordenada(-23.5658, -46.6625);
        assertThat(Geolocalizacao.eixoDeDeslocamento(consolacao, sul)).isEqualTo(Eixo.NORTE_SUL);
    }

    @Test
    @DisplayName("Deslocamento predominante para leste usa o eixo LESTE_OESTE")
    void eixoLesteOeste() {
        Coordenada leste = new Coordenada(-23.5560, -46.6500);
        assertThat(Geolocalizacao.eixoDeDeslocamento(consolacao, leste)).isEqualTo(Eixo.LESTE_OESTE);
    }
}
