package br.com.gustavoronchi.certification_automation.strategy;

import br.com.gustavoronchi.certification_automation.entities.RegistroCertidao;

import java.util.List;

public interface CertidaoStrategy {

    // identificador único do portal (ex: "RECEITA_FEDERAL")
    String getPortalId();

    // nome legível do portal para logs e relatórios
    String getPortalNome();

    // executa o download de todas as certidões do portal para um CNPJ
    // @param cnpj - CNPJ sem formatação (apenas 14 dígitos)
    // @param destino - Pasta onde os arquivos devem ser salvos
    // @return lista de registros com resultado de cada certidão
    List<RegistroCertidao> baixarCertidoes(String cnpj, String destino);
}
