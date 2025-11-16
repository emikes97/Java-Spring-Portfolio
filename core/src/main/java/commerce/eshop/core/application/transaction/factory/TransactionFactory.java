package commerce.eshop.core.application.transaction.factory;

import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import commerce.eshop.core.web.mapper.TransactionServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TransactionFactory {

    // == Fields ==
    private final TransactionServiceMapper mapper;

    // == Constructors ==
    @Autowired
    public TransactionFactory(TransactionServiceMapper mapper) {
        this.mapper = mapper;
    }

    // == Public Methods ==
    public Transaction handle(DTOTransactionRequest dto, Order order, String customerId, String idemKey){
        Map<String, Object> snapshot = mapper.toSnapShot(dto.instruction());
        return new Transaction(order, customerId, snapshot, order.getTotalOutstanding(), idemKey);
    }

    public DTOTransactionResponse toDto(Transaction transaction){
        return mapper.toDto(transaction);
    }
}
