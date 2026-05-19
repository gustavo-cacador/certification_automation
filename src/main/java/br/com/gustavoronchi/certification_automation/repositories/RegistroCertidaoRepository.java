package br.com.gustavoronchi.certification_automation.repositories;

import br.com.gustavoronchi.certification_automation.entities.RegistroCertidao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroCertidaoRepository extends JpaRepository<RegistroCertidao, Long> {

    List<RegistroCertidao> findByCnpjOrderByDataDownloadDesc(String cnpj);

    List<RegistroCertidao> findByStatusAndDataDownloadAfter(
            RegistroCertidao.StatusCertidao status, LocalDateTime after);

    Optional<RegistroCertidao> findTopByCnpjAndPortalAndTipoCertidaoOrderByDataDownloadDesc(
            String cnpj, String portal, String tipoCertidao);

    List<RegistroCertidao> findByDataDownloadBetween(LocalDateTime inicio, LocalDateTime fim);
}
