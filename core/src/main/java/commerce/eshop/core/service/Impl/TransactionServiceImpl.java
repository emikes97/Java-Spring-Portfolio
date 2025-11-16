package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.transaction.commands.PayOrder;
import commerce.eshop.core.service.TransactionsService;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionServiceImpl implements TransactionsService {

    // == Fields ==
    private final PayOrder payOrder;

    // == Constructors ==
    @Autowired
    public TransactionServiceImpl(PayOrder payOrder){
        this.payOrder = payOrder;
    }

    // == Public Methods ==
    @Override
    public DTOTransactionResponse pay(UUID customerId, UUID orderId, String idemKey, DTOTransactionRequest dto) {
        return payOrder.handle(customerId, orderId, idemKey, dto);
    }
}
