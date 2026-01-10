package commerce.eshop.core.service;

import commerce.eshop.core.web.dto.requests.checkout.DTOCheckoutRequest;
import commerce.eshop.core.web.dto.response.Checkout.DTOCheckoutResponse;

import java.util.UUID;

public interface CheckoutService {
    DTOCheckoutResponse process(UUID customerId, UUID idemkey, DTOCheckoutRequest request);
}
