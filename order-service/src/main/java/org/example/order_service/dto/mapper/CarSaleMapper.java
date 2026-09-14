package org.example.order_service.dto.mapper;


import org.example.order_service.dto.CarSaleResponse;
import org.example.order_service.dto.CreateCarSaleRequest;
import org.example.order_service.entity.CarSale;
import org.example.order_service.entity.Customer;
import org.example.shared.dto.CarSaleCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarSaleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "tax", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "discount", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CarSale toEntity(CreateCarSaleRequest request, Customer customer);

    @Mapping(target = "customerName", expression = "java(carSale.getCustomer().getFirstName() + \" \" + carSale.getCustomer().getLastName())")
    @Mapping(target = "customerId", source = "customer.id")
    CarSaleResponse toResponse(CarSale carSale);

    @Mapping(target = "saleId", source = "id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "salePrice", source = "totalAmount")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    CarSaleCreatedEvent toEvent(CarSale carSale);
}