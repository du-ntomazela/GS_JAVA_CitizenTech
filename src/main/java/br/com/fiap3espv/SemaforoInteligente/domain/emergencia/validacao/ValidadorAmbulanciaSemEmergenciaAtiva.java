package br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao;

import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.DadosAberturaEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.EmergenciaRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.StatusEmergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidadorAmbulanciaSemEmergenciaAtiva implements ValidadorAberturaEmergencia {
    private final EmergenciaRepository repository;

    @Override
    public void validar(DadosAberturaEmergencia dados) {
        if (repository.existsByAmbulanciaIdAndStatus(dados.idAmbulancia(), StatusEmergencia.ATIVA)) {
            throw new ValidacaoException("Ambulância já possui uma emergência ativa!");
        }
    }
}
