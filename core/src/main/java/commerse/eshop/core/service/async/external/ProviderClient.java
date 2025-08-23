package commerse.eshop.core.service.async.external;

import commerse.eshop.core.model.entity.CustomerPaymentMethod;

public interface ProviderClient {

    String fetchPaymentToken(String provider, CustomerPaymentMethod paymentMethod);
}
