package commerce.eshop.core.web.controller;

import commerce.eshop.core.service.CheckoutService;
import commerce.eshop.core.web.dto.requests.checkout.DTOCheckoutRequest;
import commerce.eshop.core.web.dto.response.Checkout.DTOCheckoutResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart/{customerId}/checkout")
public class CheckoutController {

    // == Fields ==
    private final CheckoutService checkoutService;

    // == Constructors ==
    @Autowired
    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // == Public Methods ==

    @PostMapping()
    /// curl -X POST "http://localhost:8080/api/v1/cart/3fa85f64-5717-4562-b3fc-2c963f66afa6/checkout" \
    ///   -H "Content-Type: application/json" \
    ///   -H "Idempotency-Key: 9b2c1c7e-3c5f-4f92-9b21-9f8f9e7a1c42" \
    ///   -d '{
    ///     "payment": {
    ///       "type": "USE_SAVED_METHOD",
    ///       "customerPaymentMethodId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    ///     },
    ///     "shippingAddressId": "c7a6d9b4-2e5a-4a1f-9f2c-123456789abc"
    ///   }'
    public DTOCheckoutResponse checkout(@PathVariable UUID customerId, @RequestHeader("Idempotency-Key") UUID idemkey, @Valid @RequestBody DTOCheckoutRequest request){
        return checkoutService.process(customerId, idemkey, request);
    }
}
