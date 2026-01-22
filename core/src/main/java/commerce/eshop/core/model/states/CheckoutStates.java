package commerce.eshop.core.model.states;

public enum CheckoutStates {
    PENDING,
    PROCESSING,
    ORDER_CREATED,
    TRANSACTION_READY,
    TRANSACTION_PROCESSING,
    TRANSACTION_WAITING_CLIENT_CONFIRMATION,
    COMPLETED,
    FAILED
}