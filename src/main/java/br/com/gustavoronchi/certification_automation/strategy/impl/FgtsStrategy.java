package br.com.gustavoronchi.certification_automation.strategy.impl;

import br.com.gustavoronchi.certification_automation.entities.RegistroCertidao;
import br.com.gustavoronchi.certification_automation.services.SeleniumHelper;
import br.com.gustavoronchi.certification_automation.strategy.AbstractCertidaoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FgtsStrategy extends AbstractCertidaoStrategy {

    private static final String URL =
            "https://consulta-crf.caixa.gov.br/consultacrf/pages/consultaEmpregador.jsf";

    public FgtsStrategy(SeleniumHelper seleniumHelper) {
        super(seleniumHelper);
    }

    @Override
    public String getPortalId() {
        return "FGTS";
    }

    @Override
    public String getPortalNome() {
        return "FGTS / Caixa - CRF";
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

            // ---------------------------------------------------
            // INICIA DRIVER
            // ---------------------------------------------------

            driver = seleniumHelper.criarDriver(pasta);

            driver.get(URL);

            log.info("[{}] Consultando FGTS para CNPJ {}",
                    getPortalId(),
                    cnpj);

            // ---------------------------------------------------
            // PREENCHE CNPJ
            // ---------------------------------------------------
            log.info("Preenchendo CNPJ...");
            var campoCnpj = seleniumHelper.aguardarVisivel(
                    driver,
                    By.xpath("//input[contains(@id,'txtInscricao1')]")
            );

            campoCnpj.clear();
            campoCnpj.sendKeys(cnpj);

            // ---------------------------------------------------
            // CLICA CONSULTAR
            // ---------------------------------------------------

            log.info("Clicando em consultar...");
            seleniumHelper.clicarQuandoPronto(
                    driver,
                    By.xpath("//input[contains(@id,'btnConsultar')]")
            );

            // ---------------------------------------------------
            // AGUARDA RESULTADO
            // ---------------------------------------------------

            log.info("Aguardando resultado da consulta...");
            seleniumHelper.aguardarVisivel(
                    driver,
                    By.partialLinkText("Certificado de Regularidade")
            );

            // ---------------------------------------------------
            // VERIFICA IRREGULARIDADE
            // ---------------------------------------------------

            if (!possuiCrf(driver)) {

                log.warn("[{}] Nenhum CRF encontrado para o CNPJ {}",
                        getPortalId(),
                        cnpj);

                resultados.add(
                        construirRegistro(
                                cnpj,
                                getPortalId(),
                                "CRF_FGTS",
                                RegistroCertidao.StatusCertidao.POSITIVA,
                                null,
                                "CRF não disponível para o CNPJ informado"
                        )
                );

                return resultados;
            }

            // ---------------------------------------------------
            // CLICA LINK CRF
            // ---------------------------------------------------

            log.info("Abrindo CRF...");
            seleniumHelper.clicarQuandoPronto(
                    driver,
                    By.partialLinkText("Regularidade do FGTS")
            );

            // ---------------------------------------------------
            // AGUARDA PÁGINA CERTIFICADO
            // ---------------------------------------------------

            seleniumHelper.aguardarVisivel(
                    driver,
                    By.xpath("//*[contains(text(),'Certificado de Regularidade do FGTS')]")
            );

            // ---------------------------------------------------
            // CLICA VISUALIZAR
            // ---------------------------------------------------

            log.info("Abrindo visualização do certificado...");
            seleniumHelper.clicarQuandoPronto(
                    driver,
                    By.xpath("//input[contains(@id,'btnVisualizar')]")
            );

            // ---------------------------------------------------
            // AGUARDA PÁGINA FINAL
            // ---------------------------------------------------

            seleniumHelper.aguardarVisivel(
                    driver,
                    By.xpath("//*[contains(text(),'Certificado de Regularidade do FGTS - CRF')]")
            );

            log.info("Gerando PDF do certificado...");

            String arquivo = salvarPaginaComoPdf(
                    driver,
                    pasta,
                    "CRF_FGTS_" + cnpj + ".pdf"
            );

            resultados.add(
                    construirRegistro(
                            cnpj,
                            getPortalId(),
                            "CRF_FGTS",
                            RegistroCertidao.StatusCertidao.SUCESSO,
                            arquivo,
                            null
                    )
            );

            log.info("[{}] PDF salvo em {}",
                    getPortalId(),
                    arquivo);

        } catch (Exception e) {

            log.error("[{}] Erro ao consultar FGTS para CNPJ {}",
                    getPortalId(),
                    cnpj,
                    e);

            resultados.add(
                    construirRegistro(
                            cnpj,
                            getPortalId(),
                            "CRF_FGTS",
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

    private boolean possuiCrf(WebDriver driver) {
        return !driver.findElements(
                By.partialLinkText("Certificado de Regularidade")
        ).isEmpty();
    }
}