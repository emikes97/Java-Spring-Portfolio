package commerce.eshop.core.application.async.internal.payment.tokenization;

import commerce.eshop.core.application.async.external.ProviderClient;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodTokenizationOrchestrator {

    // == Fields ==
    private final ProviderClient primaryClient;
    private final ProviderClient fallBackClient;

    // == Constructors ==
    @Autowired
    public PaymentMethodTokenizationOrchestrator(@Qualifier("uuidToolsClient")ProviderClient primaryClient,
                                                 @Qualifier("providerClientImpl")ProviderClient fallBackClient) {
        this.primaryClient = primaryClient;
        this.fallBackClient = fallBackClient;
    }

    // == Public Methods ==
    public String fetchPaymentToken(String provider, CustomerPaymentMethod paymentMethod){
        try {
            return primaryClient.fetchPaymentToken(provider, paymentMethod);
        } catch (IllegalStateException ex){
            return fallBackClient.fetchPaymentToken(provider, paymentMethod);
        }
    }
}
