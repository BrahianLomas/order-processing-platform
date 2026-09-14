package org.example.shared.constant;


public class AppConstants {

    // Kafka Topics
    public static final String TOPIC_CAR_SALE_CREATED = "car-sale-created";
    public static final String TOPIC_PAYMENT_PROCESSED = "payment-processed";

    // Status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    // Error Codes
    public static final String ERROR_SALE_NOT_FOUND = "SALE_NOT_FOUND";
    public static final String ERROR_PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String ERROR_INVALID_INPUT = "INVALID_INPUT";

    // Security
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
}
