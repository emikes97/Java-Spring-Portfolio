package commerce.eshop.core.tests.application.transactionTest.factory;

import commerce.eshop.core.application.transaction.factory.TransactionFactory;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.PaymentInstruction;
import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.UseNewCard;
import commerce.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import commerce.eshop.core.web.mapper.TransactionServiceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionFactoryTest {

    // == Fields ==
    private TransactionServiceMapper transactionServiceMapper;
    private TransactionFactory transactionFactory;

    // == Tests ==

    @BeforeEach
    void setUp() {
        transactionServiceMapper = mock(TransactionServiceMapper.class);

        transactionFactory = new TransactionFactory(transactionServiceMapper);
    }

    @Test
    void handle_success() {
        String customerId = "cust-123";
        String idemKey = "idem-456";
        DTOTransactionRequest dto = mock(DTOTransactionRequest.class);
        Order order = mock(Order.class);
        // PaymentInstruction instruction = mock(PaymentInstruction.class.); -> blows up
        // Object dummyInstruction = new Object(); -> this doesn't work either as it expects type PaymentInstruction
        // Instance a dummy UseNewCard to verify the test.
        UseNewCard dummyInstruction = new UseNewCard("4111","VISA",12, 30, "Mike", "443");
        Map<String, Object> snapshot = Map.of("key", "value");

        when(dto.instruction()).thenReturn(dummyInstruction);
        when(transactionServiceMapper.toSnapShot(dummyInstruction)).thenReturn(snapshot);

        Transaction tx = transactionFactory.handle(dto, order, customerId, idemKey);

        assertNotNull(tx);
        verify(transactionServiceMapper, times(1)).toSnapShot(dummyInstruction);
    }

    @Test
    void toDto_success() {
        Transaction tx = mock(Transaction.class);
        DTOTransactionResponse response = mock(DTOTransactionResponse.class);

        when(transactionServiceMapper.toDto(tx)).thenReturn(response);

        DTOTransactionResponse result = transactionFactory.toDto(tx);

        assertSame(response, result);
        verify(transactionServiceMapper, times(1)).toDto(tx);
    }
}