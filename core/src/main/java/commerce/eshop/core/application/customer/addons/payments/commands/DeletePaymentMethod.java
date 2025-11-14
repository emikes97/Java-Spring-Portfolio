package commerce.eshop.core.application.customer.addons.payments.commands;

import commerce.eshop.core.application.customer.addons.payments.writer.PaymentMethodWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class DeletePaymentMethod {

    // == Fields ==
    private final PaymentMethodWriter pmWriter;

    // == Constructors ==
    @Autowired
    public DeletePaymentMethod(PaymentMethodWriter pmWriter) {
        this.pmWriter = pmWriter;
    }

    // == Public Methods ==
    @Transactional
    public long handle(UUID customerId, UUID paymentId){
        return pmWriter.delete(customerId, paymentId);
    }
}
