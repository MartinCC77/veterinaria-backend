package cl.duoc.veterinaria.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Datos generales de Swagger y el botón "Authorize" para pegar el token JWT
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Veterinaria San Marcos",
        version = "1.0",
        description = "Usuarios, servicios y citas de la clínica. Los endpoints protegidos exigen un token JWT."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}