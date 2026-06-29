package br.com.gustavoronchi.certification_automation.services;

import br.com.gustavoronchi.certification_automation.entities.RegistroCertidao;
import br.com.gustavoronchi.certification_automation.repositories.RegistroCertidaoRepository;
import br.com.gustavoronchi.certification_automation.strategy.CertidaoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertidaoService {

    private final List<CertidaoStrategy> strategies;

    private final RegistroCertidaoRepository repository;

    @Value("${certidao.empresas.destino}")
    private String pastaBase;

    @Value("${certidao.empresas.cnpjs:}")
    private String cnpjsConfig;

    public List<RegistroCertidao> processarTodos() {
        List<String> cnpjs = parseCnpjs(cnpjsConfig);

        if (cnpjs.isEmpty()) {
            log.warn("Nenhum CNPJ configurado em 'certidao.empresas.cnpjs'");
            return List.of();
        }

        log.info("=== Iniciando processamento: {} CNPJ(s) × {} portal(is) ===",
                cnpjs.size(), strategies.size());

        List<RegistroCertidao> todosResultados = new ArrayList<>();

        for (String cnpj : cnpjs) {
            List<RegistroCertidao> resultadosCnpj = processarCnpj(cnpj);
            todosResultados.addAll(resultadosCnpj);
        }

        long sucessos = todosResultados.stream()
                .filter(r -> r.getStatus() == RegistroCertidao.StatusCertidao.SUCESSO).count();
        long falhas = todosResultados.stream()
                .filter(r -> r.getStatus() == RegistroCertidao.StatusCertidao.FALHA).count();

        log.info("=== Processamento concluído: {} sucesso(s), {} falha(s) ===", sucessos, falhas);

        return todosResultados;
    }

    // processa um único CNPJ em todos os portais
    public List<RegistroCertidao> processarCnpj(String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        List<RegistroCertidao> resultados = new ArrayList<>();

        log.info("--- Processando CNPJ: {} ---", cnpjLimpo);

        for (CertidaoStrategy strategy : strategies) {
            log.info("Portal: {}", strategy.getPortalNome());
            try {
                List<RegistroCertidao> registros = strategy.baixarCertidoes(cnpjLimpo, pastaBase);
                repository.saveAll(registros);
                resultados.addAll(registros);
            } catch (Exception e) {
                log.error("Erro inesperado no portal {}: {}", strategy.getPortalId(), e.getMessage(), e);
            }
        }

        return resultados;
    }

    // processa um CNPJ em um portal específico
    public List<RegistroCertidao> processarCnpjPortal(String cnpj, String portalId) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");

        return strategies.stream()
                .filter(s -> s.getPortalId().equalsIgnoreCase(portalId))
                .findFirst()
                .map(strategy -> {
                    List<RegistroCertidao> registros = strategy.baixarCertidoes(cnpjLimpo, pastaBase);
                    repository.saveAll(registros);
                    return registros;
                })
                .orElseThrow(() -> new IllegalArgumentException("Portal não encontrado: " + portalId));
    }

    private List<String> parseCnpjs(String cnpjsConfig) {
        if (cnpjsConfig == null || cnpjsConfig.isBlank()) return List.of();
        return Stream.of(cnpjsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
