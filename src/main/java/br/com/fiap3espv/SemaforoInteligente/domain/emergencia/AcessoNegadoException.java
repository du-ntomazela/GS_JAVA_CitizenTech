package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String message) {
        super(message);
    }
}
