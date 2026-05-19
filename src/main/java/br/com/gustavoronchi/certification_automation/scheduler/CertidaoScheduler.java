package br.com.gustavoronchi.certification_automation.scheduler;

import br.com.gustavoronchi.certification_automation.services.CertidaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// agenda a execução diária do download de certidões
// horário configurável em application.properties
// certidao.scheduler.cron=0 0 6 * * *   (padrão: todo dia às 06:00)
// para desabilitar: certidao.scheduler.enabled=false
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "certidao.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class CertidaoScheduler {

    private final CertidaoService certidaoService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Scheduled(cron = "${certidao.scheduler.cron:0 0 6 * * *}")
    public void executarJobDiario() {
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║  JOB DIÁRIO CERTIDÕES — {}", LocalDateTime.now().format(FMT));
        log.info("╚══════════════════════════════════════════════╝");

        try {
            certidaoService.processarTodos();
        } catch (Exception e) {
            log.error("Erro crítico no job de certidões: {}", e.getMessage(), e);
        }
    }
}
