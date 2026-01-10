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
    public DTOCheckoutResponse checkout(@PathVariable UUID customerId, @RequestHeader("Idempotency-Key") UUID idemkey, @Valid @RequestBody DTOCheckoutRequest request){
        return checkoutService.process(customerId, idemkey, request);
    }
}
