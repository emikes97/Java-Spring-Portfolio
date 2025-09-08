package commerce.eshop.core.web.controller;

import commerce.eshop.core.service.OrderService;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/")
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
    @PostMapping("/checkout")
    public DTOOrderPlacedResponse placeOrder(@PathVariable UUID customerId, @RequestBody(required = false) DTOOrderCustomerAddress dto){
        return orderService.placeOrder(customerId, dto);
    }

    // Order Cancel
    ///http://localhost:8080/api/v1/customers/5305688f-2c42-4f7f-9514-ce46dc310ba4/orders/c2287bfe-67a1-417e-8cdf-04e4ca78ab0b
    @PostMapping("/orders/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void  cancelOrder(@PathVariable UUID customerId, @PathVariable UUID orderId){
        orderService.cancel(customerId, orderId);
    }

    // view order
    ///curl -i "http://localhost:8080/api/v1/customers/499008e1-13fa-4db8-983f-a6fc175f2445/orders/c2287bfe-67a1-417e-8cdf-04e4ca78ab0b"
    @GetMapping("/orders/{orderId}/view")
    public DTOOrderDetailsResponse viewOrder(@PathVariable UUID customerId, @PathVariable UUID orderId){
        return orderService.viewOrder(customerId, orderId);
    }

}
