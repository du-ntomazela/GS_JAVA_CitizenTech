package br.com.fiap3espv.SemaforoInteligente.domain.emergencia;

public class EmergenciaNotFoundException extends RuntimeException {
    public EmergenciaNotFoundException(String message) {
        super(message);
    }
}
