package commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = UseSavedMethod.class, name = "USE_SAVED_METHOD"),
                @JsonSubTypes.Type(value = UseNewCard.class, name = "USE_NEW_CARD")
        }
)
public sealed interface PaymentInstruction permits UseSavedMethod, UseNewCard {}
