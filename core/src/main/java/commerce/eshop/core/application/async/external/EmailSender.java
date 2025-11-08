package commerce.eshop.core.application.async.external;

public interface EmailSender {

    public boolean sendEmail(String email, String subject, String bodyText);
}
