package br.com.fiap3espv.SemaforoInteligente.domain.plano;

/** Porta de saida para os cruzamentos. Implementada via MQTT (ou log, quando o broker esta desligado). */
public interface PublicadorComandos {
    void publicar(ComandoSemaforo comando);
}
