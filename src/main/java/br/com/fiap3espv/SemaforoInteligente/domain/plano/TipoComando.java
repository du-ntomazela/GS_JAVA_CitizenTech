package br.com.fiap3espv.SemaforoInteligente.domain.plano;

public enum TipoComando {
    /** Novo plano de tempos para o ciclo normal. */
    PLANO,
    /** Abre o verde no eixo da ambulancia (o eixo oposto passa pelo amarelo antes). */
    LIBERAR_EMERGENCIA,
    /** Ambulancia passou: o cruzamento volta ao ciclo normal. */
    RESTAURAR
}
