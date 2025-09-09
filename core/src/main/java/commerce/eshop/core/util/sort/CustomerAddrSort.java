package commerce.eshop.core.util.sort;

import java.util.Map;

public final class CustomerAddrSort {

    private CustomerAddrSort(){}

    // == Constraints - Whitelisting ==
    public static final Map<String, String> ALLOWED_SORTS = Map.of(
            "country",     "country",
            "street",      "street",
            "city",        "city",
            "postal_code", "postalCode",  // API → entity
            "created_at",  "createdAt"    // API → entity
    );

    public static final int MAX_PAGE_SIZE = 25;
}
