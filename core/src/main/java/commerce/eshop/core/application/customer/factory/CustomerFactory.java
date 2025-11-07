package commerce.eshop.core.application.customer.factory;


import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.web.dto.requests.Customer.DTOCustomerCreateUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerFactory {

    // == Fields ==
    private final PasswordEncoder passwordEncoder;

    // == Constructors ==
    public CustomerFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // == Public Methods ==
    public Customer createFrom(DTOCustomerCreateUser dto){

        final String hashed = passwordEncoder.encode(dto.password());
        final Customer customer = new Customer( dto.phoneNumber(), dto.email(), dto.userName(), hashed, dto.name(), dto.surname() );

        return customer;
    }

}
