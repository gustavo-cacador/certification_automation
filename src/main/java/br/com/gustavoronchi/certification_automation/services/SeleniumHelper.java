package br.com.gustavoronchi.certification_automation.services;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SeleniumHelper {

    private static final Logger log = LoggerFactory.getLogger(SeleniumHelper.class);

    @Value("${certidao.selenium.headless:true}")
    private boolean headless;

    @Value("${certidao.selenium.timeout-seconds:30}")
    private int timeoutSeconds;

    // cria um WebDriver configurado para download automático na pasta informada
    public WebDriver criarDriver(String pastaDownload) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        // configurações para evitar detecção de bot e melhorar estabilidade
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-blink-features=AutomationControlled",
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        );

        // configura pasta de download automático
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", pastaDownload);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("plugins.always_open_pdf_externally", true); // força download de PDFs
        options.setExperimentalOption("prefs", prefs);

        log.info("Iniciando ChromeDriver | headless={} | pasta={}", headless, pastaDownload);
        return new ChromeDriver(options);
    }

    public WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    // aguarda elemento ficar clicável e clica
    public void clicarQuandoPronto(WebDriver driver, By locator) {
        wait(driver).until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // aguarda elemento ficar visível e retorna ele
    public WebElement aguardarVisivel(WebDriver driver, By locator) {
        return wait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // fecha o driver com segurança
    public void fecharDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.warn("Erro ao fechar WebDriver: {}", e.getMessage());
            }
        }
    }

    // aguarda N segundos (use com moderação — prefira waits explícitos)
    public void aguardar(long segundos) {
        try {
            Thread.sleep(Duration.ofSeconds(segundos).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
