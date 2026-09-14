package org.example.order_service.service;

import org.example.order_service.dto.CarSaleResponse;
import org.example.order_service.dto.CreateCarSaleRequest;

import java.util.List;

public interface CarSaleService {
    CarSaleResponse createCarSale(CreateCarSaleRequest request);
    CarSaleResponse getCarSaleById(Long saleId);
    List<CarSaleResponse> getCarSalesByCustomer(Long customerId);
    CarSaleResponse cancelCarSale(Long saleId);
}
