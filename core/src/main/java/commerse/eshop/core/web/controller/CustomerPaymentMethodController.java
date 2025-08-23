package commerse.eshop.core.web.controller;

import commerse.eshop.core.service.CustomerPaymentMethodService;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerse.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/payment-methods")
public class CustomerPaymentMethodController {

    private final CustomerPaymentMethodService customerPaymentMethodService;

    @Autowired
    public CustomerPaymentMethodController(CustomerPaymentMethodService customerPaymentMethodService){
        this.customerPaymentMethodService = customerPaymentMethodService;
    }

    // Get all payment methods
    /// curl -i -X GET "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/payment-methods?page=0&size=10&sort=createdAt,desc" \
    //  -H "Content-Type: application/json"
    @GetMapping
    public Page<DTOPaymentMethodResponse> getAllPaymentMethods(@PathVariable UUID customerId, Pageable pageable){
        return customerPaymentMethodService.getAllPaymentMethods(customerId, pageable);
    }

    // Add new payment method
    ///
    @PostMapping
    public DTOPaymentMethodResponse addPaymentMethod(@PathVariable UUID customerId, @RequestBody @Valid DTOAddPaymentMethod dto){
        return customerPaymentMethodService.addPaymentMethod(customerId, dto);
    }

}
