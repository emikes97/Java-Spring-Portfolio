package commerce.eshop.core.application.email.constants;

public final class EmailSubject {

    private EmailSubject(){}

    // == Constants ==

    // == Customer ==
    public static final String CUSTOMER_ACCOUNT_CREATED            = "ACCOUNT has been created";
    public static final String CUSTOMER_ACCOUNT_UPDATE             = "Account has been updated";
    public static final String CUSTOMER_PASSWORD_UPDATE_SUCCESS    = "Password has been updated";
    public static final String CUSTOMER_PASSWORD_UPDATE_FAILURE    = "Password has not been updated";

    // == Orders ==
    public static final String ORDER_CONFIRMATION                  = "Order confirmed";
    public static final String ORDER_CANCEL_CONFIRMATION           = "Order cancelled";

    // == Payments / Transactions ==
    public static final String PAYMENT_CONFIRMATION                = "Payment received";
    public static final String PAYMENT_FAILED_CONFIRMATION         = "Payment failed";
}
