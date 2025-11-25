package commerce.eshop.core.application.async.external.impl.emails;

import commerce.eshop.core.application.async.external.contracts.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component("externalMockEmailSender")
public class ExternalMockEmailSender implements EmailSender {

    // == Fields ==
    private final RestTemplate restTemplate;
    private static final String SUCCESS_URL = "https://httpstat.us/200";
    private static final String FAILURE_URL = "https://httpstat.us/500";
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 300L;

    // == Constructors ==
    @Autowired
    public ExternalMockEmailSender(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // == Public Methods ==
    @Override
    public boolean sendEmail(String email, String subject, String bodyText) {

        int attempts;
        int status = -1;

        for (attempts = 1; attempts <= MAX_ATTEMPTS; attempts++){

            String url = ThreadLocalRandom.current().nextInt(6) == 0
                    ? FAILURE_URL
                    : SUCCESS_URL;

            try {

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                status = response.getStatusCode().value();

                if(response.getStatusCode().is2xxSuccessful()){
                    log.info("ExternalEmailSender: SENT email to {} | httpStatus={}", email, status);
                    return true;
                }


            } catch (RestClientException exception){
                // If we can't reach the host, divert to our fallback (internal mock)
                log.error("ExternalEmailSender: provider unreachable for {} | {}", email, exception.getMessage());
                throw new IllegalStateException("External email simulator unavailable", exception);
            }

            // give some backoff time before hitting again
            if(attempts < MAX_ATTEMPTS) {
                long sleep = INITIAL_BACKOFF_MS * attempts;
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    log.error("Backoff interrupted — aborting retries");
                    break;
                }
            }
        }

        log.warn("ExternalEmailSender: FAILED to send email to {} | httpStatus={}", email, status);
        return false;
    }
}
