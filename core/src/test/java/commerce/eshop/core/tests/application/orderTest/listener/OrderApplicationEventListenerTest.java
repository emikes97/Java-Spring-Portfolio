//package commerce.eshop.core.tests.application.orderTest.listener;
//
//import commerce.eshop.core.application.email.EmailComposer;
//import commerce.eshop.core.application.events.email.EmailEventRequest;
//import commerce.eshop.core.application.events.order.CancelledOrderEvent;
//import commerce.eshop.core.application.events.order.PlacedOrderEvent;
//import commerce.eshop.core.application.infrastructure.DomainLookupService;
//import commerce.eshop.core.application.order.listener.OrderApplicationEventListener;
//import commerce.eshop.core.model.entity.Customer;
//import commerce.eshop.core.model.entity.Order;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.ApplicationEventPublisher;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class OrderApplicationEventListenerTest {
//
//    // == Fields ==
//    private EmailComposer emailComposer;
//    private ApplicationEventPublisher applicationEventPublisher;
//    private DomainLookupService domainLookupService;
//    private OrderApplicationEventListener orderApplicationEventListener;
//
//    @BeforeEach
//    void setUp() {
//        emailComposer = mock(EmailComposer.class);
//        applicationEventPublisher = mock(ApplicationEventPublisher.class);
//        domainLookupService = mock(DomainLookupService.class);
//
//        orderApplicationEventListener = new OrderApplicationEventListener(emailComposer, applicationEventPublisher, domainLookupService);
//    }
//
//    @Test
//    void onPlacedOrderEvent_success() {
//        UUID customerId = UUID.randomUUID();
//        UUID orderId = UUID.randomUUID();
//        BigDecimal totalOutstanding = new BigDecimal("42.50");
//        String currency = "Euro";
//        PlacedOrderEvent event = new PlacedOrderEvent(
//                customerId,
//                orderId,
//                totalOutstanding,
//                currency
//        );
//        Order order = mock(Order.class);
//        Customer customer = mock(Customer.class);
//        EmailEventRequest emailEvent = mock(EmailEventRequest.class);
//
//        when(domainLookupService.getOrderOrThrow(customerId, orderId, "Placeholder"))
//                .thenReturn(order);
//        when(domainLookupService.getCustomerOrThrow(customerId, "Placeholder"))
//                .thenReturn(customer);
//        when(emailComposer.orderConfirmed(customer, order, totalOutstanding, currency))
//                .thenReturn(emailEvent);
//
//        orderApplicationEventListener.on(event);
//
//        verify(domainLookupService, times(1))
//                .getOrderOrThrow(customerId, orderId, "Placeholder");
//        verify(domainLookupService, times(1))
//                .getCustomerOrThrow(customerId, "Placeholder");
//        verify(emailComposer, times(1))
//                .orderConfirmed(customer, order, totalOutstanding, currency);
//        verify(applicationEventPublisher, times(1))
//                .publishEvent(emailEvent);
//    }
//
//    @Test
//    void onCancelledOrderEvent_success() {
//        UUID customerId = UUID.randomUUID();
//        UUID orderId = UUID.randomUUID();
//        CancelledOrderEvent event = new CancelledOrderEvent(customerId, orderId);
//        Order order = mock(Order.class);
//        Customer customer = mock(Customer.class);
//        EmailEventRequest emailEvent = mock(EmailEventRequest.class);
//
//        when(domainLookupService.getOrderOrThrow(customerId, orderId, "Placeholder"))
//                .thenReturn(order);
//        when(domainLookupService.getCustomerOrThrow(customerId, "Placeholder"))
//                .thenReturn(customer);
//        when(emailComposer.orderCancelled(customer, order))
//                .thenReturn(emailEvent);
//
//        orderApplicationEventListener.on(event);
//
//        verify(domainLookupService, times(1))
//                .getOrderOrThrow(customerId, orderId, "Placeholder");
//        verify(domainLookupService, times(1))
//                .getCustomerOrThrow(customerId, "Placeholder");
//        verify(emailComposer, times(1))
//                .orderCancelled(customer, order);
//        verify(applicationEventPublisher, times(1))
//                .publishEvent(emailEvent);
//    }
//}