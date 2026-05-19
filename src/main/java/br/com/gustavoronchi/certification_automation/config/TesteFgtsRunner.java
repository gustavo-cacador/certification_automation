package br.com.gustavoronchi.certification_automation.config;

import br.com.gustavoronchi.certification_automation.strategy.impl.FgtsStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TesteFgtsRunner implements CommandLineRunner {

    private final FgtsStrategy fgtsStrategy;

    @Value("${certidao.empresas.cnpjs}")
    private String cnpjs;

    @Value("${certidao.empresas.destino}")
    private String destino;

    @Override
    public void run(String... args) {

        fgtsStrategy.baixarCertidoes(
                cnpjs,
                destino
        );
    }
}