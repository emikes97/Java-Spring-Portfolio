package commerce.eshop.core.model.states;

public enum CheckoutStates {
    PENDING,
    PROCESSING,
    ORDER_CREATED,
    TRANSACTION_READY,
    COMPLETED,
    FAILED
}