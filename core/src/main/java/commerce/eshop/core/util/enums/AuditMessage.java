package commerce.eshop.core.util.enums;

/// Text bank for auditing ///
public enum AuditMessage {
    // === Customers ===
    CREATE_USER_SUCCESS("Customer created successfully"),
    GET_PROFILE_SUCCESS("Customer profile retrieved successfully"),
    GET_ORDERS_SUCCESS("Customer orders retrieved successfully"),
    GET_CART_ITEMS_SUCCESS("Customer cart items retrieved successfully"),
    UPDATE_NAME_SUCCESS("Customer name updated successfully"),
    UPDATE_SURNAME_SUCCESS("Customer surname updated successfully"),
    UPDATE_FULLNAME_SUCCESS("Customer full name updated successfully"),
    UPDATE_USERNAME_SUCCESS("Customer username updated successfully"),
    UPDATE_PASSWORD_SUCCESS("Customer password updated successfully"),

    // === Customer Addresses ===
    ADDR_GET_ALL_SUCCESS("Customer addresses retrieved successfully"),
    ADDR_ADD_SUCCESS("Customer address added successfully"),
    ADDR_UPDATE_SUCCESS("Customer address updated successfully"),
    ADDR_MAKE_DEFAULT_SUCCESS("Customer address set as default successfully"),
    ADDR_DELETE_SUCCESS("Customer address deleted successfully"),

    // === Customer Payment Methods ===
    PM_GET_ALL_SUCCESS("Customer payment methods retrieved successfully"),
    PM_ADD_SUCCESS("Customer payment method added successfully"),
    PM_UPDATE_SUCCESS("Customer payment method updated successfully"),
    PM_RETRIEVE_SUCCESS("Customer payment method retrieved successfully"),
    PM_DELETE_SUCCESS("Customer payment method deleted successfully"),

    // === Cart ===
    CART_VIEW_ALL_SUCCESS("Cart items viewed successfully"),
    CART_FIND_ITEM_SUCCESS("Cart item found successfully"),
    CART_ADD_ITEM_SUCCESS("Cart item added successfully"),
    CART_REMOVE_SUCCESS("Cart item removed successfully"),
    CART_CLEAR_SUCCESS("Cart cleared successfully"),

    // === Categories ===
    CATEGORY_CREATE_SUCCESS("Category created successfully"),
    CATEGORY_UPDATE_SUCCESS("Category updated successfully"),
    CATEGORY_DELETE_SUCCESS("Category deleted successfully"),

    // === Orders ===
    ORDER_PLACE_SUCCESS("Order placed successfully"),
    ORDER_CANCEL_SUCCESS("Order cancelled successfully"),
    ORDER_VIEW_SUCCESS("Order viewed successfully"),

    // === Products ===
    PRODUCT_ADD_SUCCESS("Product added successfully"),
    PRODUCT_GET_SUCCESS("Product retrieved successfully"),
    PRODUCT_GET_ALL_SUCCESS("All products retrieved successfully"),
    PRODUCT_INCREASE_QTY_SUCCESS("Product quantity increased successfully"),
    PRODUCT_DECREASE_QTY_SUCCESS("Product quantity decreased successfully"),
    PRODUCT_LINK_SUCCESS("Product linked successfully"),
    PRODUCT_UNLINK_SUCCESS("Product unlinked successfully"),
    PRODUCT_REMOVE_SUCCESS("Product removed successfully"),

    // === Transactions ===
    TRANSACTION_PAY_SUCCESS("Transaction payment executed successfully"),

    // === Wishlist ===
    WISHLIST_ADD_NEW_WISH_SUCCESS("New wish added successfully"),
    WISHLIST_FIND_ALL_WISHES_SUCCESS("All wishes retrieved successfully"),
    WISHLIST_FIND_A_WISH_SUCCESS("Wish retrieved successfully"),
    WISHLIST_REMOVE_A_WISH_SUCCESS("Wish removed successfully"),
    WISHLIST_CLEARED_SUCCESS("Wishlist cleared successfully");


    private final String message;

    AuditMessage(String message) {this.message = message;}

    public String getMessage() {return message;}
}
