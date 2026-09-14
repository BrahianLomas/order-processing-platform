package org.example.shared.exception;

public class PaymentException extends RuntimeException {

    private String errorCode;

    public PaymentException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
    }

    public PaymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
