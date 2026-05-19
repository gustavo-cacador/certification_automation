package br.com.gustavoronchi.certification_automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CertificationAutomationApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertificationAutomationApplication.class, args);
	}

}
