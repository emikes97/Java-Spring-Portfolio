package commerce.eshop.core.application.async.external.impl;

import commerce.eshop.core.application.async.external.EmailSender;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class MockEmailSender implements EmailSender {

    @Override
    public boolean sendEmail(String email, String subject, String bodyText) {
        // 1 in 6 fails → 5:1 success:failure
        return ThreadLocalRandom.current().nextInt(6) != 0;
    }
}
