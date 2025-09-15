package commerce.eshop.core.web.controller;

import commerce.eshop.core.service.TransactionsService;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerce.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payout")
public class TransactionController {

    private final TransactionsService transactionsService;

    @Autowired
    public TransactionController(TransactionsService transactionsService){
        this.transactionsService = transactionsService;
    }

    // Process order
    ///curl -X POST "http://localhost:8080/api/v1/payout/3fe32988-08e3-4fe4-ab99-14288ea8609f/order/b76d4cad-5c11-4b04-b4ff-e882d5fbe41e?idemKey=abc-123" \
    ///   -H "Content-Type: application/json" \
    ///   -d '{
    ///     "instruction": {
    ///       "type": "USE_SAVED_METHOD",
    ///       "customerPaymentMethodId": "d41a102b-dda1-403a-9897-3eaeff6d0c49"
    ///     }
    ///   }'
    /// Or
    ///
    /// curl -X POST "http://localhost:8080/api/v1/payout/3fe32988-08e3-4fe4-ab99-14288ea8609f/order/b76d4cad-5c11-4b04-b4ff-e882d5fbe41e?idemKey=abc-123:RANDOM" \
    ///   -H "Content-Type: application/json" \
    ///   -d '{
    ///     "instruction": {
    ///       "type": "USE_NEW_CARD",
    ///       "panMasked": "555555******4444",
    ///       "brand": "MASTERCARD",
    ///       "expMonth": 11,
    ///       "expYear": 2029,
    ///       "holderName": "Test User",
    ///       "cvc": "321"
    ///     }
    ///   }'
    @PostMapping("/{customerId}/order/{orderId}")
    public DTOTransactionResponse pay(@PathVariable UUID customerId, @PathVariable UUID orderId, @RequestParam String idemKey, @RequestBody @Valid DTOTransactionRequest dto){
        return transactionsService.pay(customerId, orderId, idemKey, dto);
    }
}
