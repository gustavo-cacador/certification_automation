package br.com.gustavoronchi.certification_automation.strategy;

import br.com.gustavoronchi.certification_automation.entities.RegistroCertidao;
import br.com.gustavoronchi.certification_automation.services.SeleniumHelper;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.PrintsPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.print.PrintOptions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;

@RequiredArgsConstructor
public abstract class AbstractCertidaoStrategy
        implements CertidaoStrategy {

    protected final SeleniumHelper seleniumHelper;

    protected String criarPasta(String destino,
                                String cnpj,
                                String portal) {
        String pasta =
                destino
                        + File.separator
                        + cnpj
                        + File.separator
                        + portal;

        new File(pasta).mkdirs();
        return pasta;
    }

    protected String salvarPaginaComoPdf(WebDriver driver,
                                         String pasta,
                                         String nomeArquivo) throws Exception {
        Pdf pdf = ((PrintsPage) driver)
                .print(new PrintOptions());

        byte[] data = Base64
                .getDecoder()
                .decode(pdf.getContent());

        String caminho =
                pasta
                        + File.separator
                        + nomeArquivo;

        Files.write(Paths.get(caminho), data);
        return caminho;
    }

    protected RegistroCertidao construirRegistro(
            String cnpj,
            String portal,
            String tipoCertidao,
            RegistroCertidao.StatusCertidao status,
            String arquivo,
            String erro
    ) {

        return RegistroCertidao.builder()
                .cnpj(cnpj)
                .portal(portal)
                .tipoCertidao(tipoCertidao)
                .status(status)
                .caminhoArquivo(arquivo)
                .dataDownload(LocalDateTime.now())
                .mensagemErro(erro)
                .build();
    }
}