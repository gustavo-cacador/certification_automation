package br.com.gustavoronchi.certification_automation.controller;

import br.com.gustavoronchi.certification_automation.entities.RegistroCertidao;
import br.com.gustavoronchi.certification_automation.repositories.RegistroCertidaoRepository;
import br.com.gustavoronchi.certification_automation.services.CertidaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/certidoes")
@RequiredArgsConstructor
public class CertidaoController {

    private final CertidaoService certidaoService;
    private final RegistroCertidaoRepository repository;

    // executa o processamento completo agora (equivale ao job diário)
    @PostMapping("/executar")
    public ResponseEntity<Map<String, Object>> executarAgora() {
        List<RegistroCertidao> resultados = certidaoService.processarTodos();
        return ResponseEntity.ok(Map.of(
                "processados", resultados.size(),
                "sucessos", resultados.stream()
                        .filter(r -> r.getStatus() == RegistroCertidao.StatusCertidao.SUCESSO).count(),
                "falhas", resultados.stream()
                        .filter(r -> r.getStatus() == RegistroCertidao.StatusCertidao.FALHA).count()
        ));
    }

    // processa apenas um CNPJ específico em todos os portais
    @PostMapping("/cnpj/{cnpj}")
    public ResponseEntity<List<RegistroCertidao>> processarCnpj(@PathVariable String cnpj) {
        return ResponseEntity.ok(certidaoService.processarCnpj(cnpj));
    }

    // processa um cnpj em um portal especifico
    @PostMapping("/cnpj/{cnpj}/{portal}")
    public ResponseEntity<List<RegistroCertidao>> processarCnpjPortal(
            @PathVariable String cnpj,
            @PathVariable String portal) {
        return ResponseEntity.ok(certidaoService.processarCnpjPortal(cnpj, portal));
    }

    // retorna o histórico de downloads de um cnpj
    @GetMapping("/historico/{cnpj}")
    public ResponseEntity<List<RegistroCertidao>> historico(@PathVariable String cnpj) {
        return ResponseEntity.ok(
                repository.findByCnpjOrderByDataDownloadDesc(cnpj.replaceAll("[^0-9]", ""))
        );
    }
}