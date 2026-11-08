package br.com.fiap3espv.SemaforoInteligente.service;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.*;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CruzamentoService {
    private final CruzamentoRepository repository;

    @Transactional
    public DadosDetalhamentoCruzamento cadastrarCruzamento(DadosCadastroCruzamento dados) {
        if (repository.existsByCodigo(dados.codigo())) {
            throw new ValidacaoException("Já existe um cruzamento com o código informado!");
        }
        if (dados.modoOperacao() == ModoOperacao.EMERGENCIA) {
            throw new ValidacaoException("O modo EMERGENCIA só é ativado pela central de emergências!");
        }
        Cruzamento cruzamento = new Cruzamento(dados);
        repository.save(cruzamento);
        return new DadosDetalhamentoCruzamento(cruzamento);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemCruzamentos> listarCruzamentos(StatusConexao status, Pageable pageable) {
        Page<Cruzamento> pagina = status == null
                ? repository.findAllByAtivoTrueOrderByCodigoAsc(pageable)
                : repository.findAllByAtivoTrueAndStatusConexaoOrderByCodigoAsc(status, pageable);
        return pagina.map(DadosListagemCruzamentos::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoCruzamento detalharCruzamento(Long id) {
        return new DadosDetalhamentoCruzamento(buscar(id));
    }

    @Transactional
    public DadosDetalhamentoCruzamento atualizarCruzamento(DadosAtualizacaoCruzamento dados) {
        Cruzamento cruzamento = buscar(dados.id());
        cruzamento.atualizarInformacoes(dados);
        return new DadosDetalhamentoCruzamento(cruzamento);
    }

    @Transactional
    public void excluirCruzamento(Long id) {
        buscar(id).excluir();
    }

    private Cruzamento buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CruzamentoNotFoundException("ID do cruzamento informado não existe!"));
    }
}
