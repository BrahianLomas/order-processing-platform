package org.example.order_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.order_service.dto.CarSaleListResponse;
import org.example.order_service.dto.CarSaleResponse;
import org.example.order_service.dto.CreateCarSaleRequest;
import org.example.order_service.service.CarSaleService;
import org.example.shared.response.GenericResponse;
import org.example.shared.response.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
@Validated
public class CarSaleController {

    private final CarSaleService carSaleService;

    @PostMapping
    public ResponseEntity<GenericResponse> createCarSale(@RequestBody @Valid CreateCarSaleRequest request) {
        try {
            CarSaleResponse response = carSaleService.createCarSale(request);
            response.markSuccess();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GenericResponse.error(ResponseCode.BUSINESS_ERROR, e.getMessage()));
        }
    }

    @GetMapping("/{saleId}")
    public ResponseEntity<GenericResponse> getCarSale(@PathVariable @Positive(message = "saleId must be a positive number") Long saleId) {
        try {
            CarSaleResponse response = carSaleService.getCarSaleById(saleId);
            response.markSuccess();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.error(ResponseCode.NOT_FOUND, e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<GenericResponse> getCustomerSales(@PathVariable @Positive(message = "customerId must be a positive number") Long customerId) {
        try {
            List<CarSaleResponse> sales = carSaleService.getCarSalesByCustomer(customerId);
            CarSaleListResponse response = new CarSaleListResponse(sales);
            response.markSuccess();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GenericResponse.error(ResponseCode.BUSINESS_ERROR, e.getMessage()));
        }
    }

    @DeleteMapping("/{saleId}")
    public ResponseEntity<GenericResponse> cancelCarSale(@PathVariable @Positive(message = "saleId must be a positive number") Long saleId) {
        try {
            CarSaleResponse response = carSaleService.cancelCarSale(saleId);
            response.markSuccess();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(GenericResponse.error(ResponseCode.BUSINESS_ERROR, e.getMessage()));
        }
    }
}
