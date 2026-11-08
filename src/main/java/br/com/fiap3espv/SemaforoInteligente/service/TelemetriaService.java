package br.com.fiap3espv.SemaforoInteligente.service;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.CruzamentoNotFoundException;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.CruzamentoRepository;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.ModoOperacao;
import br.com.fiap3espv.SemaforoInteligente.domain.plano.*;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosDetalhamentoLeitura;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.DadosPacoteSensores;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.LeituraSensor;
import br.com.fiap3espv.SemaforoInteligente.domain.telemetria.LeituraSensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Recebe os pacotes dos dispositivos de borda (via MQTT ou REST), grava o historico
 * e, se o cruzamento estiver em modo adaptativo, calcula e envia um novo plano.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetriaService {
    private final CruzamentoRepository cruzamentoRepository;
    private final LeituraSensorRepository leituraRepository;
    private final PlanoSemaforicoRepository planoRepository;
    private final CalculadoraPlanoSemaforico calculadora;
    private final PublicadorComandos publicador;

    @Transactional
    public DadosProcessamentoTelemetria registrarPacote(String codigoCruzamento, DadosPacoteSensores pacote) {
        Cruzamento cruzamento = cruzamentoRepository.findByCodigoAndAtivoTrue(codigoCruzamento)
                .orElseThrow(() -> new CruzamentoNotFoundException("Cruzamento " + codigoCruzamento + " não está cadastrado!"));
        return processar(cruzamento, pacote);
    }

    @Transactional
    public DadosProcessamentoTelemetria registrarPacote(Long idCruzamento, DadosPacoteSensores pacote) {
        Cruzamento cruzamento = cruzamentoRepository.findById(idCruzamento)
                .filter(Cruzamento::isAtivo)
                .orElseThrow(() -> new CruzamentoNotFoundException("ID do cruzamento informado não existe!"));
        return processar(cruzamento, pacote);
    }

    @Transactional
    public void atualizarStatus(String codigoCruzamento, String status) {
        cruzamentoRepository.findByCodigoAndAtivoTrue(codigoCruzamento).ifPresentOrElse(cruzamento -> {
            if ("online".equalsIgnoreCase(status)) {
                cruzamento.marcarOnline();
            } else {
                cruzamento.marcarOffline();
                log.warn("ALERTA: cruzamento {} ficou offline e voltou ao ciclo fixo local", codigoCruzamento);
            }
        }, () -> log.warn("Status recebido de cruzamento desconhecido: {}", codigoCruzamento));
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoLeitura> listarLeituras(Long idCruzamento, Pageable pageable) {
        if (!cruzamentoRepository.existsById(idCruzamento)) {
            throw new CruzamentoNotFoundException("ID do cruzamento informado não existe!");
        }
        return leituraRepository.findAllByCruzamentoIdOrderByDataHoraDesc(idCruzamento, pageable)
                .map(DadosDetalhamentoLeitura::new);
    }

    private DadosProcessamentoTelemetria processar(Cruzamento cruzamento, DadosPacoteSensores pacote) {
        LocalDateTime dataHora = pacote.timestamp() != null ? pacote.timestamp() : LocalDateTime.now();

        List<LeituraSensor> leituras = pacote.vias().stream()
                .map(via -> new LeituraSensor(cruzamento, via, dataHora))
                .toList();
        leituraRepository.saveAll(leituras);
        cruzamento.registrarComunicacao(LocalDateTime.now());

        DadosDetalhamentoPlano planoAplicado = null;
        if (cruzamento.getModoOperacao() == ModoOperacao.ADAPTATIVO) {
            PlanoSemaforico plano = calculadora.calcular(cruzamento, pacote.vias());
            planoRepository.save(plano);
            publicador.publicar(ComandoSemaforo.plano(plano, ModoOperacao.ADAPTATIVO));
            planoAplicado = new DadosDetalhamentoPlano(plano);
        }

        return new DadosProcessamentoTelemetria(cruzamento.getCodigo(), leituras.size(), planoAplicado);
    }
}
