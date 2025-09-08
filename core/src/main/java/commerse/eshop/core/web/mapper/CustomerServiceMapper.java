package commerse.eshop.core.web.mapper;

import commerse.eshop.core.model.entity.CartItem;
import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.Order;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerAdResponse;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerCartItemResponse;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerOrderResponse;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomerServiceMapper {

    public DTOCustomerResponse toDtoCustomerRes(Customer c){
        return new DTOCustomerResponse(
                c.getCustomerId(),
                c.getPhoneNumber(),
                c.getEmail(),
                c.getUsername(),
                c.getName(),
                c.getSurname(),
                c.getCreatedAt()
        );
    }

    @SuppressWarnings("unchecked")
    public DTOCustomerOrderResponse toDtoCustomerOrder(Order o){
        var addrDto = toDtoFromJson((Map<String, Object>) o.getAddressToSend());
        return new DTOCustomerOrderResponse(
                o.getOrderId(),
                o.getCustomer().getCustomerId(),
                o.getTotalOutstanding(),
                addrDto,
                o.getCreatedAt(),
                o.getCompletedAt()
        );
    }

    public DTOCustomerCartItemResponse toDtoCartItem(CartItem ci){
        return new DTOCustomerCartItemResponse(
                ci.getCartItemId(),
                ci.getCart().getCartId(),
                ci.getProduct().getProductId(),
                ci.getProductName(),
                ci.getQuantity(),
                ci.getPriceAt(),
                ci.getAddedAt());
    }

    private DTOCustomerAdResponse toDtoFromJson(Map<String, Object> a){
        return new DTOCustomerAdResponse(
                (String) a.get("country"),
                (String) a.get("street"),
                (String) a.get("city"),
                (String) a.get("postalCode")
        );
    }
}
