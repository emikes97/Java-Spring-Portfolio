package commerse.eshop.core.model.entity.consts;

public final class EndpointsNameMethods {

    private EndpointsNameMethods(){};

    // == Constants ==

    // === Customers ===
    public static final String CREATE_USER           = "createUser";
    public static final String GET_PROFILE_BY_ID     = "getProfile";       // UUID param
    public static final String GET_PROFILE_BY_SEARCH = "getProfile";       // phone/email param
    public static final String GET_ORDERS            = "getOrders";
    public static final String GET_CART_ITEMS        = "getCartItems";

    public static final String UPDATE_NAME           = "updateName";
    public static final String UPDATE_SURNAME        = "updateSurname";
    public static final String UPDATE_FULLNAME       = "updateFullName";
    public static final String UPDATE_USERNAME       = "updateUsername";
    public static final String UPDATE_PASSWORD       = "updateUserPassword";

    // === Customer Addresses ===
    public static final String ADDR_GET_ALL        = "getAllAddresses";
    public static final String ADDR_ADD            = "addCustomerAddress";
    public static final String ADDR_UPDATE         = "updateCustomerAddress";
    public static final String ADDR_MAKE_DEFAULT   = "makeDefaultCustomerAddress";
    public static final String ADDR_DELETE         = "deleteCustomerAddress";

    // === Customer Payment Methods ===
    public static final String PM_GET_ALL     = "getAllPaymentMethods";
    public static final String PM_ADD         = "addPaymentMethod";
    public static final String PM_UPDATE      = "updatePaymentMethod";
    public static final String PM_RETRIEVE    = "retrievePaymentMethod";
    public static final String PM_DELETE      = "deletePaymentMethod";

    // === Cart ===
    public static final String CART_VIEW_ALL  = "viewAllCartItems";
    public static final String CART_FIND_ITEM = "findItem";
    public static final String CART_ADD_ITEM  = "addCartItem";
    public static final String CART_REMOVE    = "removeCartItem";
    public static final String CART_CLEAR     = "clearCart";

    // === Categories ===
    public static final String CATEGORY_CREATE = "addNewCategory";
    public static final String CATEGORY_UPDATE = "update";
    public static final String CATEGORY_DELETE = "delete";

    // === Orders ===
    public static final String ORDER_PLACE  = "placeOrder";
    public static final String ORDER_CANCEL = "cancel";
    public static final String ORDER_VIEW   = "viewOrder";

    // === Products ===
    public static final String PRODUCT_ADD          = "addProduct";
    public static final String PRODUCT_GET          = "getProduct";
    public static final String PRODUCT_GET_ALL      = "getAllProducts";
    public static final String PRODUCT_INCREASE_QTY = "increaseQuantity";
    public static final String PRODUCT_DECREASE_QTY = "decreaseQuantity";
    public static final String PRODUCT_LINK         = "link";
    public static final String PRODUCT_UNLINK       = "unlink";
    public static final String PRODUCT_REMOVE       = "removeProduct";

    // === Transactions ===
    public static final String TRANSACTION_PAY = "pay";
    public static final String EVENT_PUBLISHED = "publishEvent";
}
