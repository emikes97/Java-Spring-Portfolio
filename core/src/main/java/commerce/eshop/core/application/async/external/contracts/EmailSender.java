package commerce.eshop.core.application.async.external.contracts;

public interface EmailSender {

    public boolean sendEmail(String email, String subject, String bodyText);
}
