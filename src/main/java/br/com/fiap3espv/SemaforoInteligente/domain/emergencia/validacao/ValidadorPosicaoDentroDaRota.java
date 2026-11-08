package br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao;

import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.Emergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Geolocalizacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValidadorPosicaoDentroDaRota implements ValidadorPosicaoEmergencia {
    private final double toleranciaMetros;

    public ValidadorPosicaoDentroDaRota(@Value("${emergencia.tolerancia-desvio-metros:500}") double toleranciaMetros) {
        this.toleranciaMetros = toleranciaMetros;
    }

    @Override
    public void validar(Emergencia emergencia, Coordenada posicao, LocalDateTime dataHora) {
        double desvio = Geolocalizacao.projetar(emergencia.getOrigem(), emergencia.getDestino(), posicao).desvio();

        if (desvio > toleranciaMetros) {
            throw new ValidacaoException("Posição informada está " + Math.round(desvio) + " m fora da rota declarada!");
        }
    }
}
