package commerse.eshop.core.service.async.external;

import commerse.eshop.core.model.entity.Transaction;
import commerse.eshop.core.web.dto.response.Providers.Charging.ProviderChargeResult;

import java.util.UUID;

public interface PaymentProviderClient {

    ProviderChargeResult chargeWithSavedMethod(Transaction transaction, UUID customerPaymentMethodId);

    ProviderChargeResult tokenizeAndCharge(Transaction transaction, String Band, String panMasked, Integer expMonth, Integer expYear,
                                           String holderName);
}
