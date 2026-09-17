package org.example.order_service.service.impl;

import org.example.order_service.dto.CarSaleResponse;
import org.example.order_service.dto.CreateCarSaleRequest;
import org.example.order_service.dto.mapper.CarSaleMapperImpl;
import org.example.order_service.entity.CarSale;
import org.example.order_service.entity.Customer;
import org.example.order_service.kafka.CarSaleEventProducer;
import org.example.order_service.repository.CarSaleRepository;
import org.example.order_service.repository.CustomerRepository;
import org.example.shared.constant.AppConstants;
import org.example.shared.dto.CarSaleCreatedEvent;
import org.example.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CarSaleServiceImplTest {

    private CarSaleRepository carSaleRepository;
    private CustomerRepository customerRepository;
    private CarSaleEventProducer eventProducer;
    private CarSaleServiceImpl carSaleService;

    @BeforeEach
    void setUp() {
        carSaleRepository = Mockito.mock(CarSaleRepository.class);
        customerRepository = Mockito.mock(CustomerRepository.class);
        eventProducer = Mockito.mock(CarSaleEventProducer.class);
        // Real MapStruct-generated mapper (pure mapping logic, no need to mock it).
        carSaleService = new CarSaleServiceImpl(carSaleRepository, customerRepository, eventProducer, new CarSaleMapperImpl());
    }

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId(3L);
        customer.setFirstName("Juan");
        customer.setLastName("Perez");
        customer.setEmail("cliente@test.com");
        customer.setPhone("555-1234");
        customer.setAddress("Main St 1");
        customer.setCity("City");
        customer.setState("State");
        customer.setZipCode("00000");
        return customer;
    }

    private CreateCarSaleRequest request() {
        CreateCarSaleRequest request = new CreateCarSaleRequest();
        request.setCustomerEmail("cliente@test.com");
        request.setVehicleVin("1HGCM82633A004352");
        request.setVehicleMake("Toyota");
        request.setVehicleModel("Corolla");
        request.setVehicleYear(2023);
        request.setVehicleColor("Blanco");
        request.setVehicleMileage(0L);
        request.setSalePrice(new BigDecimal("25000"));
        request.setDiscount(new BigDecimal("500"));
        return request;
    }

    @Test
    void createCarSale_validRequest_computesPricingCorrectly() {
        when(customerRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(customer()));
        when(carSaleRepository.save(any(CarSale.class))).thenAnswer(invocation -> {
            CarSale saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        CarSaleResponse response = carSaleService.createCarSale(request());

        // tax = (25000 - 500) * 0.08 = 1960.00 ; totalAmount = 25000 - 500 + 1960 = 26460.00
        assertThat(response.getTax()).isEqualByComparingTo("1960.00");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("26460.00");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getCustomerName()).isEqualTo("Juan Perez");
    }

    @Test
    void createCarSale_publishesEventWithTotalAmountAsSalePrice() {
        when(customerRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(customer()));
        when(carSaleRepository.save(any(CarSale.class))).thenAnswer(invocation -> {
            CarSale saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        carSaleService.createCarSale(request());

        ArgumentCaptor<CarSaleCreatedEvent> eventCaptor = ArgumentCaptor.forClass(CarSaleCreatedEvent.class);
        verify(eventProducer).publishCarSaleCreated(eventCaptor.capture());
        // Documented quirk: the event's sale_price is actually the computed totalAmount, not the raw salePrice.
        assertThat(eventCaptor.getValue().getSalePrice()).isEqualByComparingTo("26460.00");
        assertThat(eventCaptor.getValue().getSaleId()).isEqualTo(10L);
    }

    @Test
    void createCarSale_customerNotFound_throwsBusinessException() {
        when(customerRepository.findByEmail("cliente@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carSaleService.createCarSale(request()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("CUSTOMER_NOT_FOUND"));

        verify(eventProducer, Mockito.never()).publishCarSaleCreated(any());
    }

    @Test
    void getCarSaleById_found_returnsResponse() {
        CarSale carSale = CarSale.builder()
                .id(10L).customer(customer()).status("PENDING")
                .vehicleVin("VIN").vehicleMake("Toyota").vehicleModel("Corolla").vehicleYear(2023)
                .vehicleColor("Blanco").vehicleMileage(0L)
                .salePrice(BigDecimal.TEN).discount(BigDecimal.ZERO).tax(BigDecimal.ZERO).totalAmount(BigDecimal.TEN)
                .build();
        when(carSaleRepository.findById(10L)).thenReturn(Optional.of(carSale));

        CarSaleResponse response = carSaleService.getCarSaleById(10L);

        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    void getCarSaleById_notFound_throwsBusinessException() {
        when(carSaleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carSaleService.getCarSaleById(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("SALE_NOT_FOUND"));
    }

    @Test
    void cancelCarSale_pendingSale_setsStatusCancelled() {
        CarSale carSale = CarSale.builder()
                .id(10L).customer(customer()).status(AppConstants.STATUS_PENDING)
                .vehicleVin("VIN").vehicleMake("Toyota").vehicleModel("Corolla").vehicleYear(2023)
                .vehicleColor("Blanco").vehicleMileage(0L)
                .salePrice(BigDecimal.TEN).discount(BigDecimal.ZERO).tax(BigDecimal.ZERO).totalAmount(BigDecimal.TEN)
                .build();
        when(carSaleRepository.findById(10L)).thenReturn(Optional.of(carSale));
        when(carSaleRepository.save(any(CarSale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CarSaleResponse response = carSaleService.cancelCarSale(10L);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelCarSale_alreadyPaid_throwsBusinessExceptionAndDoesNotSave() {
        CarSale carSale = CarSale.builder()
                .id(10L).customer(customer()).status(AppConstants.STATUS_PAID)
                .vehicleVin("VIN").vehicleMake("Toyota").vehicleModel("Corolla").vehicleYear(2023)
                .vehicleColor("Blanco").vehicleMileage(0L)
                .salePrice(BigDecimal.TEN).discount(BigDecimal.ZERO).tax(BigDecimal.ZERO).totalAmount(BigDecimal.TEN)
                .build();
        when(carSaleRepository.findById(10L)).thenReturn(Optional.of(carSale));

        assertThatThrownBy(() -> carSaleService.cancelCarSale(10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo("CANNOT_CANCEL"));

        verify(carSaleRepository, Mockito.never()).save(any());
    }

    @Test
    void getCarSalesByCustomer_returnsMappedList() {
        CarSale saleOne = CarSale.builder()
                .id(1L).customer(customer()).status("PENDING")
                .vehicleVin("VIN1").vehicleMake("Toyota").vehicleModel("Corolla").vehicleYear(2023)
                .vehicleColor("Blanco").vehicleMileage(0L)
                .salePrice(BigDecimal.TEN).discount(BigDecimal.ZERO).tax(BigDecimal.ZERO).totalAmount(BigDecimal.TEN)
                .build();
        CarSale saleTwo = CarSale.builder()
                .id(2L).customer(customer()).status("PAID")
                .vehicleVin("VIN2").vehicleMake("Honda").vehicleModel("Civic").vehicleYear(2022)
                .vehicleColor("Negro").vehicleMileage(1000L)
                .salePrice(BigDecimal.TEN).discount(BigDecimal.ZERO).tax(BigDecimal.ZERO).totalAmount(BigDecimal.TEN)
                .build();
        when(carSaleRepository.findByCustomerId(3L)).thenReturn(List.of(saleOne, saleTwo));

        List<CarSaleResponse> responses = carSaleService.getCarSalesByCustomer(3L);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CarSaleResponse::getId).containsExactly(1L, 2L);
    }
}
