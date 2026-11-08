package br.com.fiap3espv.SemaforoInteligente.service;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.*;
import br.com.fiap3espv.SemaforoInteligente.domain.emergencia.ValidacaoException;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanoSemaforicoService {
    private final CruzamentoRepository cruzamentoRepository;
    private final PlanoSemaforicoRepository repository;
    private final CalculadoraPlanoSemaforico calculadora;
    private final PublicadorComandos publicador;

    @Transactional(readOnly = true)
    public DadosDetalhamentoPlano planoAtual(Long idCruzamento) {
        buscarCruzamento(idCruzamento);
        return repository.findFirstByCruzamentoIdOrderByDataHoraDescIdDesc(idCruzamento)
                .map(DadosDetalhamentoPlano::new)
                .orElseThrow(() -> new ValidacaoException("Cruzamento ainda não recebeu nenhum plano!"));
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoPlano> historico(Long idCruzamento, Pageable pageable) {
        buscarCruzamento(idCruzamento);
        return repository.findAllByCruzamentoIdOrderByDataHoraDescIdDesc(idCruzamento, pageable)
                .map(DadosDetalhamentoPlano::new);
    }

    /** Intervencao manual da operadora: fixa os tempos ate que o modo seja alterado de novo. */
    @Transactional
    public DadosDetalhamentoPlano aplicarPlanoManual(Long idCruzamento, DadosPlanoManual dados) {
        Cruzamento cruzamento = buscarCruzamento(idCruzamento);
        validarForaDeEmergencia(cruzamento);

        cruzamento.alterarModo(ModoOperacao.MANUAL);
        PlanoSemaforico plano = calculadora.manual(cruzamento, dados);
        repository.save(plano);
        publicador.publicar(ComandoSemaforo.plano(plano, ModoOperacao.MANUAL));
        return new DadosDetalhamentoPlano(plano);
    }

    @Transactional
    public DadosDetalhamentoCruzamento alterarModo(Long idCruzamento, DadosAlteracaoModo dados) {
        Cruzamento cruzamento = buscarCruzamento(idCruzamento);
        if (dados.modo() == ModoOperacao.EMERGENCIA) {
            throw new ValidacaoException("O modo EMERGENCIA só é ativado pela central de emergências!");
        }
        if (dados.modo() == ModoOperacao.MANUAL) {
            throw new ValidacaoException("Para o modo MANUAL envie os tempos em POST /cruzamento/{id}/plano!");
        }
        validarForaDeEmergencia(cruzamento);

        cruzamento.alterarModo(dados.modo());
        if (dados.modo() == ModoOperacao.CICLO_FIXO) {
            PlanoSemaforico plano = calculadora.cicloFixo(cruzamento);
            repository.save(plano);
            publicador.publicar(ComandoSemaforo.plano(plano, ModoOperacao.CICLO_FIXO));
        }
        return new DadosDetalhamentoCruzamento(cruzamento);
    }

    private void validarForaDeEmergencia(Cruzamento cruzamento) {
        if (cruzamento.getModoOperacao() == ModoOperacao.EMERGENCIA) {
            throw new ValidacaoException("Cruzamento está com onda verde ativa para uma ambulância!");
        }
    }

    private Cruzamento buscarCruzamento(Long id) {
        return cruzamentoRepository.findById(id)
                .filter(Cruzamento::isAtivo)
                .orElseThrow(() -> new CruzamentoNotFoundException("ID do cruzamento informado não existe!"));
    }
}
