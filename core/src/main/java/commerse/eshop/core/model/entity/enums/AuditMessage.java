package commerse.eshop.core.model.entity.enums;

/// Text bank for auditing ///
public enum AuditMessage {
    CREATE_CUSTOMER_SUCCESS("Customer created successfully"),
    CREATE_CUSTOMER_FAILED("Customer creation failed"),
    CREATE_ORDER_SUCCESS("Order created successfully"),
    PAYMENT_SUCCESS("Payment executed successfully"),
    PAYMENT_FAILED("Payment failed");

    private final String message;

    AuditMessage(String message) {this.message = message;}

    public String getMessage() {return message;}
}
