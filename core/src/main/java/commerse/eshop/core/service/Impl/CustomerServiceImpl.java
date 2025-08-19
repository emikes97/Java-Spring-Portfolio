package commerse.eshop.core.service.Impl;

import commerse.eshop.core.model.entity.Cart;
import commerse.eshop.core.model.entity.CartItem;
import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.Order;
import commerse.eshop.core.repository.CartItemRepo;
import commerse.eshop.core.repository.CartRepo;
import commerse.eshop.core.repository.CustomerRepo;
import commerse.eshop.core.repository.OrderRepo;
import commerse.eshop.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    @Override
    public Customer getProfile(UUID customerId) {
        return customerRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Customer was not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Order> getOrders(UUID customerId, Pageable pageable) {
        return orderRepo.findByCustomer_CustomerId(customerId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CartItem> getCartItems(UUID customerId, Pageable pageable) {
        Cart cart = cartRepo.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found for customer: " + customerId));
        return cartItemRepo.findByCart_CartId(cart.getCartId(), pageable);
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
}
