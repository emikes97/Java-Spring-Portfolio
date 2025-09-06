package commerse.eshop.core.model.entity.enums;

/// Text bank for auditing ///
public enum AuditMessage {
    // === Customers ===
    CREATE_USER_SUCCESS("Customer created successfully"),
    CREATE_USER_FAILED("Customer creation failed"),
    GET_PROFILE_SUCCESS("Customer profile retrieved successfully"),
    GET_PROFILE_FAILED("Customer profile retrieval failed"),
    GET_ORDERS_SUCCESS("Customer orders retrieved successfully"),
    GET_ORDERS_FAILED("Customer orders retrieval failed"),
    GET_CART_ITEMS_SUCCESS("Customer cart items retrieved successfully"),
    GET_CART_ITEMS_FAILED("Customer cart items retrieval failed"),
    UPDATE_NAME_SUCCESS("Customer name updated successfully"),
    UPDATE_NAME_FAILED("Customer name update failed"),
    UPDATE_SURNAME_SUCCESS("Customer surname updated successfully"),
    UPDATE_SURNAME_FAILED("Customer surname update failed"),
    UPDATE_FULLNAME_SUCCESS("Customer full name updated successfully"),
    UPDATE_FULLNAME_FAILED("Customer full name update failed"),
    UPDATE_USERNAME_SUCCESS("Customer username updated successfully"),
    UPDATE_USERNAME_FAILED("Customer username update failed"),
    UPDATE_PASSWORD_SUCCESS("Customer password updated successfully"),
    UPDATE_PASSWORD_FAILED("Customer password update failed"),

    // === Customer Addresses ===
    ADDR_GET_ALL_SUCCESS("Customer addresses retrieved successfully"),
    ADDR_GET_ALL_FAILED("Customer addresses retrieval failed"),
    ADDR_ADD_SUCCESS("Customer address added successfully"),
    ADDR_ADD_FAILED("Customer address addition failed"),
    ADDR_UPDATE_SUCCESS("Customer address updated successfully"),
    ADDR_UPDATE_FAILED("Customer address update failed"),
    ADDR_MAKE_DEFAULT_SUCCESS("Customer address set as default successfully"),
    ADDR_MAKE_DEFAULT_FAILED("Customer address set as default failed"),
    ADDR_DELETE_SUCCESS("Customer address deleted successfully"),
    ADDR_DELETE_FAILED("Customer address deletion failed"),

    // === Customer Payment Methods ===
    PM_GET_ALL_SUCCESS("Customer payment methods retrieved successfully"),
    PM_GET_ALL_FAILED("Customer payment methods retrieval failed"),
    PM_ADD_SUCCESS("Customer payment method added successfully"),
    PM_ADD_FAILED("Customer payment method addition failed"),
    PM_UPDATE_SUCCESS("Customer payment method updated successfully"),
    PM_UPDATE_FAILED("Customer payment method update failed"),
    PM_RETRIEVE_SUCCESS("Customer payment method retrieved successfully"),
    PM_RETRIEVE_FAILED("Customer payment method retrieval failed"),
    PM_DELETE_SUCCESS("Customer payment method deleted successfully"),
    PM_DELETE_FAILED("Customer payment method deletion failed"),

    // === Cart ===
    CART_VIEW_ALL_SUCCESS("Cart items viewed successfully"),
    CART_VIEW_ALL_FAILED("Cart items view failed"),
    CART_FIND_ITEM_SUCCESS("Cart item found successfully"),
    CART_FIND_ITEM_FAILED("Cart item find failed"),
    CART_ADD_ITEM_SUCCESS("Cart item added successfully"),
    CART_ADD_ITEM_FAILED("Cart item addition failed"),
    CART_REMOVE_SUCCESS("Cart item removed successfully"),
    CART_REMOVE_FAILED("Cart item removal failed"),
    CART_CLEAR_SUCCESS("Cart cleared successfully"),
    CART_CLEAR_FAILED("Cart clear failed"),

    // === Categories ===
    CATEGORY_CREATE_SUCCESS("Category created successfully"),
    CATEGORY_CREATE_FAILED("Category creation failed"),
    CATEGORY_UPDATE_SUCCESS("Category updated successfully"),
    CATEGORY_UPDATE_FAILED("Category update failed"),
    CATEGORY_DELETE_SUCCESS("Category deleted successfully"),
    CATEGORY_DELETE_FAILED("Category deletion failed"),

    // === Orders ===
    ORDER_PLACE_SUCCESS("Order placed successfully"),
    ORDER_PLACE_FAILED("Order placement failed"),
    ORDER_CANCEL_SUCCESS("Order cancelled successfully"),
    ORDER_CANCEL_FAILED("Order cancellation failed"),
    ORDER_VIEW_SUCCESS("Order viewed successfully"),
    ORDER_VIEW_FAILED("Order view failed"),

    // === Products ===
    PRODUCT_ADD_SUCCESS("Product added successfully"),
    PRODUCT_ADD_FAILED("Product addition failed"),
    PRODUCT_GET_SUCCESS("Product retrieved successfully"),
    PRODUCT_GET_FAILED("Product retrieval failed"),
    PRODUCT_GET_ALL_SUCCESS("All products retrieved successfully"),
    PRODUCT_GET_ALL_FAILED("All products retrieval failed"),
    PRODUCT_INCREASE_QTY_SUCCESS("Product quantity increased successfully"),
    PRODUCT_INCREASE_QTY_FAILED("Product quantity increase failed"),
    PRODUCT_LINK_SUCCESS("Product linked successfully"),
    PRODUCT_LINK_FAILED("Product link failed"),
    PRODUCT_UNLINK_SUCCESS("Product unlinked successfully"),
    PRODUCT_UNLINK_FAILED("Product unlink failed"),
    PRODUCT_REMOVE_SUCCESS("Product removed successfully"),
    PRODUCT_REMOVE_FAILED("Product removal failed"),

    // === Transactions ===
    TRANSACTION_PAY_SUCCESS("Transaction payment executed successfully"),
    TRANSACTION_PAY_FAILED("Transaction payment failed");

    private final String message;

    AuditMessage(String message) {this.message = message;}

    public String getMessage() {return message;}
}
