package br.com.gustavoronchi.certification_automation.strategy.impl;

import br.com.gustavoronchi.certification_automation.entities.RegistroCertidao;
import br.com.gustavoronchi.certification_automation.services.SeleniumHelper;
import br.com.gustavoronchi.certification_automation.strategy.AbstractCertidaoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MunicipalStrategy extends AbstractCertidaoStrategy {

    private static final String URL =
            "http://tributario.vitoria.es.gov.br/Servicos/CertidaoNegativa/CertidaoNegativa.aspx";

    public MunicipalStrategy(SeleniumHelper seleniumHelper) {
        super(seleniumHelper);
    }

    @Override
    public String getPortalId() {
        return "MUNICIPAL_VITORIA";
    }

    @Override
    public String getPortalNome() {
        return "Prefeitura de Vitória - Certidão Negativa";
    }

    @Override
    public List<RegistroCertidao> baixarCertidoes(String cnpj, String destino) {

        List<RegistroCertidao> resultados = new ArrayList<>();

        WebDriver driver = null;

        try {

            String pasta = criarPasta(
                    destino,
                    cnpj,
                    getPortalId()
            );

            driver = seleniumHelper.criarDriver(pasta);

            log.info("[{}] Consultando CNPJ {}",
                    getPortalId(),
                    cnpj);

            driver.get(URL);

            // ---------------------------------------------------
            // SELECIONA CNPJ
            // ---------------------------------------------------

            seleniumHelper.aguardar(2);

            seleniumHelper.clicarQuandoPronto(
                    driver,
                    By.id("ctl00_conteudo_rblTipoDocumento_1")
            );

            seleniumHelper.aguardar(2);

            log.info(
                    "Radio selecionado = {}",
                    driver.findElement(
                            By.id("ctl00_conteudo_rblTipoDocumento_1")
                    ).isSelected()
            );

            // ---------------------------------------------------
            // PREENCHE CNPJ
            // ---------------------------------------------------

            By campoLocator = By.id("ctl00_conteudo_txtTermoBusca");

            seleniumHelper.wait(driver).until(
                    ExpectedConditions.presenceOfElementLocated(campoLocator)
            );

            WebElement campo = driver.findElement(campoLocator);

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value='';",
                    campo
            );

            campo.click();

            campo.sendKeys(Keys.CONTROL + "a");
            campo.sendKeys(Keys.DELETE);

            campo.sendKeys(cnpj);

            log.info("Valor digitado: {}",
                    campo.getAttribute("value"));

            log.info("Valor preenchido: {}",
                    driver.findElement(campoLocator).getAttribute("value"));

            log.info(
                    "Valor final do campo = [{}]",
                    driver.findElement(
                            By.id("ctl00_conteudo_txtTermoBusca")
                    ).getAttribute("value")
            );

            // ---------------------------------------------------
            // CONTINUAR
            // ---------------------------------------------------

            log.info("Enviando formulário...");

            WebElement btnContinuar = driver.findElement(
                    By.id("ctl00_conteudo_btnEnviar")
            );

            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", btnContinuar);


            seleniumHelper.aguardar(2);

            log.info("URL atual: {}", driver.getCurrentUrl());

            log.info("HTML contém Emitir? {}",
                    driver.getPageSource().contains("btnEmitir"));

            try {

                Alert alert = seleniumHelper.wait(driver)
                        .until(ExpectedConditions.alertIsPresent());

                log.error("ALERTA: {}", alert.getText());

                alert.accept();

                return resultados;

            } catch (TimeoutException ignored) {
            }

            // ---------------------------------------------------
            // AGUARDA BOTÃO EMITIR
            // ---------------------------------------------------

            log.info("Aguardando botão emitir...");

            seleniumHelper.aguardarVisivel(
                    driver,
                    By.id("ctl00_conteudo_btnEmitir")
            );

            // ---------------------------------------------------
            // EMITIR
            // ---------------------------------------------------

            WebElement btnEmitir = driver.findElement(
                    By.id("ctl00_conteudo_btnEmitir")
            );

            String onclick = btnEmitir.getAttribute("onclick");

            log.info("ONCLICK => {}", onclick);

            ((JavascriptExecutor) driver)
                    .executeScript(onclick);

            seleniumHelper.aguardar(3);

            log.info("Quantidade de abas: {}",
                    driver.getWindowHandles().size());

            String urlRelativa = onclick
                    .split("'")[1];

            String urlPdf =
                    "https://tributario.vitoria.es.gov.br/Servicos/CertidaoNegativa/"
                            + urlRelativa;

            log.info("URL PDF = {}", urlPdf);

            driver.get(urlPdf);

            log.info(driver.getPageSource());
            log.info("URL atual: {}", driver.getCurrentUrl());
            log.info("Título: {}", driver.getTitle());

            // ---------------------------------------------------
            // LOGS DE DIAGNÓSTICO
            // ---------------------------------------------------

            log.info("URL PDF: {}", driver.getCurrentUrl());

            log.info("Título PDF: {}", driver.getTitle());

            log.info("Quantidade de cookies: {}",
                    driver.manage().getCookies().size());

            log.info("Pasta de download: {}", pasta);

            driver.manage()
                    .getCookies()
                    .forEach(cookie ->
                            log.info("COOKIE => {}={}",
                                    cookie.getName(),
                                    cookie.getValue())
                    );

            seleniumHelper.aguardar(5);

            String arquivo = renomearPdfBaixado(
                    pasta,
                    "CND_MUNICIPAL_" + cnpj + ".pdf"
            );

            resultados.add(
                    construirRegistro(
                            cnpj,
                            getPortalId(),
                            "CND_MUNICIPAL",
                            RegistroCertidao.StatusCertidao.SUCESSO,
                            arquivo,
                            null
                    )
            );

        } catch (Exception e) {

            log.error("[{}] Erro ao consultar CNPJ {}",
                    getPortalId(),
                    cnpj,
                    e);

            resultados.add(
                    construirRegistro(
                            cnpj,
                            getPortalId(),
                            "CND_MUNICIPAL",
                            RegistroCertidao.StatusCertidao.FALHA,
                            null,
                            e.getMessage()
                    )
            );

        } finally {

            seleniumHelper.fecharDriver(driver);

        }

        return resultados;
    }
}