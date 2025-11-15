package commerce.eshop.core.application.order.factory;

import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import commerce.eshop.core.web.mapper.OrderServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderFactory {

    // == Fields ==
    private final OrderServiceMapper mapper;

    // == Constructors ==
    @Autowired
    public OrderFactory(OrderServiceMapper mapper) {
        this.mapper = mapper;
    }

    // == Public Methods ==

    public Order handle(Customer customer, DTOOrderCustomerAddress dto, BigDecimal total_outstanding){
        Order order = new Order(customer, mapper.toMap(dto), total_outstanding);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        return order;
    }

    public DTOOrderPlacedResponse toDto(Order order){
        return mapper.toDto(order);
    }
}
