package commerce.eshop.core.application.async.internal.email;

import commerce.eshop.core.application.async.external.contracts.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderOrchestrator {

    // == Fields ==
    private final EmailSender primaryClient;
    private final EmailSender fallbackClient;

    // == Constructors ==
    @Autowired
    public EmailSenderOrchestrator(@Qualifier("externalMockEmailSender") EmailSender primaryClient,
                                   @Qualifier("internalMockEmailSender") EmailSender fallbackClient) {
        this.primaryClient = primaryClient;
        this.fallbackClient = fallbackClient;
    }

    // == Public Methods ==
    public Boolean sendEmail(String email, String subject, String bodyText){

        try {
            return primaryClient.sendEmail(email, subject, bodyText);
        } catch (IllegalStateException exception){
            return fallbackClient.sendEmail(email, subject, bodyText);
        }
    }
}
