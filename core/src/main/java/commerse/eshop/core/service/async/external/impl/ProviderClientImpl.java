package commerse.eshop.core.service.async.external.impl;

import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import commerse.eshop.core.service.async.external.ProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class ProviderClientImpl implements ProviderClient {

    @Override
    public String fetchPaymentToken(String provider, CustomerPaymentMethod paymentMethod) {

        try{
            Thread.sleep(ThreadLocalRandom.current().nextInt(200,800));
        } catch (InterruptedException ignored){}

        String token = "tok_" + provider.toLowerCase() + "_" + UUID.randomUUID();
        log.info("Generated token for provider={} paymentId={}", provider, paymentMethod.getCustomerPaymentId());

        return token;
    }
}
