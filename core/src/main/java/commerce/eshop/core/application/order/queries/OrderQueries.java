package commerce.eshop.core.application.order.queries;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerce.eshop.core.web.dto.response.Order.DTOOrderItemsResponse;
import commerce.eshop.core.web.mapper.OrderServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class OrderQueries {

    // == Fields ==
    private final DomainLookupService domainLookupService;
    private final OrderServiceMapper orderServiceMapper;

    // == Constructors ==
    @Autowired
    public OrderQueries(DomainLookupService domainLookupService, OrderServiceMapper orderServiceMapper) {
        this.domainLookupService = domainLookupService;
        this.orderServiceMapper = orderServiceMapper;
    }

    // == Public Methods ==
    @Transactional(readOnly = true)
    public DTOOrderDetailsResponse viewOrder(UUID customerId, UUID orderId){

        final Order order = domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.ORDER_VIEW);

        List<DTOOrderItemsResponse> orderItems = domainLookupService.getOrderItemsStream(orderId).map(oi -> new DTOOrderItemsResponse(
                        oi.getOrderItemId(),     // use snapshot columns on order_item
                        oi.getProductName(),
                        oi.getQuantity(),
                        oi.getPriceAt()
                )).toList();

        return orderServiceMapper.toDtoDetails(order, orderItems);
    }
}
