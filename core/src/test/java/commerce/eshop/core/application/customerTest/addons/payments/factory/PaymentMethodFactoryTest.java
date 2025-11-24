package commerce.eshop.core.application.customerTest.addons.payments.factory;

import commerce.eshop.core.application.customer.addons.payments.factory.PaymentMethodFactory;
import commerce.eshop.core.application.util.enums.TokenStatus;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PaymentMethodFactoryTest {

    private PaymentMethodFactory paymentMethodFactory;

    @BeforeEach
    void setUp() {
        paymentMethodFactory = new PaymentMethodFactory();
    }

    @Test
    void create() {
        Customer customer = mock(Customer.class);

        DTOAddPaymentMethod dto = new DTOAddPaymentMethod(
                "VISA",     // provider
                "CREDIT",   // brand
                "1234",     // last4
                (short) 2030,       // yearExp
                (short) 12,          // monthExp
                false            // is default
        );

        boolean makeDefault = true;

        CustomerPaymentMethod paymentMethod = paymentMethodFactory.create(dto, customer, makeDefault);

        assertNotNull(paymentMethod);
        assertSame(customer, paymentMethod.getCustomer());

        assertEquals("VISA", paymentMethod.getProvider());
        assertEquals("CREDIT", paymentMethod.getBrand());
        assertEquals("1234", paymentMethod.getLast4());
        assertEquals((short)2030, paymentMethod.getYearExp());
        assertEquals((short)12, paymentMethod.getMonthExp());

        assertTrue(paymentMethod.isDefault());

        assertEquals(TokenStatus.PENDING, paymentMethod.getTokenStatus());
        assertNull(paymentMethod.getProviderPaymentMethodToken());
    }
}