package commerse.eshop.core.web.controller;

import commerse.eshop.core.service.Impl.OrderServiceImpl;
import commerse.eshop.core.service.OrderService;
import commerse.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerse.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/checkout")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping
    public DTOOrderPlacedResponse placeOrder(@PathVariable UUID customerId, @RequestBody(required = false) DTOOrderCustomerAddress dto){
        return orderService.placeOrder(customerId, dto);
    }

}
