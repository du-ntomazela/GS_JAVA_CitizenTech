package br.com.fiap3espv.SemaforoInteligente.domain.cruzamento;

public enum ModoOperacao {
    /** Ciclo fixo tradicional (modo de seguranca quando o cruzamento perde a conexao). */
    CICLO_FIXO,
    /** Tempo de verde calculado a partir das filas medidas pelos sensores. */
    ADAPTATIVO,
    /** Plano definido manualmente pela operadora da central de transito. */
    MANUAL,
    /** Onda verde ativa para uma ambulancia. */
    EMERGENCIA
}
