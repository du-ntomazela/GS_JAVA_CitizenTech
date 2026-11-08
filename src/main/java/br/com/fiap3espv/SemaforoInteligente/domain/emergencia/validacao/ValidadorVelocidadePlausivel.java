package br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao;

import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.Emergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Geolocalizacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ValidadorVelocidadePlausivel implements ValidadorPosicaoEmergencia {
    /** Margem para o ruido do GPS do celular. */
    private static final double MARGEM_GPS_METROS = 50;

    private final double velocidadeMaximaKmh;

    public ValidadorVelocidadePlausivel(@Value("${emergencia.velocidade-maxima-kmh:180}") double velocidadeMaximaKmh) {
        this.velocidadeMaximaKmh = velocidadeMaximaKmh;
    }

    @Override
    public void validar(Emergencia emergencia, Coordenada posicao, LocalDateTime dataHora) {
        if (emergencia.getUltimaPosicao() == null || emergencia.getUltimaAtualizacao() == null) {
            return;
        }

        double distancia = Geolocalizacao.distanciaMetros(emergencia.getUltimaPosicao(), posicao);
        double segundos = Math.max(1, Duration.between(emergencia.getUltimaAtualizacao(), dataHora).toMillis() / 1000.0);
        double maximoPermitido = velocidadeMaximaKmh / 3.6 * segundos + MARGEM_GPS_METROS;

        if (distancia > maximoPermitido) {
            throw new ValidacaoException("Deslocamento incompatível com a velocidade de um veículo (possível GPS falsificado)!");
        }
    }
}
