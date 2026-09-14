package org.example.order_service.repository;

import org.example.order_service.entity.CarSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarSaleRepository extends JpaRepository<CarSale, Long> {
    List<CarSale> findByCustomerId(Long customerId);
}
