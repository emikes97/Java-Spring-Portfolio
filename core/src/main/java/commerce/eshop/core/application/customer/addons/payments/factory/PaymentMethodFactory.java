package commerce.eshop.core.application.customer.addons.payments.factory;

import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.util.enums.TokenStatus;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodFactory {

    public CustomerPaymentMethod create(DTOAddPaymentMethod dto, Customer customer, boolean makeDefault){
        var pm = new CustomerPaymentMethod(
                customer,
                dto.provider(),
                dto.brand(),
                dto.last4(),
                dto.yearExp(),
                dto.monthExp(),
                makeDefault
        );
        pm.setTokenStatus(TokenStatus.PENDING); // Set status to pending until 3rd party provides the token
        pm.setProviderPaymentMethodToken(null); // Set the token to null
        return pm;
    }
}
