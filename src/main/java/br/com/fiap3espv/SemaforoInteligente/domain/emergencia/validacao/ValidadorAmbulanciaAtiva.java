package br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao;

import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.AmbulanciaRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.DadosAberturaEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidadorAmbulanciaAtiva implements ValidadorAberturaEmergencia {
    private final AmbulanciaRepository ambulanciaRepository;

    @Override
    public void validar(DadosAberturaEmergencia dados) {
        boolean ambulanciaAtiva = ambulanciaRepository.findAtivoById(dados.idAmbulancia());

        if (!ambulanciaAtiva) {
            throw new ValidacaoException("Emergência não pode ser aberta para ambulância inativa!");
        }
    }
}
