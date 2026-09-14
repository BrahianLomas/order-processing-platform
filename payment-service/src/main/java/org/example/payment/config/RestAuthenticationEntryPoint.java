package org.example.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.shared.response.GenericResponse;
import org.example.shared.response.ResponseCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Homologa con {@link GenericResponse} las respuestas 401 que Spring Security genera
 * en el filtro de seguridad (token ausente/inválido), antes de que la petición llegue
 * a un controller o a {@link GlobalExceptionHandler}.
 *
 * Usa su propio {@link ObjectMapper} (en vez de inyectar el bean de Spring) para no
 * depender de que la autoconfiguración de Jackson esté disponible en este punto.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        GenericResponse body = GenericResponse.error(ResponseCode.UNAUTHORIZED, "Token faltante, inválido o expirado");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
