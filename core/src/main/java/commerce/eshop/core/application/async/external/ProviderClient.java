package commerce.eshop.core.application.async.external;

import commerce.eshop.core.model.entity.CustomerPaymentMethod;

public interface ProviderClient {

    String fetchPaymentToken(String provider, CustomerPaymentMethod paymentMethod);
}
