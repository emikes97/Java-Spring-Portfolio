package commerse.eshop.core.service;

import commerse.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerse.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;

import java.util.UUID;

public interface TransactionsService {

    public DTOTransactionResponse pay(UUID customerId, UUID orderId, String idemKey, DTOTransactionRequest dto);

 }
