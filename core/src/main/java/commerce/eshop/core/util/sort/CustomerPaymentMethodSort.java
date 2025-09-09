package commerce.eshop.core.util.sort;

import java.util.Map;

public final class CustomerPaymentMethodSort {

    private CustomerPaymentMethodSort(){}

    // == Whitelist & Constraints ==

    // Allowed sort columns for customer_payment_methods pagination
    public static final Map<String, String> PAYMENT_METHOD_SORT_WHITELIST = Map.ofEntries(
            Map.entry("provider", "provider"),
            Map.entry("brand", "brand"),
            Map.entry("last_4", "last4"),
            Map.entry("year_exp", "yearExp"),
            Map.entry("month_exp", "monthExp"),
            Map.entry("is_default", "isDefault"),
            Map.entry("created_at", "createdAt")
    );

    public static final int MAX_PAGE_SIZE = 25;
}
