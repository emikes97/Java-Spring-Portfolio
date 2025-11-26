package commerce.eshop.core.tests.application.orderTest.orchestrator;

import commerce.eshop.core.application.events.order.PlacedOrderEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.order.factory.DefaultAddressFactory;
import commerce.eshop.core.application.order.factory.OrderFactory;
import commerce.eshop.core.application.order.orchestrator.OrderPlacementExecutor;
import commerce.eshop.core.application.order.validation.AuditedOrderValidation;
import commerce.eshop.core.application.order.writer.OrderCartWriter;
import commerce.eshop.core.application.order.writer.OrderWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotSerializeTransactionException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderPlacementExecutorTest {

    // == Field ==
    private AuditedOrderValidation auditedOrderValidation;
    private DefaultAddressFactory defaultAddressFactory;
    private OrderCartWriter orderCartWriter;
    private OrderWriter orderWriter;
    private OrderFactory orderFactory;
    private DomainLookupService domainLookupService;
    private ApplicationEventPublisher applicationEventPublisher;
    private OrderPlacementExecutor orderPlacementExecutor;

    @BeforeEach
    void setUp() {
        auditedOrderValidation = mock(AuditedOrderValidation.class);
        defaultAddressFactory = mock(DefaultAddressFactory.class);
        orderCartWriter = mock(OrderCartWriter.class);
        orderWriter = mock(OrderWriter.class);
        orderFactory = mock(OrderFactory.class);
        domainLookupService = mock(DomainLookupService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        orderPlacementExecutor = new OrderPlacementExecutor(auditedOrderValidation, defaultAddressFactory, orderCartWriter,
                orderWriter, orderFactory, domainLookupService, applicationEventPublisher);
    }

    @Test
    void tryPlaceOrder_success() {
        UUID customerId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        Cart cart = mock(Cart.class);
        Order orderBeforeSave = mock(Order.class);
        Order orderSaved = mock(Order.class);
        DTOOrderCustomerAddress defaultAddress = mock(DTOOrderCustomerAddress.class);
        DTOOrderPlacedResponse dtoResponse = mock(DTOOrderPlacedResponse.class);
        BigDecimal totalOutstanding = new BigDecimal("99.99");

        // Lookups
        when(domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE))
                .thenReturn(customer);
        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(orderCartWriter.lockCart(cartId)).thenReturn(true);
        DTOOrderCustomerAddress incomingAddress = null;
        when(auditedOrderValidation.checkAddressDto(incomingAddress)).thenReturn(true);
        when(defaultAddressFactory.handle(customerId)).thenReturn(defaultAddress);
        when(orderCartWriter.sumCartOutstanding(cartId)).thenReturn(totalOutstanding);
        when(orderFactory.handle(customer, defaultAddress, totalOutstanding))
                .thenReturn(orderBeforeSave);
        when(orderWriter.save(orderBeforeSave, cartId, customerId))
                .thenReturn(orderSaved);
        when(orderSaved.getOrderId()).thenReturn(orderId);
        when(orderSaved.getTotalOutstanding()).thenReturn(totalOutstanding);
        when(orderFactory.toDto(orderSaved)).thenReturn(dtoResponse);

        ArgumentCaptor<PlacedOrderEvent> eventCaptor =
                ArgumentCaptor.forClass(PlacedOrderEvent.class);

        // Act
        DTOOrderPlacedResponse result = orderPlacementExecutor.tryPlaceOrder(customerId, incomingAddress);

        // Assert result is the mapped DTO
        assertSame(dtoResponse, result);

        // Verify flow
        verify(auditedOrderValidation, times(1)).checkCustomerId(customerId);
        verify(domainLookupService, times(1))
                .getCustomerOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);
        verify(domainLookupService, times(1))
                .getCartOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE);
        verify(orderCartWriter, times(1)).lockCart(cartId);
        verify(auditedOrderValidation, times(1)).checkAddressDto(incomingAddress);
        verify(defaultAddressFactory, times(1)).handle(customerId);
        verify(orderCartWriter, times(1)).sumCartOutstanding(cartId);
        verify(auditedOrderValidation, times(1))
                .checkOutstanding(totalOutstanding, customerId);
        verify(orderFactory, times(1))
                .handle(customer, defaultAddress, totalOutstanding);
        verify(orderCartWriter, times(1))
                .reserveStock(cartId, customerId);
        verify(orderWriter, times(1))
                .save(orderBeforeSave, cartId, customerId);
        verify(applicationEventPublisher, times(1))
                .publishEvent(eventCaptor.capture());
        verify(orderFactory, times(1)).toDto(orderSaved);

        // Check event contents
        PlacedOrderEvent event = eventCaptor.getValue();
        assertEquals(customerId, event.customerId());
        assertEquals(orderId, event.orderId());
        assertEquals(totalOutstanding, event.total_outstanding());
        assertEquals("Euro", event.currency());
    }

    @Test
    void tryPlaceOrder_dblockFail_throw() {
        UUID customerId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        Cart cart = mock(Cart.class);

        when(domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE))
                .thenReturn(customer);
        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.ORDER_PLACE))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(orderCartWriter.lockCart(cartId)).thenReturn(false);

        assertThrows(
                CannotSerializeTransactionException.class,
                () -> orderPlacementExecutor.tryPlaceOrder(customerId, null)
        );

        verify(orderCartWriter, never()).sumCartOutstanding(any(UUID.class));
        verify(orderFactory, never()).handle(any(), any(), any());
        verify(orderWriter, never()).save(any(Order.class), any(UUID.class), any(UUID.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}