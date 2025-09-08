package commerce.eshop.core.service.async.external.impl;

import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.service.async.external.PaymentProviderClient;
import commerce.eshop.core.web.dto.response.Providers.Charging.ProviderChargeResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class PaymentProviderClientImpl implements PaymentProviderClient {

    ///  A basic simulation to a charging method.
    ///  While we will need the id from the db for the customer in case we would have a real simulator for charging,
    ///  currently there is no reason to call the db to fetch the token or tokenize the new card as we will draw the outcome
    ///  via the idemKey only. In case we add a new module to simulate the 3rd party client we can utilize it to "validate" the payment through that.

    /// idemKey=abc-123 → always SUCCESSFUL
    ///
    /// idemKey=abc-123:FAIL → always FAILED (decline simulated)
    ///
    /// idemKey=abc-123:RANDOM → randomly SUCCESSFUL or FAILED (decided once, cached, so repeated calls for the same key return the same outcome)

    // == Fields ==
    private final Map<String, ProviderChargeResult> outcomes = new ConcurrentHashMap<>();


    // == Public Methods ==
    @Override
    public ProviderChargeResult chargeWithSavedMethod(Transaction transaction, UUID customerPaymentMethodId) {
        return outcomes.computeIfAbsent(transaction.getIdempotencyKey(), this::compute);
    }

    @Override
    public ProviderChargeResult tokenizeAndCharge(Transaction transaction, String Band, String panMasked, Integer expMonth, Integer expYear, String holderName) {
        return outcomes.computeIfAbsent(transaction.getIdempotencyKey(), this::compute);
    }

    // == Private Methods ==

    private ProviderChargeResult compute(String idemKey){

        // Simple simulation rules based on the IdemKey.

        // Failed outcome
        if (idemKey.endsWith(":FAIL")){
            return new ProviderChargeResult("FAKE-" + idemKey, false, "DECLINED", "Simulated decline");
        }

        // Randomized outcome
        if (idemKey.endsWith(":RANDOM")){
            boolean ok = ThreadLocalRandom.current().nextBoolean();
            return ok
                    ? new ProviderChargeResult("FAKE-" + idemKey, true , null , null)
                    : new ProviderChargeResult("FAKE-" + idemKey, false, "RANDOM_FAIL", "Randomized Failure");
        }

        // Default success
        return new ProviderChargeResult("FAKE-" + idemKey, true, null, null);
    }
}
