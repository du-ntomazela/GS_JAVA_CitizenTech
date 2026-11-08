package br.com.fiap3espv.SemaforoInteligente.infra.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                "bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"))
                .info(new Info()
                        .title("Semáforo Inteligente")
                        .description("""
                                API REST do backend central do Semáforo Inteligente (Global Solution FIAP 2026.2 - CitizenTech).
                                Recebe a telemetria dos cruzamentos via MQTT, calcula o tempo de verde de cada fase,
                                permite a intervenção manual da central de trânsito e orquestra a onda verde para ambulâncias.
                                Faça login em POST /login e use o token no botão Authorize.""")
                        .contact(new Contact()
                                .name("Equipe Semáforo Inteligente")
                                .email("semaforointeligente@email.com.br")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")
                        )
                );
    }
}
