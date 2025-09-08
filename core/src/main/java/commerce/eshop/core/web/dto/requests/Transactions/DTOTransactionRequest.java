package commerce.eshop.core.web.dto.requests.Transactions;

import commerce.eshop.core.web.dto.requests.Transactions.PaymentVariants.PaymentInstruction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DTOTransactionRequest(@NotNull @Valid PaymentInstruction instruction) {}
