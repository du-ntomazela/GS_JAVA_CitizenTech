package br.com.fiap3espv.SemaforoInteligente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SemaforoInteligenteApplication {

	public static void main(String[] args) {
		SpringApplication.run(SemaforoInteligenteApplication.class, args);
	}

}
