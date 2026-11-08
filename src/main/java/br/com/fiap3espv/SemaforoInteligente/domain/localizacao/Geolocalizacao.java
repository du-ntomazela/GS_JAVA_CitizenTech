package br.com.fiap3espv.SemaforoInteligente.domain.localizacao;

/**
 * Calculos geograficos usados pela onda verde.
 * Para distancias urbanas (poucos km) projeta as coordenadas em um plano local (metros),
 * o que e preciso o suficiente e muito mais simples do que trabalhar na esfera.
 */
public final class Geolocalizacao {
    private static final double RAIO_TERRA_METROS = 6_371_000;

    private Geolocalizacao() {
    }

    /** Distancia em metros entre duas coordenadas (formula de Haversine). */
    public static double distanciaMetros(Coordenada a, Coordenada b) {
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());

        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * RAIO_TERRA_METROS * Math.asin(Math.sqrt(h));
    }

    /**
     * Projeta o ponto sobre o segmento origem -> destino.
     * Retorna a distancia percorrida ao longo do segmento (metros, limitada ao segmento)
     * e a distancia perpendicular do ponto ate o segmento.
     */
    public static ProjecaoNaRota projetar(Coordenada origem, Coordenada destino, Coordenada ponto) {
        double[] d = emMetros(origem, destino);
        double[] p = emMetros(origem, ponto);

        double comprimento2 = d[0] * d[0] + d[1] * d[1];
        double t = comprimento2 == 0 ? 0 : (p[0] * d[0] + p[1] * d[1]) / comprimento2;
        double tLimitado = Math.max(0, Math.min(1, t));

        double px = d[0] * tLimitado;
        double py = d[1] * tLimitado;
        double desvio = Math.hypot(p[0] - px, p[1] - py);
        double distanciaNaRota = Math.sqrt(comprimento2) * tLimitado;

        return new ProjecaoNaRota(distanciaNaRota, desvio, t);
    }

    /** Eixo de deslocamento predominante entre dois pontos. */
    public static Eixo eixoDeDeslocamento(Coordenada origem, Coordenada destino) {
        double[] d = emMetros(origem, destino);
        return Math.abs(d[1]) >= Math.abs(d[0]) ? Eixo.NORTE_SUL : Eixo.LESTE_OESTE;
    }

    /** Converte o ponto em metros (x = leste, y = norte) relativos a referencia. */
    private static double[] emMetros(Coordenada referencia, Coordenada ponto) {
        double latMedia = Math.toRadians((referencia.getLatitude() + ponto.getLatitude()) / 2);
        double x = Math.toRadians(ponto.getLongitude() - referencia.getLongitude()) * RAIO_TERRA_METROS * Math.cos(latMedia);
        double y = Math.toRadians(ponto.getLatitude() - referencia.getLatitude()) * RAIO_TERRA_METROS;
        return new double[]{x, y};
    }

    public record ProjecaoNaRota(double distanciaNaRota, double desvio, double fracao) {
    }
}
