package commerce.eshop.core.application.customerTest.addons.payments.commands;

import commerce.eshop.core.application.customer.addons.payments.commands.UpdatePaymentInformation;
import commerce.eshop.core.application.customer.addons.payments.writer.PaymentMethodWriter;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdatePaymentInformationTest {

    private PaymentMethodWriter paymentMethodWriter;
    private DomainLookupService domainLookupService;
    private UpdatePaymentInformation updatePaymentInformation;

    @BeforeEach
    void setUp() {
        paymentMethodWriter = mock(PaymentMethodWriter.class);
        domainLookupService = mock(DomainLookupService.class);

        updatePaymentInformation = new UpdatePaymentInformation(paymentMethodWriter, domainLookupService);
    }

    @Test
    void handle_updatesAllFields_andMakesDefault() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        CustomerPaymentMethod paymentMethod = mock(CustomerPaymentMethod.class);
        CustomerPaymentMethod saved = mock(CustomerPaymentMethod.class);

        DTOUpdatePaymentMethod dto = new DTOUpdatePaymentMethod(
                "VISA",      // provider
                "CREDIT",    // brand
                "1234",      // last4
                (short) 2030,        // yearExp
                (short) 12,          // monthExp
                true         // isDefault
        );


        when(domainLookupService.getPaymentMethodOrThrow(customerId, paymentId, EndpointsNameMethods.PM_UPDATE))
                .thenReturn(paymentMethod);
        when(paymentMethodWriter.save(paymentMethod, EndpointsNameMethods.PM_UPDATE)).thenReturn(saved);

        CustomerPaymentMethod result = updatePaymentInformation.handle(customerId, paymentId, dto);

        assertSame(saved, result);

        verify(paymentMethod).setProvider("VISA");
        verify(paymentMethod).setBrand("CREDIT");
        verify(paymentMethod).setLast4("1234");
        verify(paymentMethod).setYearExp((short) 2030);
        verify(paymentMethod).setMonthExp((short) 12);

        verify(paymentMethodWriter).updateDefaultToFalse(customerId);
        verify(paymentMethod).setDefault(true);

        verify(paymentMethodWriter).save(paymentMethod, EndpointsNameMethods.PM_UPDATE);
    }

    @Test
    void handle_skipsBlankFields_andSetsDefaultFalse() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        CustomerPaymentMethod paymentMethod = mock(CustomerPaymentMethod.class);
        CustomerPaymentMethod saved = mock(CustomerPaymentMethod.class);

        DTOUpdatePaymentMethod dto = new DTOUpdatePaymentMethod(
                null,          // provider
                "",            // brand
                "   ",         // last4 blank
                null,          // yearExp
                null,          // monthExp
                false          // isDefault
        );

        when(domainLookupService.getPaymentMethodOrThrow(customerId, paymentId, EndpointsNameMethods.PM_UPDATE))
                .thenReturn(paymentMethod);
        when(paymentMethodWriter.save(paymentMethod, EndpointsNameMethods.PM_UPDATE)).thenReturn(saved);

        CustomerPaymentMethod result = updatePaymentInformation.handle(customerId, paymentId, dto);

        assertSame(saved, result);

        // No setters should be called
        verify(paymentMethod, never()).setProvider(anyString());
        verify(paymentMethod, never()).setBrand(anyString());
        verify(paymentMethod, never()).setLast4(anyString());
        verify(paymentMethod, never()).setYearExp(anyShort());
        verify(paymentMethod, never()).setMonthExp(anyShort());

        // Default = false
        verify(paymentMethodWriter, never()).updateDefaultToFalse(any());
        verify(paymentMethod).setDefault(false);

        verify(paymentMethodWriter).save(paymentMethod, EndpointsNameMethods.PM_UPDATE);
    }

    @Test
    void handle_domainThrows_propagatesException_andDoesNothingElse() {
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        DTOUpdatePaymentMethod dto = mock(DTOUpdatePaymentMethod.class);
        NoSuchElementException ex = new NoSuchElementException("not found");

        when(domainLookupService.getPaymentMethodOrThrow(customerId, paymentId, EndpointsNameMethods.PM_UPDATE))
                .thenThrow(ex);

        NoSuchElementException thrown = assertThrows(
                NoSuchElementException.class,
                () -> updatePaymentInformation.handle(customerId, paymentId, dto)
        );

        assertSame(ex, thrown);

        verify(paymentMethodWriter, never()).updateDefaultToFalse(any());
        verify(paymentMethodWriter, never()).save(any(), anyString());
    }
}