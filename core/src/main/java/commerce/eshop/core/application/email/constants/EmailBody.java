package commerce.eshop.core.application.email.constants;

public final class EmailBody {

    private EmailBody(){}

    // == Customer ==
    public static final String CUSTOMER_ACCOUNT_CREATED = """
        Hi {{customerName}},

        Welcome to {{companyName}}! Your account has been created.

        To finish setting things up, please verify your email:
        {{verifyLink}}

        If you didn’t create this account, please ignore this message or contact us at {{supportEmail}}.

        — The {{companyName}} Team
        """;

    public static final String CUSTOMER_ACCOUNT_UPDATE = """
        Hi {{customerName}},

        Your account details were updated on {{timestamp}}.

        Changed fields: {{changedFields}}

        If you didn’t make these changes, secure your account immediately:
        {{securityLink}}  •  Need help? {{supportEmail}}

        — The {{companyName}} Team
        """;

    public static final String CUSTOMER_PASSWORD_UPDATE_SUCCESS = """
        Hi {{customerName}},

        Your password was successfully updated on {{timestamp}}.
        If this wasn’t you, reset your password right away:
        {{resetLink}}

        — The {{companyName}} Security Team
        """;

    public static final String CUSTOMER_PASSWORD_UPDATE_FAILURE = """
        Hi {{customerName}},

        We couldn’t complete your password change attempt on {{timestamp}}.
        No changes were made to your account.

        You can try again here:
        {{changePasswordLink}}
        If you continue to have issues, contact {{supportEmail}}.

        — The {{companyName}} Support Team
        """;

    // == Orders ==
    public static final String ORDER_CONFIRMATION = """
        Hi {{customerName}},

        Thanks for your order {{orderId}} placed on {{orderDate}}.
        Total: {{total}} {{currency}}

        We’ll email you when it ships. You can view your order anytime:
        {{orderLink}}

        — The {{companyName}} Team
        """;

    public static final String ORDER_CANCEL_CONFIRMATION = """
        Hi {{customerName}},

        Your order {{orderId}} has been cancelled on {{timestamp}}.
        If you didn’t request this, please reach out immediately: {{supportEmail}}.

        — The {{companyName}} Team
        """;

    // == Payments / Transactions ==
    public static final String PAYMENT_CONFIRMATION = """
        Hi {{customerName}},

        We’ve received your payment {{paymentId}} for order {{orderId}}.
        Amount: {{amount}} {{currency}}
        Status: {{paymentStatus}}

        Thank you! You can review your order here:
        {{orderLink}}

        — The {{companyName}} Team
        """;

    public static final String PAYMENT_FAILED_CONFIRMATION = """
        Hi {{customerName}},

        We couldn’t process your payment {{paymentId}} for order {{orderId}}.
        Reason: {{failureReason}}

        No funds were captured. You can try again here:
        {{paymentLink}}  •  Need help? {{supportEmail}}

        — The {{companyName}} Team
        """;
}
