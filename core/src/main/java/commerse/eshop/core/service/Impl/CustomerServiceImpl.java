package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.*;
import commerse.eshop.core.repository.CartItemRepo;
import commerse.eshop.core.repository.CartRepo;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.repository.OrderRepo;
import commerse.eshop.core.service.CustomerService;
import commerse.eshop.core.web.dto.requests.Customer.DTOCustomerCreateUser;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerAdResponse;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerCartItemResponse;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerOrderResponse;
import commerse.eshop.core.web.dto.response.Customer.DTOCustomerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerServiceImpl(CustomerRepo customerRepo, OrderRepo orderRepo, CartRepo cartRepo, CartItemRepo cartItemRepo, PasswordEncoder passwordEncoder) {
        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public DTOCustomerResponse createUser(DTOCustomerCreateUser dto) {

        // Hash the password
        String hashed = passwordEncoder.encode(dto.password());

        /// Create the user,
        Customer customer = new Customer(dto.phoneNumber(), dto.email(), dto.userName(), hashed, dto.name(), dto.surname());
        customerRepo.save(customer);
        Cart cart = new Cart(customer);
        cartRepo.save(cart);

        return toDto(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOCustomerResponse getProfile(UUID customerId) {
        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Customer was not found"));
        return toDto(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOCustomerResponse getProfile(String phoneOrEmail) {

        if (phoneOrEmail == null)
            throw new IllegalArgumentException("[Error] You can't request a search without the required identifications.");

        Customer customer = customerRepo.findByPhoneNumberOrEmail(phoneOrEmail).orElseThrow(() ->
                new NoSuchElementException("Customer not found for given phone/email"));
        return toDto(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOCustomerOrderResponse> getOrders(UUID customerId, Pageable pageable) {
        return orderRepo.findByCustomer_CustomerId(customerId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOCustomerCartItemResponse> getCartItems(UUID customerId, Pageable pageable) {
        Cart cart = cartRepo.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found for customer: " + customerId));
        return cartItemRepo.findByCart_CartId(cart.getCartId(), pageable).map(this::toDto);
    }

    @Transactional
    @Override
    public void updateName(UUID customerId, String password, String name) {

        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Customer was not found"));

        if(!passwordEncoder.matches(password, customer.getPasswordHash())){
            throw new RuntimeException("Invalid Password");
        }

        // Update Name
        customer.setName(name);
        customerRepo.save(customer);
    }

    @Transactional
    @Override
    public void updateSurname(UUID customerId, String password, String lastName) {

        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Customer was not found"));

        if(!passwordEncoder.matches(password, customer.getPasswordHash())){
            throw new RuntimeException("Invalid Password");
        }

        // Update Name
        customer.setSurname(lastName);
        customerRepo.save(customer);
    }

    @Transactional
    @Override
    public void updateFullName(UUID customerId, String password, String name, String lastName) {
        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Customer was not found"));

        if(!passwordEncoder.matches(password, customer.getPasswordHash())){
            throw new RuntimeException("Invalid Password");
        }

        // Update Name
        customer.setName(name);
        customer.setSurname(lastName);
        customerRepo.save(customer);
    }

    @Transactional
    @Override
    public void updateUserName(UUID customerId, String password, String userName) {
        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Customer was not found"));

        if(!passwordEncoder.matches(password, customer.getPasswordHash())){
            throw new RuntimeException("Invalid Password");
        }

        // Update Name
        customer.setUsername(userName);
        customerRepo.save(customer);
    }

    @Transactional
    @Override
    public void updateUserPassword(UUID customerId, String currentPassword, String newPassword) {

        Customer customer = customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));

        if(!passwordEncoder.matches(currentPassword, customer.getPasswordHash())){
            throw new RuntimeException("Invalid Password");
        }

        if(passwordEncoder.matches(newPassword, customer.getPasswordHash())){
            throw new RuntimeException("New password must be different from the current password");
        }

        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepo.save(customer);
    }

    ///  Private methods / Helpers to map to DTOs

    private DTOCustomerResponse toDto(Customer c){
        return new DTOCustomerResponse(
                c.getCustomerId(),
                c.getPhoneNumber(),
                c.getEmail(),
                c.getUsername(),
                c.getName(),
                c.getSurname(),
                c.getCreatedAt()
        );
    }

    @SuppressWarnings("unchecked")
    private DTOCustomerOrderResponse toDto(Order o){
        var addrDto = toDtoFromJson((Map<String, Object>) o.getAddressToSend());
        return new DTOCustomerOrderResponse(
                o.getOrderId(),
                o.getCustomer().getCustomerId(),
                o.getTotalOutstanding(),
                addrDto,
                o.getCreatedAt(),
                o.getCompletedAt()
        );
    }

    private DTOCustomerCartItemResponse toDto(CartItem ci){
        return new DTOCustomerCartItemResponse(
                ci.getCartItemId(),
                ci.getCart().getCartId(),
                ci.getProduct().getProductId(),
                ci.getProductName(),
                ci.getQuantity(),
                ci.getPriceAt(),
                ci.getAddedAt());
    }

    private DTOCustomerAdResponse toDtoFromJson(Map<String, Object> a){
        return new DTOCustomerAdResponse(
                (String) a.get("country"),
                (String) a.get("street"),
                (String) a.get("city"),
                (String) a.get("postalCode")
        );
    }
}
