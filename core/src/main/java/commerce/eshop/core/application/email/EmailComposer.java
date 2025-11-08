package commerce.eshop.core.application.email;

import commerce.eshop.core.application.events.EmailEventRequest;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.application.email.constants.EmailBody;
import commerce.eshop.core.application.email.constants.EmailSubject;
import commerce.eshop.core.application.email.properties.EmailBrandProps;
import commerce.eshop.core.application.email.templating.TemplateEngine;
import commerce.eshop.core.application.email.templating.Vars;
import commerce.eshop.core.application.email.enums.EmailKind;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Map;

@Component
public class EmailComposer {

    private final EmailBrandProps brand;

    public EmailComposer(EmailBrandProps brand) { this.brand = brand; }

    // --- Customer events ---
    public EmailEventRequest accountCreated(Customer c, String verifyLink) {
        Map<String,Object> v = Vars.of()
                .put("customerName", c.getName() + " " + c.getSurname())
                .put("companyName", brand.companyName())
                .put("verifyLink", verifyLink)
                .put("supportEmail", brand.supportEmail())
                .build();
        return build(c.getCustomerId(), null, c.getEmail(),
                EmailKind.ACCOUNT_CREATED, EmailSubject.CUSTOMER_ACCOUNT_CREATED, EmailBody.CUSTOMER_ACCOUNT_CREATED, v);
    }

    public EmailEventRequest accountUpdated(Customer c, String changedFields) {
        Map<String,Object> v = Vars.of()
                .put("customerName", c.getName() + " " + c.getSurname())
                .put("companyName", brand.companyName())
                .put("timestamp", OffsetDateTime.now())
                .put("changedFields", changedFields)
                .put("securityLink", brand.baseUrl() + "/account/security")
                .put("supportEmail", brand.supportEmail())
                .build();
        return build(c.getCustomerId(), null, c.getEmail(),
                EmailKind.ACCOUNT_UPDATE, EmailSubject.CUSTOMER_ACCOUNT_UPDATE, EmailBody.CUSTOMER_ACCOUNT_UPDATE, v);
    }

    public EmailEventRequest passwordUpdated(Customer c, boolean success) {
        String subj = success ? EmailSubject.CUSTOMER_PASSWORD_UPDATE_SUCCESS : EmailSubject.CUSTOMER_PASSWORD_UPDATE_FAILURE;
        String body = success ? EmailBody.CUSTOMER_PASSWORD_UPDATE_SUCCESS : EmailBody.CUSTOMER_PASSWORD_UPDATE_FAILURE;
        Map<String,Object> v = Vars.of()
                .put("customerName", c.getName() + " " + c.getSurname())
                .put("companyName", brand.companyName())
                .put("timestamp", OffsetDateTime.now())
                .put("resetLink", brand.baseUrl() + "/account/reset")
                .put("changePasswordLink", brand.baseUrl() + "/account/change-password")
                .put("supportEmail", brand.supportEmail())
                .build();
        return build(c.getCustomerId(), null, c.getEmail(), EmailKind.PASSWORD_CHANGE, subj, body, v);
    }

    // --- Orders ---
    public EmailEventRequest orderConfirmed(Customer c, Order o, BigDecimal total, String currency) {
        Map<String,Object> v = Vars.of()
                .put("customerName", c.getName() + " " + c.getSurname())
                .put("companyName", brand.companyName())
                .put("orderId", o.getOrderId())
                .put("orderDate", o.getCreatedAt().toLocalDate())
                .put("total", total)
                .put("currency", currency)
                .put("orderLink", brand.baseUrl() + "/orders/" + o.getOrderId())
                .build();
        return build(c.getCustomerId(), o.getOrderId(), c.getEmail(),
                EmailKind.ORDER_CONFIRMATION, EmailSubject.ORDER_CONFIRMATION, EmailBody.ORDER_CONFIRMATION, v);
    }

    public EmailEventRequest orderCancelled(Customer c, Order o) {
        Map<String,Object> v = Vars.of()
                .put("customerName", c.getName() + " " + c.getSurname())
                .put("companyName", brand.companyName())
                .put("orderId", o.getOrderId())
                .put("timestamp", OffsetDateTime.now())
                .put("supportEmail", brand.supportEmail())
                .build();
        return build(c.getCustomerId(), o.getOrderId(), c.getEmail(),
                EmailKind.ORDER_CANCEL_CONFIRMATION, EmailSubject.ORDER_CANCEL_CONFIRMATION, EmailBody.ORDER_CANCEL_CONFIRMATION, v);
    }

    // --- Payments / Transactions ---
    public EmailEventRequest paymentConfirmed(Customer c, Order o, Transaction t, BigDecimal amount, String currency) {
        Map<String,Object> v = Vars.of()
                .put("customerName", c.getName() + " " + c.getSurname())
                .put("companyName", brand.companyName())
                .put("paymentId", t.getTransactionId())
                .put("orderId", o.getOrderId())
                .put("amount", amount)
                .put("currency", currency)
                .put("paymentStatus", "Paid")
                .put("orderLink", brand.baseUrl() + "/orders/" + o.getOrderId())
                .build();
        return build(c.getCustomerId(), t.getTransactionId(), c.getEmail(),
                EmailKind.PAYMENT_CONFIRMATION, EmailSubject.PAYMENT_CONFIRMATION, EmailBody.PAYMENT_CONFIRMATION, v);
    }

    public EmailEventRequest paymentFailed(Customer c, Order o, Transaction t, String reason) {
        Map<String,Object> v = Vars.of()
                .put("customerName", c.getName() + " " + c.getSurname())
                .put("companyName", brand.companyName())
                .put("paymentId", t.getTransactionId())
                .put("orderId", o.getOrderId())
                .put("failureReason", reason)
                .put("paymentLink", brand.baseUrl() + "/pay/" + t.getTransactionId())
                .put("supportEmail", brand.supportEmail())
                .build();
        return build(c.getCustomerId(), t.getTransactionId(), c.getEmail(),
                EmailKind.PAYMENT_FAILED_CONFIRMATION, EmailSubject.PAYMENT_FAILED_CONFIRMATION, EmailBody.PAYMENT_FAILED_CONFIRMATION, v);
    }

    // --- Core builder ---
    private EmailEventRequest build(
            UUID customerId, UUID refId, String toEmail,
            EmailKind kind, String subjectTpl, String bodyTpl, Map<String,Object> vars) {

        String subject = TemplateEngine.render(subjectTpl, vars);
        String body    = TemplateEngine.render(bodyTpl, vars);
        String customerName = String.valueOf(vars.getOrDefault("customerName", ""));

        return new EmailEventRequest(customerId, refId, customerName, toEmail, kind, subject, body);
    }
}
