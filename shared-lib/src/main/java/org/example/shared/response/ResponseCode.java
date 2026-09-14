package org.example.shared.response;

import lombok.Getter;

/**
 * Catálogo único de códigos de respuesta, compartido por los servicios con endpoints REST
 * (api-gateway, order-service, payment-service) para homologar {@link GenericResponse}.
 */
@Getter
public enum ResponseCode {

    SUCCESS("00", "Successful"),

    INTERNAL_ERROR("E001", "Internal Error"),
    BUSINESS_ERROR("E002", "Business rule violation"),
    PAYMENT_ERROR("E003", "Payment processing error"),
    UNAUTHORIZED("E004", "Unauthorized"),
    VALIDATION_ERROR("E005", "Invalid input"),
    NOT_FOUND("E006", "Resource not found"),
    DUPLICATE_RESOURCE("E007", "Resource already exists"),
    EXTERNAL_SERVICE_ERROR("E008", "External service error");

    private final String code;
    private final String description;

    ResponseCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
