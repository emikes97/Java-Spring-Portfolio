package commerce.eshop.core.tests.application.orderTest.queries;

import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.order.queries.OrderQueries;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.OrderItem;
import commerce.eshop.core.web.dto.response.Order.DTOOrderDetailsResponse;
import commerce.eshop.core.web.dto.response.Order.DTOOrderItemsResponse;
import commerce.eshop.core.web.mapper.OrderServiceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderQueriesTest {

    // == Fields ==
    private DomainLookupService domainLookupService;
    private OrderServiceMapper orderServiceMapper;
    private OrderQueries orderQueries;

    @BeforeEach
    void setUp() {
        domainLookupService = mock(DomainLookupService.class);
        orderServiceMapper = mock(OrderServiceMapper.class);

        orderQueries = new OrderQueries(domainLookupService, orderServiceMapper);
    }

    @Test
    void viewOrder_fetchesOrderItemsAndMapsToDetailsDto() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        OrderItem orderItem = mock(OrderItem.class);
        DTOOrderDetailsResponse mappedResponse = mock(DTOOrderDetailsResponse.class);


        when(domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.ORDER_VIEW))
                .thenReturn(order);
        when(orderItem.getOrderItemId()).thenReturn(10L);
        when(orderItem.getProductName()).thenReturn("Test Product");
        when(orderItem.getQuantity()).thenReturn(3);
        when(orderItem.getPriceAt()).thenReturn(new BigDecimal("19.99"));
        when(domainLookupService.getOrderItemsStream(orderId))
                .thenReturn(Stream.of(orderItem));
        when(orderServiceMapper.toDtoDetails(eq(order), anyList()))
                .thenReturn(mappedResponse);

        ArgumentCaptor<List<DTOOrderItemsResponse>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);

        DTOOrderDetailsResponse result = orderQueries.viewOrder(customerId, orderId);

        assertSame(mappedResponse, result);

        verify(domainLookupService, times(1))
                .getOrderOrThrow(customerId, orderId, EndpointsNameMethods.ORDER_VIEW);
        verify(domainLookupService, times(1))
                .getOrderItemsStream(orderId);
        verify(orderServiceMapper, times(1))
                .toDtoDetails(eq(order), itemsCaptor.capture());

        List<DTOOrderItemsResponse> capturedItems = itemsCaptor.getValue();
        assertEquals(1, capturedItems.size());

        DTOOrderItemsResponse dtoItem = capturedItems.get(0);
        assertEquals(10L, dtoItem.productId());
        assertEquals("Test Product", dtoItem.productName());
        assertEquals(3, dtoItem.quantity());
        assertEquals(new BigDecimal("19.99"), dtoItem.priceAt());
    }
}