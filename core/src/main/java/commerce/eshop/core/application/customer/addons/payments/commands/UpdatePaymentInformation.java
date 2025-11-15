package commerce.eshop.core.application.customer.addons.payments.commands;

import commerce.eshop.core.application.customer.addons.payments.writer.PaymentMethodWriter;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class UpdatePaymentInformation {

    // == Fields ==
    private final PaymentMethodWriter pmWriter;
    private final DomainLookupService domainLookupService;

    // == Constructors ==
    @Autowired
    public UpdatePaymentInformation(PaymentMethodWriter pmWriter, DomainLookupService domainLookupService) {
        this.pmWriter = pmWriter;
        this.domainLookupService = domainLookupService;
    }

    // == Public Methods ==
    @Transactional
    public CustomerPaymentMethod handle(UUID customerId, UUID paymentMethodId, DTOUpdatePaymentMethod dto){

        CustomerPaymentMethod paymentMethod = domainLookupService.getPaymentMethodOrThrow(customerId, paymentMethodId, EndpointsNameMethods.PM_UPDATE);

        // == Update fields ==
        if(dto.provider() != null && !dto.provider().isBlank())
            paymentMethod.setProvider(dto.provider());
        if(dto.brand() != null && !dto.brand().isBlank())
            paymentMethod.setBrand(dto.brand());
        if(dto.last4() != null && !dto.last4().isBlank())
            paymentMethod.setLast4(dto.last4());
        if(dto.yearExp() != null)
            paymentMethod.setYearExp(dto.yearExp());
        if(dto.monthExp() != null)
            paymentMethod.setMonthExp(dto.monthExp());

        if(dto.isDefault()){
            pmWriter.updateDefaultToFalse(customerId);
            paymentMethod.setDefault(true);
        } else {
            paymentMethod.setDefault(false);
        }

        paymentMethod = pmWriter.save(paymentMethod, EndpointsNameMethods.PM_UPDATE);
        return paymentMethod;
    }
}
