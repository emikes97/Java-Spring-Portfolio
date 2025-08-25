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

    // Place order
    /// curl -i -X POST "http://localhost:8080/api/v1/customers/499008e1-13fa-4db8-983f-a6fc175f2445/checkout" ^
    ///   -H "Content-Type: application/json" ^
    ///   -d "{\"country\":\"GR\",\"street\":\"Akadimias 1\",\"city\":\"Athens\",\"postalCode\":\"10562\"}"
    @PostMapping
    public DTOOrderPlacedResponse placeOrder(@PathVariable UUID customerId, @RequestBody(required = false) DTOOrderCustomerAddress dto){
        return orderService.placeOrder(customerId, dto);
    }

}
