package commerce.eshop.core.application.async.external.impl.payment.token;

import commerce.eshop.core.application.async.external.ProviderClient;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component("uuidToolsClient")
public class UuidToolsTokenGeneration implements ProviderClient {

    // == Fields ==
    private static final String UUIDTOOLS_URL = "https://www.uuidtools.com/api/generate/v4";
    private static final int MAX_ATTEMPTS = 5;
    private static final long INITIAL_BACKOFF_MS = 300L;
    private final RestTemplate restTemplate;

    // == Constructors ==
    @Autowired
    public UuidToolsTokenGeneration(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // == Public Methods ==
    @Override
    public String fetchPaymentToken(String provider, CustomerPaymentMethod paymentMethod) {

        for(int attempts = 1; attempts <= MAX_ATTEMPTS; attempts++){
            try {

                ResponseEntity<String[]> response = restTemplate.getForEntity(UUIDTOOLS_URL, String[].class);

                if(response.getStatusCode().is2xxSuccessful()
                        && response.getBody() != null
                        && response.getBody().length > 0
                        && response.getBody()[0] != null){

                    String uuid = response.getBody()[0].trim();
                    String token = "tok_" + provider.toLowerCase() + "_" + uuid;

                    log.info("UUIDTools token generated on attempt {} | provider={} | paymentId={}",
                            attempts, provider, paymentMethod.getCustomerPaymentId());

                    return token;
                }

                log.warn("UUIDTools invalid response on attempt {} | provider={} | paymentId={} | body={}",
                        attempts, provider, paymentMethod.getCustomerPaymentId(), (Object) response.getBody());

            } catch (RestClientException exception){
                log.warn("UUIDTools request failed on attempt {} | provider={} | paymentId={} | {}",
                        attempts, provider, paymentMethod.getCustomerPaymentId(), exception.getMessage());
            }

            // give some backoff time before hitting again
            if(attempts < MAX_ATTEMPTS){
                long sleep = INITIAL_BACKOFF_MS * attempts;
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException exception){
                    Thread.currentThread().interrupt();
                    log.error("Backoff interrupted — aborting retries");
                    break;
                }
            }
        }

        // All attempts failed
        log.error("UUIDTools token generation FAILED after {} attempts | provider={} | paymentId={}",
                MAX_ATTEMPTS, provider, paymentMethod.getCustomerPaymentId());
        throw new IllegalStateException("External UUID token generation failed");
    }
}
