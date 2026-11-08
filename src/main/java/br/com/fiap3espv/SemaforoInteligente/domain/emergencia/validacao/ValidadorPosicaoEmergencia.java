package br.com.fiap3espv.SemaforoInteligente.domain.emergencia.validacao;

import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.Emergencia;
import br.com.fiap3espv.SemaforoInteligente.domain.localizacao.Coordenada;

import java.time.LocalDateTime;

/** Regras anti-fraude: o backend so abre semaforos se a ambulancia estiver realmente na rota declarada. */
public interface ValidadorPosicaoEmergencia {
    void validar(Emergencia emergencia, Coordenada posicao, LocalDateTime dataHora);
}
