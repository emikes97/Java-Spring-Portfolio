package commerce.eshop.core.tests.application.orderTest.commands;

import commerce.eshop.core.application.order.commands.PlaceOrder;
import commerce.eshop.core.application.order.orchestrator.OrderPlacementExecutor;
import commerce.eshop.core.web.dto.requests.Order.DTOOrderCustomerAddress;
import commerce.eshop.core.web.dto.response.Order.DTOOrderPlacedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.dao.DeadlockLoserDataAccessException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaceOrderTest {

    // == Fields ==
    private OrderPlacementExecutor orderPlacementExecutor;
    private PlaceOrder placeOrder;

    @BeforeEach
    void setUp() {
        orderPlacementExecutor = mock(OrderPlacementExecutor.class);

        placeOrder = new PlaceOrder(orderPlacementExecutor);
    }

    @Test
    void handle_success() {
        UUID customerId = UUID.randomUUID();
        DTOOrderCustomerAddress addressDto = mock(DTOOrderCustomerAddress.class);
        DTOOrderPlacedResponse response = mock(DTOOrderPlacedResponse.class);

        when(orderPlacementExecutor.tryPlaceOrder(customerId, addressDto)).thenReturn(response);

        DTOOrderPlacedResponse result = placeOrder.handle(customerId, addressDto);

        assertSame(response, result);
        verify(orderPlacementExecutor, times(1)).tryPlaceOrder(customerId, addressDto);
    }

    @Test
    void handle_failOnRetries_andThenSuccess() {
        UUID customerId = UUID.randomUUID();
        DTOOrderCustomerAddress addressDto = mock(DTOOrderCustomerAddress.class);
        DTOOrderPlacedResponse response = mock(DTOOrderPlacedResponse.class);

        CannotSerializeTransactionException ex1 =
                new CannotSerializeTransactionException("serialization failure");
        DeadlockLoserDataAccessException ex2 =
                new DeadlockLoserDataAccessException("deadlock", null);

        when(orderPlacementExecutor.tryPlaceOrder(customerId, addressDto))
                .thenThrow(ex1)       // 1st attempt
                .thenThrow(ex2)       // 2nd attempt
                .thenReturn(response); // 3rd attempt

        DTOOrderPlacedResponse result = placeOrder.handle(customerId, addressDto);

        assertSame(response, result);
        verify(orderPlacementExecutor, times(3)).tryPlaceOrder(customerId, addressDto);
    }


    @Test
    void handle_exceedsMaxRetries_throw() {
        UUID customerId = UUID.randomUUID();
        DTOOrderCustomerAddress addressDto = mock(DTOOrderCustomerAddress.class);

        CannotSerializeTransactionException ex =
                new CannotSerializeTransactionException("always failing");

        when(orderPlacementExecutor.tryPlaceOrder(customerId, addressDto))
                .thenThrow(ex); // every attempt throws the same

        CannotSerializeTransactionException thrown = assertThrows(
                CannotSerializeTransactionException.class,
                () -> placeOrder.handle(customerId, addressDto)
        );

        assertSame(ex, thrown);
        // 5 attempts max
        verify(orderPlacementExecutor, times(5)).tryPlaceOrder(customerId, addressDto);
    }
}