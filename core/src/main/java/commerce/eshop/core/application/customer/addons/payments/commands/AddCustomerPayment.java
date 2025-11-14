package commerce.eshop.core.application.customer.addons.payments.commands;

import commerce.eshop.core.application.customer.addons.payments.factory.PaymentMethodFactory;
import commerce.eshop.core.application.customer.addons.payments.writer.PaymentMethodWriter;
import commerce.eshop.core.application.events.PaymentMethodCreatedEvent;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class AddCustomerPayment {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final PaymentMethodWriter pmWriter;
    private final PaymentMethodFactory pmFactory;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public AddCustomerPayment(DomainLookupService domainLookupService, PaymentMethodWriter pmWriter, PaymentMethodFactory pmFactory, ApplicationEventPublisher publisher) {
        this.domainLookupService = domainLookupService;
        this.pmWriter = pmWriter;
        this.pmFactory = pmFactory;
        this.publisher = publisher;
    }

    // == Public Methods ==
    @Transactional
    public CustomerPaymentMethod handle(UUID customerId, DTOAddPaymentMethod dto){

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.PM_ADD);
        boolean makeDefault = Boolean.TRUE.equals(dto.isDefault());

        if(makeDefault) // in case it's true, disable the current default method.
            pmWriter.updateDefaultToFalse(customerId);

        CustomerPaymentMethod paymentMethod = pmFactory.create(dto, customer, makeDefault);

        paymentMethod = pmWriter.save(paymentMethod, EndpointsNameMethods.PM_ADD);
        publisher.publishEvent(new PaymentMethodCreatedEvent(customerId, paymentMethod.getCustomerPaymentId(),
                paymentMethod.getProvider()));

        return paymentMethod;
    }
}
