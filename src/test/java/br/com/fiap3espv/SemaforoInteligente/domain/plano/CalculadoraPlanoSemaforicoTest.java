package br.com.fiap3espv.SemaforoInteligente.domain.plano;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosLeituraVia;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.Via;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraPlanoSemaforicoTest {
    private final CalculadoraPlanoSemaforico calculadora = new CalculadoraPlanoSemaforico(120, 15, 3);
    private final Cruzamento cruzamento = new Cruzamento();

    @Test
    @DisplayName("Eixo com fila maior recebe mais tempo de verde e o ciclo total é respeitado")
    void eixoMaisCarregadoRecebeMaisVerde() {
        List<DadosLeituraVia> leituras = List.of(
                new DadosLeituraVia(Via.NORTE, 30, 20),
                new DadosLeituraVia(Via.SUL, 25, 18),
                new DadosLeituraVia(Via.LESTE, 5, 4),
                new DadosLeituraVia(Via.OESTE, 3, 2));

        PlanoSemaforico plano = calculadora.calcular(cruzamento, leituras);

        assertThat(plano.getVerdeNorteSul()).isGreaterThan(plano.getVerdeLesteOeste());
        assertThat(plano.getDuracaoCiclo()).isEqualTo(120);
        assertThat(plano.getOrigem()).isEqualTo(OrigemPlano.ALGORITMO);
    }

    @Test
    @DisplayName("Sem nenhum carro o verde é dividido igualmente")
    void semDemandaDivideIgualmente() {
        List<DadosLeituraVia> leituras = List.of(
                new DadosLeituraVia(Via.NORTE, 0, 0),
                new DadosLeituraVia(Via.LESTE, 0, 0));

        PlanoSemaforico plano = calculadora.calcular(cruzamento, leituras);

        assertThat(plano.getVerdeNorteSul()).isEqualTo(plano.getVerdeLesteOeste());
    }

    @Test
    @DisplayName("Eixo vazio nunca fica abaixo do verde mínimo (pedestres e conversões)")
    void respeitaVerdeMinimo() {
        List<DadosLeituraVia> leituras = List.of(
                new DadosLeituraVia(Via.NORTE, 80, 40),
                new DadosLeituraVia(Via.LESTE, 0, 0));

        PlanoSemaforico plano = calculadora.calcular(cruzamento, leituras);

        assertThat(plano.getVerdeLesteOeste()).isEqualTo(15);
        assertThat(plano.getVerdeNorteSul()).isEqualTo(120 - 6 - 15);
    }

    @Test
    @DisplayName("Ciclo fixo divide o tempo igualmente entre as fases")
    void cicloFixo() {
        PlanoSemaforico plano = calculadora.cicloFixo(cruzamento);

        assertThat(plano.getVerdeNorteSul()).isEqualTo(57);
        assertThat(plano.getVerdeLesteOeste()).isEqualTo(57);
        assertThat(plano.getOrigem()).isEqualTo(OrigemPlano.CICLO_FIXO);
    }
}
