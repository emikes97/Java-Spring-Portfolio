package commerce.eshop.core.application.checkout.commands;

import commerce.eshop.core.application.checkout.factory.CreateCheckoutJob;
import commerce.eshop.core.application.checkout.writer.CheckoutWriter;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.web.dto.requests.checkout.DTOCheckoutRequest;
import commerce.eshop.core.web.dto.response.Checkout.DTOCheckoutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class ProcessCheckout {

    // == Fields ==
    private final CheckoutWriter writer;
    private final CreateCheckoutJob createJob;

    // == Constructors ==
    @Autowired
    public ProcessCheckout(CheckoutWriter writer, CreateCheckoutJob createJob) {
        this.writer = writer;
        this.createJob = createJob;
    }

    // == Public Methods ==

    @Transactional
    public DTOCheckoutResponse process(UUID customerId, UUID idemKey, DTOCheckoutRequest request){

        CheckoutJob job = createJob.create(customerId, idemKey, request);
        job = writer.save(job);

        return new DTOCheckoutResponse(job.getOrderId());
    }
}
