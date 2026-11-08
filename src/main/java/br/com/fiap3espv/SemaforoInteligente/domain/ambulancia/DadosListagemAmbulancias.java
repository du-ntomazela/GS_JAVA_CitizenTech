package br.com.fiap3espv.SemaforoInteligente.domain.ambulancia;

public record DadosListagemAmbulancias(
        Long id,
        String codigo,
        String placa,
        String base) {
    public DadosListagemAmbulancias(Ambulancia ambulancia) {
        this(
                ambulancia.getId(),
                ambulancia.getCodigo(),
                ambulancia.getPlaca(),
                ambulancia.getBase()
        );
    }
}
