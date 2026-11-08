package br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao;

import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.DadosAberturaEmergencia;

public interface ValidadorAberturaEmergencia {
    void validar(DadosAberturaEmergencia dados);
}
