package com.ecoembes.configuracion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionOpenApi {

    @Bean
    public OpenAPI configurarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servidor Ecoembes - API REST (Prototipo 1)")
                        .version("1.0")
                        .description("API REST del Servidor Ecoembes para la gestión de contenedores, consulta de plantas de reciclaje y asignación de envases."));
    }
}
