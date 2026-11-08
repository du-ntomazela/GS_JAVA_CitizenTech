package br.com.fiap3espv.SemaforoInteligente.service;

import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.Cruzamento;
import br.com.fiap3espv.SemaforoInteligente.domain.cruzamento.CruzamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Complementa o Last Will do MQTT: se um cruzamento ficar sem enviar dados alem do limite,
 * ele e marcado como OFFLINE (o dispositivo, sozinho, volta ao ciclo fixo).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorConexaoCruzamentos {
    private final CruzamentoRepository repository;

    @Value("${semaforo.offline.timeout-segundos:60}")
    private long timeoutSegundos;

    @Scheduled(fixedDelayString = "${semaforo.offline.verificacao-ms:15000}")
    @Transactional
    public void verificarCruzamentosSemComunicacao() {
        List<Cruzamento> semComunicacao = repository.findOnlineSemComunicacaoDesde(LocalDateTime.now().minusSeconds(timeoutSegundos));
        semComunicacao.forEach(cruzamento -> {
            cruzamento.marcarOffline();
            log.warn("ALERTA: cruzamento {} sem comunicação há mais de {} s, marcado como OFFLINE", cruzamento.getCodigo(), timeoutSegundos);
        });
    }
}
