package com.engstrategy.alugai_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ArenaHub API")
                        .version("1.0.0")
                        .description("API para reserva de horários em quadras esportivas.")
                        .contact(new Contact()
                                .name("Suporte")
                                .email("rlimamendes085@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Ambiente de desenvolvimento")
//                        new Server().url("https://api.arenahub.com").description("Ambiente de produção")
                ));
    }
}