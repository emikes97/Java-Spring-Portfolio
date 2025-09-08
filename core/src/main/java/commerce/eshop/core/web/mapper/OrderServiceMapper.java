package commerce.eshop.core.web.mapper;

import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerce.eshop.core.web.dto.response.Order.DTOOrderItemsResponse;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderServiceMapper {

    public Map<String,Object> toMap(DTOOrderCustomerAddress dto){
        Map<String, Object> map = new HashMap<>();
        map.put("country", dto.country());
        map.put("street", dto.street());
        map.put("city", dto.city());
        map.put("postalCode", dto.postalCode());
        return map;
    }

    public DTOOrderCustomerAddress toAdDto(Map<String, Object> addr){
        return new DTOOrderCustomerAddress(
                (String) addr.get("country"),
                (String) addr.get("street"),
                (String) addr.get("city"),
                (String) addr.get("postalCode"));
    }

    public DTOOrderPlacedResponse toDto(Order o){
        return new DTOOrderPlacedResponse(
                o.getOrderId(),
                o.getTotalOutstanding(),
                toAdDto(o.getAddressToSend()),
                o.getCreatedAt(),
                o.getCompletedAt()
        );}

    public DTOOrderDetailsResponse toDtoDetails(Order o, List<DTOOrderItemsResponse> orderItems ){

        return new DTOOrderDetailsResponse(
                o.getOrderId(),
                o.getTotalOutstanding(),
                toAdDto(o.getAddressToSend()),
                orderItems,
                o.getCreatedAt(),
                o.getCompletedAt()
        );
    }
}
