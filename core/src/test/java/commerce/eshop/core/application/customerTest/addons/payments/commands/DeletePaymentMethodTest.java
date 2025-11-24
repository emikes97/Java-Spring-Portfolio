package commerce.eshop.core.application.customerTest.addons.payments.commands;

import commerce.eshop.core.application.customer.addons.payments.commands.DeletePaymentMethod;
import commerce.eshop.core.application.customer.addons.payments.writer.PaymentMethodWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeletePaymentMethodTest {

    private PaymentMethodWriter paymentMethodWriter;
    private DeletePaymentMethod deletePaymentMethod;

    @BeforeEach
    void setUp() {
        paymentMethodWriter = mock(PaymentMethodWriter.class);

        deletePaymentMethod = new DeletePaymentMethod(paymentMethodWriter);
    }

    @Test
    void handle_success() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        when(paymentMethodWriter.delete(customerId, paymentId)).thenReturn(1L);

        long result = deletePaymentMethod.handle(customerId, paymentId);

        assertEquals(1L, result);
        verify(paymentMethodWriter).delete(customerId, paymentId);
    }

    @Test
    void handle_writerThrows_exception() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        RuntimeException ex = new RuntimeException("delete failed");

        when(paymentMethodWriter.delete(customerId, paymentId)).thenThrow(ex);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> deletePaymentMethod.handle(customerId, paymentId)
        );

        assertSame(ex, thrown);
        verify(paymentMethodWriter).delete(customerId, paymentId);
    }
}