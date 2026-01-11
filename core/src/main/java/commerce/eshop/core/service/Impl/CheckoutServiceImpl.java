package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.checkout.commands.ProcessCheckout;
import commerce.eshop.core.service.CheckoutService;
import commerce.eshop.core.web.dto.requests.checkout.DTOCheckoutRequest;
import commerce.eshop.core.web.dto.response.Checkout.DTOCheckoutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    // == Fields ==
    private final ProcessCheckout checkout;

    // == Constructors ==
    @Autowired
    public CheckoutServiceImpl(ProcessCheckout checkout) {
        this.checkout = checkout;
    }

    // == Public Methods ==

    @Override
    public DTOCheckoutResponse process(UUID customerId, UUID idemKey, DTOCheckoutRequest request) {
        int attempts = 0;
        while (true) {
            try {
                return checkout.process(customerId, idemKey, request);
            } catch (CannotSerializeTransactionException | DeadlockLoserDataAccessException ex) {
                if (++attempts >= 5) {
                    throw ex;
                }
                int backoffMs = ThreadLocalRandom.current().nextInt(100, 300);
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while retrying checkout placement", ie);
                }
            }
        }
    }
}
