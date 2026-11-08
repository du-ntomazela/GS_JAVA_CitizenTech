package br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao;

import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.DadosAberturaEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Geolocalizacao;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDistanciaMinimaDestino implements ValidadorAberturaEmergencia {
    private static final double DISTANCIA_MINIMA_METROS = 100;

    @Override
    public void validar(DadosAberturaEmergencia dados) {
        double distancia = Geolocalizacao.distanciaMetros(new Coordenada(dados.origem()), new Coordenada(dados.destino()));

        if (distancia < DISTANCIA_MINIMA_METROS) {
            throw new ValidacaoException("Origem e destino precisam estar a pelo menos " + (int) DISTANCIA_MINIMA_METROS + " m de distância!");
        }
    }
}
