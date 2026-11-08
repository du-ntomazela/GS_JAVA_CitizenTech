package br.com.fiap3espv.SemaforoInteligente.service;

import br.com.fiap3espv.SemaforoInteligente.domain.ambulancia.*;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AmbulanciaService {
    private final AmbulanciaRepository repository;

    @Transactional
    public DadosDetalhamentoAmbulancia cadastrarAmbulancia(DadosCadastroAmbulancia dados) {
        if (repository.existsByCodigoOrPlaca(dados.codigo(), dados.placa())) {
            throw new ValidacaoException("Já existe uma ambulância com o código ou a placa informados!");
        }
        Ambulancia ambulancia = new Ambulancia(dados);
        repository.save(ambulancia);
        return new DadosDetalhamentoAmbulancia(ambulancia);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemAmbulancias> listarAmbulancias(Pageable pageable) {
        return repository.findAllByAtivoTrueOrderByCodigoAsc(pageable).map(DadosListagemAmbulancias::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoAmbulancia detalharAmbulancia(Long id) {
        return new DadosDetalhamentoAmbulancia(buscar(id));
    }

    @Transactional
    public DadosDetalhamentoAmbulancia atualizarAmbulancia(DadosAtualizacaoAmbulancia dados) {
        Ambulancia ambulancia = buscar(dados.id());
        ambulancia.atualizarInformacoes(dados);
        return new DadosDetalhamentoAmbulancia(ambulancia);
    }

    @Transactional
    public void excluirAmbulancia(Long id) {
        buscar(id).excluir();
    }

    private Ambulancia buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AmbulanciaNotFoundException("ID da ambulância informada não existe!"));
    }
}
