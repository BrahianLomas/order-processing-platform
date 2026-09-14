package org.example.order_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.shared.response.GenericResponse;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CarSaleListResponse extends GenericResponse {

    private List<CarSaleResponse> sales;

    public CarSaleListResponse(List<CarSaleResponse> sales) {
        this.sales = sales;
    }
}
