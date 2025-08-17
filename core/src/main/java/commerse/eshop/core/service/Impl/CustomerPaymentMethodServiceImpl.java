package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import commerse.eshop.core.repository.CustomerPaymentMethodRepo;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.service.CustomerPaymentMethodService;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOAddPaymentMethod;
import commerse.eshop.core.web.dto.requests.CustomerPaymentMethodRequests.DTOUpdatePaymentMethod;
import commerse.eshop.core.web.dto.response.PaymentMethod.DTOPaymentMethodResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class CustomerPaymentMethodServiceImpl implements CustomerPaymentMethodService {

    private final CustomerPaymentMethodRepo customerPaymentMethodRepo;
    private final CustomerRepo customerRepo;

    @Autowired
    public CustomerPaymentMethodServiceImpl(CustomerPaymentMethodRepo customerPaymentMethodRepo, CustomerRepo customerRepo){
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
        this.customerRepo = customerRepo;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOPaymentMethodResponse> getAllPaymentMethods(UUID customerId, Pageable pageable) {
        return customerPaymentMethodRepo.findByCustomer_CustomerId(customerId, pageable).map(this::toDto);
    }

    @Transactional
    @Override
    public DTOPaymentMethodResponse addPaymentMethod(UUID customerId, DTOAddPaymentMethod dto) {

        boolean makeDefault = Boolean.TRUE.equals(dto.isDefault());

        if (makeDefault){
            int outcome = customerPaymentMethodRepo.updateDefaultMethodToFalse(customerId);
            log.info("Updated tables={}", outcome);
        }

        CustomerPaymentMethod customerPaymentMethod = new CustomerPaymentMethod(customerRepo.getReferenceById(customerId),
                dto.provider(), dto.brand(), dto.last4(), dto.yearExp(), dto.monthExp(), makeDefault);

        customerPaymentMethodRepo.saveAndFlush(customerPaymentMethod);

        return toDto(customerPaymentMethod);
    }

    @Transactional
    @Override
    public DTOPaymentMethodResponse updatePaymentMethod(UUID customerId, UUID paymentMethodId, DTOUpdatePaymentMethod dto) {

        CustomerPaymentMethod paymentMethod = customerPaymentMethodRepo.findByCustomer_CustomerIdAndPaymentMethodId(
                customerId, paymentMethodId).orElseThrow(
                () -> new RuntimeException("The payment method doesn't exist"));

        if(dto.provider() != null && !dto.provider().isBlank())
            paymentMethod.setProvider(dto.provider());
        if(dto.brand() != null && !dto.brand().isBlank())
            paymentMethod.setBrand(dto.brand());
        if(dto.last4() != null && !dto.last4().isBlank())
            paymentMethod.setLast4(dto.last4());
        if(dto.yearExp() != null)
            paymentMethod.setYearExp(dto.yearExp());
        if(dto.monthExp() != null)
            paymentMethod.setMonthExp(dto.monthExp());

        if (dto.isDefault() != null){
            boolean makeDefault = Boolean.TRUE.equals(dto.isDefault());

            if(makeDefault)
            {
                customerPaymentMethodRepo.updateDefaultMethodToFalse(customerId);
                paymentMethod.setDefault(true);
            } else {
                paymentMethod.setDefault(false);
            }
        }

        customerPaymentMethodRepo.save(paymentMethod);

        return toDto(paymentMethod);
    }

    @Override
    public CustomerPaymentMethod retrievePaymentMethod(UUID customerId, UUID paymentMethodId) {
        return customerPaymentMethodRepo.findByCustomer_CustomerIdAndPaymentMethodId(customerId, paymentMethodId).orElseThrow(
                () -> new RuntimeException("The payment method doesn't exist.")
        );
    }

    @Override
    public void deletePaymentMethod(UUID customerId, UUID paymentId) {
        long outcome = customerPaymentMethodRepo.deleteByCustomer_CustomerIdAndPaymentMethodId(customerId, paymentId);

        if(outcome == 1){
            log.info("Payment method has been deleted={}", outcome);
        }
    }

    private DTOPaymentMethodResponse toDto(CustomerPaymentMethod p){
        return new DTOPaymentMethodResponse(
                p.getProvider(),
                p.getBrand(),
                p.getLast4(),
                p.getYearExp(),
                p.getMonthExp(),
                p.isDefault(),
                p.getCreatedAt()
        );}
}
