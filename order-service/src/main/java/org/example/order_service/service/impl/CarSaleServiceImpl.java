package org.example.order_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.order_service.dto.CarSaleResponse;
import org.example.order_service.dto.CreateCarSaleRequest;
import org.example.order_service.dto.mapper.CarSaleMapper;
import org.example.order_service.entity.CarSale;
import org.example.order_service.entity.Customer;
import org.example.order_service.kafka.CarSaleEventProducer;
import org.example.order_service.repository.CarSaleRepository;
import org.example.order_service.repository.CustomerRepository;
import org.example.order_service.service.CarSaleService;
import org.example.shared.constant.AppConstants;
import org.example.shared.dto.CarSaleCreatedEvent;
import org.example.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarSaleServiceImpl implements CarSaleService {

    private final CarSaleRepository carSaleRepository;
    private final CustomerRepository customerRepository;
    private final CarSaleEventProducer eventProducer;
    private final CarSaleMapper carSaleMapper;

    @Override
    public CarSaleResponse createCarSale(CreateCarSaleRequest request) {
        log.info("Recibido request: {}", request);
        try {
            // Validar cliente
            Customer customer = customerRepository.findByEmail(request.getCustomerEmail())
                    .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Cliente no encontrado"));

            // Calcular montos
            BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
            BigDecimal tax = request.getSalePrice()
                    .subtract(discount)
                    .multiply(new BigDecimal("0.08"));
            BigDecimal totalAmount = request.getSalePrice()
                    .subtract(discount)
                    .add(tax);

            // Mapear y guardar (UNA LÍNEA!)
            CarSale carSale = carSaleMapper.toEntity(request, customer);
            carSale.setDiscount(discount);
            carSale.setTax(tax);
            carSale.setTotalAmount(totalAmount);

            CarSale savedSale = carSaleRepository.save(carSale);
            log.info("Venta creada exitosamente: {}", savedSale.getId());

            // Publicar evento (UNA LÍNEA!)
            CarSaleCreatedEvent event = carSaleMapper.toEvent(savedSale);
            eventProducer.publishCarSaleCreated(event);

            return carSaleMapper.toResponse(savedSale);
        } catch (Exception e) {
            log.error("❌ Error creando venta: ", e);
            throw e;
        }
    }

    @Override
    public CarSaleResponse getCarSaleById(Long saleId) {
        CarSale carSale = carSaleRepository.findById(saleId)
                .orElseThrow(() -> new BusinessException("SALE_NOT_FOUND", "Venta no encontrada"));
        return carSaleMapper.toResponse(carSale);
    }

    @Override
    public List<CarSaleResponse> getCarSalesByCustomer(Long customerId) {
        return carSaleRepository.findByCustomerId(customerId)
                .stream()
                .map(carSaleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CarSaleResponse cancelCarSale(Long saleId) {
        CarSale carSale = carSaleRepository.findById(saleId)
                .orElseThrow(() -> new BusinessException("SALE_NOT_FOUND", "Venta no encontrada"));

        if (carSale.getStatus().equals(AppConstants.STATUS_PAID)) {
            throw new BusinessException("CANNOT_CANCEL", "No se puede cancelar venta pagada");
        }

        carSale.setStatus("CANCELLED");
        CarSale updated = carSaleRepository.save(carSale);

        return carSaleMapper.toResponse(updated);
    }
}