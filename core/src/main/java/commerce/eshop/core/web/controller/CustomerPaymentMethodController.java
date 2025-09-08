package commerce.eshop.core.web.controller;

import commerce.eshop.core.service.CustomerPaymentMethodService;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import commerce.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    /// curl -i -X GET "http://localhost:8080/api/v1/customers/00000000-0000-0000-0000-000000000101/payment-methods?page=0&size=10&sort=createdAt,desc"
    ///  -H "Content-Type: application/json"
    @GetMapping
    public Page<DTOPaymentMethodResponse> getAllPaymentMethods(@PathVariable UUID customerId, Pageable pageable){
        return customerPaymentMethodService.getAllPaymentMethods(customerId, pageable);
    }

    // Add new payment method
    ///curl -i -X POST "http://localhost:8080/api/v1/customers/03b9d0fb-2a6c-4c9f-997b-3e0fed17465c/payment-methods" \
    //  -H "Content-Type: application/json" \
    //  -d '{"provider": "Viva", "brand": "VISA", "last4": "4242", "yearExp": 2028, "monthExp": 12, "isDefault": true }'
    @PostMapping
    public DTOPaymentMethodResponse addPaymentMethod(@PathVariable UUID customerId, @RequestBody @Valid DTOAddPaymentMethod dto){
        return customerPaymentMethodService.addPaymentMethod(customerId, dto);
    }

    // Update payment method
    ///  curl -i -X PUT "http://localhost:8080/api/v1/customers/03b9d0fb-2a6c-4c9f-997b-3e0fed17465c/payment-methods/44b83e5b-8907-4698-9589-dbba789d1778" \
    //  -H "Content-Type: application/json" \
    //  -d '{
    //    "brand": "MASTERCARD",
    //    "last4": "1111",
    //    "yearExp": 2030,
    //    "monthExp": 6,
    //    "isDefault": false
    //  }'
    @PutMapping("/{customerPaymentId}")
    public DTOPaymentMethodResponse updatePaymentMethod(@PathVariable UUID customerId, @PathVariable UUID customerPaymentId, @RequestBody @Valid DTOUpdatePaymentMethod dto){
        return customerPaymentMethodService.updatePaymentMethod(customerId, customerPaymentId, dto);
    }

    // Retrieve a single payment method
    ///curl -i -X GET "http://localhost:8080/api/v1/customers/03b9d0fb-2a6c-4c9f-997b-3e0fed17465c/payment-methods/44b83e5b-8907-4698-9589-dbba789d1778" \
    //  -H "Content-Type: application/json"
    @GetMapping("/{paymentId}")
    public DTOPaymentMethodResponse retrievePaymentMethod(@PathVariable UUID customerId, @PathVariable UUID paymentId){
        return customerPaymentMethodService.retrievePaymentMethod(customerId, paymentId);
    }

    // Delete a payment Method
    ///curl -i -X DELETE "http://localhost:8080/api/v1/customers/03b9d0fb-2a6c-4c9f-997b-3e0fed17465c/payment-methods/44b83e5b-8907-4698-9589-dbba789d1778"
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{paymentId}")
    public void deletePaymentMethod(@PathVariable UUID customerId, @PathVariable UUID paymentId){
        customerPaymentMethodService.deletePaymentMethod(customerId, paymentId);
    }
}
