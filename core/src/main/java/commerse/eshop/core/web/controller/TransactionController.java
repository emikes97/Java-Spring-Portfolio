package commerse.eshop.core.web.controller;

import commerse.eshop.core.service.TransactionsService;
import commerse.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import commerse.eshop.core.web.dto.response.Transactions.DTOTransactionResponse;
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
    ///
    @PostMapping("/{customerId}/order/{orderId}")
    public DTOTransactionResponse pay(@PathVariable UUID customerId, @PathVariable UUID orderId, @RequestParam String idemKey, @RequestBody @Valid DTOTransactionRequest dto){
        return transactionsService.pay(customerId, orderId, idemKey, dto);
    }
}
