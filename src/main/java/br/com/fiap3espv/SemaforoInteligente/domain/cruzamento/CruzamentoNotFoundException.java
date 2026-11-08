package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

public class CruzamentoNotFoundException extends RuntimeException {
    public CruzamentoNotFoundException(String message) {
        super(message);
    }
}
