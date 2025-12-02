package commerce.eshop.core.application.async.external.impl.payment.token;

import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.application.async.external.contracts.ProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Slf4j
@Component("providerClientImpl")
public class ProviderClientImpl implements ProviderClient {

    private final Function<String, String> tokenGenerator =
            provider -> "tok_" + provider.toLowerCase() + "_" + UUID.randomUUID();

    @Override
    public String fetchPaymentToken(String provider, CustomerPaymentMethod paymentMethod) {

        try{
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000,3000));
        } catch (InterruptedException ignored){
        }

        String token = tokenGenerator.apply(provider);
        log.info("Generated token for provider={} paymentId={}", provider, paymentMethod.getCustomerPaymentId());
        return token;
    }
}
