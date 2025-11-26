package commerce.eshop.core.tests.application.orderTest.factory;

import commerce.eshop.core.application.order.factory.OrderFactory;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import commerce.eshop.core.web.mapper.OrderServiceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderFactoryTest {

    // == Fields ==
    private OrderServiceMapper orderServiceMapper;
    private OrderFactory orderFactory;

    // == Tests ==

    @BeforeEach
    void setUp() {
        orderServiceMapper = mock(OrderServiceMapper.class);

        orderFactory = new OrderFactory(orderServiceMapper);
    }

    @Test
    void handle_success() {
        Customer customer = mock(Customer.class);
        DTOOrderCustomerAddress dto = mock(DTOOrderCustomerAddress.class);
        BigDecimal total = new BigDecimal("100.50");
        Map<String, Object> mappedAddress = Map.of("city", "Athens");

        when(orderServiceMapper.toMap(dto)).thenReturn(mappedAddress);

        Order order = orderFactory.handle(customer, dto, total);

        assertNotNull(order);
        assertEquals(customer, order.getCustomer());
        assertEquals(total, order.getTotalOutstanding());
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus());
        assertEquals(mappedAddress, order.getAddressToSend());

        verify(orderServiceMapper, times(1)).toMap(dto);
    }

    @Test
    void toDto_success() {
        Order order = mock(Order.class);
        DTOOrderPlacedResponse response = mock(DTOOrderPlacedResponse.class);

        when(orderServiceMapper.toDto(order)).thenReturn(response);

        DTOOrderPlacedResponse result = orderFactory.toDto(order);

        assertSame(response, result);
        verify(orderServiceMapper, times(1)).toDto(order);
    }
}