package br.com.fiap3espv.SemaforoInteligente.domain.ambulancia;

public class AmbulanciaNotFoundException extends RuntimeException {
    public AmbulanciaNotFoundException(String message) {
        super(message);
    }
}
