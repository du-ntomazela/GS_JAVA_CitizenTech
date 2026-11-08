package br.com.fiap3espv.SemaforoInteligente.infra.mqtt;

/** Topicos: cidade/{cruzamento}/sensores, cidade/{cruzamento}/comandos e cidade/{cruzamento}/status. */
public final class TopicosMqtt {
    public static final String TODOS_SENSORES = "cidade/+/sensores";
    public static final String TODOS_STATUS = "cidade/+/status";

    private TopicosMqtt() {
    }

    public static String comandos(String codigoCruzamento) {
        return "cidade/" + codigoCruzamento + "/comandos";
    }

    /** Extrai o codigo do cruzamento de um topico no formato cidade/{cruzamento}/{tipo}. */
    public static String codigoCruzamento(String topico) {
        String[] partes = topico.split("/");
        if (partes.length != 3 || !"cidade".equals(partes[0])) {
            throw new IllegalArgumentException("Tópico fora do padrão: " + topico);
        }
        return partes[1];
    }

    public static String tipo(String topico) {
        return topico.substring(topico.lastIndexOf('/') + 1);
    }
}
