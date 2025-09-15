package commerce.eshop.core.service.async.external;

public interface EmailSender {

    public boolean sendEmail(String email, String subject, String bodyText);
}
