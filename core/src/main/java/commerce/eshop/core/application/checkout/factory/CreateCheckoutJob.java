package commerce.eshop.core.application.checkout.factory;

import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.web.dto.requests.checkout.DTOCheckoutRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateCheckoutJob {

    // == Public Methods ==
    public CheckoutJob create(UUID customerId, UUID idemKey, DTOCheckoutRequest request){
        return new CheckoutJob(idemKey, customerId, request.address(), request.payment());
    }
}
