package br.com.gustavoronchi.certification_automation.entities;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "registro_certidao")
public class RegistroCertidao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // cnpj da empresa
    @Column(nullable = false, length = 14)
    private String cnpj;

    // nome do portal (ex: RECEITA_FEDERAL, FGTS)
    @Column(nullable = false)
    private String portal;

    // tipo da certidão (ex: CND_FEDERAL, CRF_FGTS, CND_MUNICIPAL)
    @Column(nullable = false)
    private String tipoCertidao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCertidao status;

    // caminho completo do arquivo salvo em disco
    private String caminhoArquivo;

    // data e hora em que o download foi executado
    @Column(nullable = false)
    private LocalDateTime dataDownload;

    // data de validade da certidão (quando disponível no documento)
    private LocalDate dataValidade;

    // mensagem de erro, caso o download tenha falhado
    @Column(length = 1000)
    private String mensagemErro;

    public RegistroCertidao() {
    }

    public RegistroCertidao(Long id, String cnpj, String portal, String tipoCertidao, StatusCertidao status, String caminhoArquivo, LocalDateTime dataDownload, LocalDate dataValidade, String mensagemErro) {
        this.id = id;
        this.cnpj = cnpj;
        this.portal = portal;
        this.tipoCertidao = tipoCertidao;
        this.status = status;
        this.caminhoArquivo = caminhoArquivo;
        this.dataDownload = dataDownload;
        this.dataValidade = dataValidade;
        this.mensagemErro = mensagemErro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getPortal() {
        return portal;
    }

    public void setPortal(String portal) {
        this.portal = portal;
    }

    public String getTipoCertidao() {
        return tipoCertidao;
    }

    public void setTipoCertidao(String tipoCertidao) {
        this.tipoCertidao = tipoCertidao;
    }

    public StatusCertidao getStatus() {
        return status;
    }

    public void setStatus(StatusCertidao status) {
        this.status = status;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public LocalDateTime getDataDownload() {
        return dataDownload;
    }

    public void setDataDownload(LocalDateTime dataDownload) {
        this.dataDownload = dataDownload;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    public enum StatusCertidao {
        SUCESSO,
        FALHA,
        PENDENTE,

        // empresa com débitos — certidão positiva / negativa com efeito de positiva
        POSITIVA
    }
}
