package commerce.eshop.core.tests.application.orderTest.commands;

import commerce.eshop.core.application.events.order.CancelledOrderEvent;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.order.commands.CancelOrder;
import commerce.eshop.core.application.order.validation.AuditedOrderValidation;
import commerce.eshop.core.application.order.writer.OrderWriter;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.model.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancelOrderTest {

    // == Fields ==
    private AuditedOrderValidation auditedOrderValidation;
    private DomainLookupService domainLookupService;
    private OrderWriter orderWriter;
    private ApplicationEventPublisher applicationEventPublisher;
    private CancelOrder cancelOrder;

    @BeforeEach
    void setUp() {
        auditedOrderValidation = mock(AuditedOrderValidation.class);
        domainLookupService = mock(DomainLookupService.class);
        orderWriter = mock(OrderWriter.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        cancelOrder = new CancelOrder(auditedOrderValidation, domainLookupService, orderWriter, applicationEventPublisher);
    }

    @Test
    void handle_happyPath_cancelsOrderRestoresStockAndPublishesEvent() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID savedOrderId = UUID.randomUUID();
        Order order = mock(Order.class);
        Order savedOrder = mock(Order.class);
        int expectedItems = 3;
        int updatedItems = 3;

        when(domainLookupService.getOrderOrThrow(customerId, orderId, EndpointsNameMethods.ORDER_CANCEL))
                .thenReturn(order);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING_PAYMENT);
        when(order.getOrderId()).thenReturn(orderId);
        when(orderWriter.countOrderItems(orderId)).thenReturn(expectedItems);
        when(orderWriter.restoreProductStock(orderId)).thenReturn(updatedItems);
        when(orderWriter.save(order, customerId, EndpointsNameMethods.ORDER_CANCEL))
                .thenReturn(savedOrder);
        when(savedOrder.getOrderId()).thenReturn(savedOrderId);

        cancelOrder.handle(customerId, orderId);

        verify(auditedOrderValidation, times(1))
                .checkCustomerAndOrder(customerId, orderId);
        verify(auditedOrderValidation, times(1))
                .checkOrderStatus(OrderStatus.PENDING_PAYMENT, customerId);
        verify(auditedOrderValidation, times(1))
                .checkExpectedUpdated(expectedItems, customerId);
        verify(auditedOrderValidation, times(1))
                .checkRestockUpdate(updatedItems, expectedItems, customerId);
        verify(domainLookupService, times(1))
                .getOrderOrThrow(customerId, orderId, EndpointsNameMethods.ORDER_CANCEL);
        verify(orderWriter, times(1)).countOrderItems(orderId);
        verify(orderWriter, times(1)).restoreProductStock(orderId);
        verify(order, times(1)).setOrderStatus(OrderStatus.CANCELLED);
        verify(orderWriter, times(1))
                .save(order, customerId, EndpointsNameMethods.ORDER_CANCEL);

        ArgumentCaptor<CancelledOrderEvent> eventCaptor =
                ArgumentCaptor.forClass(CancelledOrderEvent.class);

        verify(applicationEventPublisher, times(1))
                .publishEvent(eventCaptor.capture());

        CancelledOrderEvent event = eventCaptor.getValue();
        assertEquals(customerId, event.customerId());
        assertEquals(savedOrderId, event.orderId());
    }
}