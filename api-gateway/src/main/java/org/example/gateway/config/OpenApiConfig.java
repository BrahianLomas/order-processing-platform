package org.example.gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Gateway",
                version = "1.0",
                description = "Authentication service: register, login and refresh a JWT (HS256). "
                        + "All endpoints here are public — use POST /api/auth/login to get a token, "
                        + "then paste it into the Authorize button on order-service's or payment-service's Swagger UI."
        )
)
public class OpenApiConfig {
}
