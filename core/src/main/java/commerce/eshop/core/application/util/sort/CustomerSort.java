package commerce.eshop.core.application.util.sort;

import java.util.Map;

public final class CustomerSort {

    private CustomerSort(){}

    // == Whitelisting & Constraints ==

    /** For DTOCustomerResponse */
    public static final Map<String, String> CUSTOMER_PROFILE_SORT_WHITELIST = Map.ofEntries(
            Map.entry("username", "username"),
            Map.entry("name", "name"),
            Map.entry("surname", "surname"),
            Map.entry("created_at", "createdAt")
    );

    /** For DTOCustomerOrderResponse */
    public static final Map<String, String> CUSTOMER_ORDERS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("total_outstanding", "totalOutstanding"),
            Map.entry("created_at", "orderCreatedAt"),
            Map.entry("completed_at", "orderCompletedAt")
    );

    /** For DTOCustomerCartItemResponse */
    public static final Map<String, String> CUSTOMER_CART_ITEMS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("quantity", "quantity"),
            Map.entry("price_at", "priceAt"),
            Map.entry("added_at", "addedAt")
    );

    /** For DTOCustomerAddressResponse */
    public static final Map<String, String> CUSTOMER_ADDRESS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("country", "country"),
            Map.entry("street", "street"),
            Map.entry("city", "city"),
            Map.entry("postal_code", "postalCode")
    );

    public static final int MAX_PAGE_SIZE = 25;
}
