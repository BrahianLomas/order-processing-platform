package org.example.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Clase base de la que heredan todas las respuestas REST del proyecto, para homologar
 * el formato de respuesta entre servicios. En vez de envolver el payload en un campo
 * genérico "data", cada respuesta concreta (p. ej. {@code LoginResponse}, {@code CarSaleResponse})
 * extiende esta clase y agrega sus propios campos junto a {@code responseCode}/{@code description}/{@code timestamp}.
 *
 * Para respuestas de error sin un payload específico, se usa directamente esta clase
 * vía {@link #error(ResponseCode, String)}.
 *
 * {@code @JsonInclude(NON_NULL)}: cuando una instancia de una subclase se usa como
 * elemento anidado (p. ej. dentro de una lista envuelta en otra GenericResponse) y
 * nunca se le llama {@link #markSuccess()}/{@link #markError}, estos 3 campos quedan
 * en null y se omiten del JSON en vez de aparecer como ruido.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse {

    @JsonProperty("response_code")
    protected String responseCode;

    @JsonProperty("description")
    protected String description;

    @JsonProperty("timestamp")
    protected LocalDateTime timestamp;

    public void markSuccess() {
        this.responseCode = ResponseCode.SUCCESS.getCode();
        this.description = ResponseCode.SUCCESS.getDescription();
        this.timestamp = LocalDateTime.now();
    }

    public void markError(ResponseCode code, String description) {
        this.responseCode = code.getCode();
        this.description = (description != null && !description.isBlank()) ? description : code.getDescription();
        this.timestamp = LocalDateTime.now();
    }

    public static GenericResponse success() {
        GenericResponse response = new GenericResponse();
        response.markSuccess();
        return response;
    }

    public static GenericResponse error(ResponseCode code, String description) {
        GenericResponse response = new GenericResponse();
        response.markError(code, description);
        return response;
    }

    public static GenericResponse error(ResponseCode code) {
        return error(code, null);
    }
}
