package commerce.eshop.core.service.Impl;

import commerce.eshop.core.model.entity.*;
import commerce.eshop.core.repository.*;
import commerce.eshop.core.service.DomainLookupService;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.service.CustomerService;
import commerce.eshop.core.util.SortSanitizer;
import commerce.eshop.core.web.dto.requests.Customer.DTOCustomerCreateUser;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerCartItemResponse;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerOrderResponse;
import commerce.eshop.core.web.dto.response.Customer.DTOCustomerResponse;
import commerce.eshop.core.web.mapper.CustomerServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    // == Fields ==
    private final CustomerRepo customerRepo;
    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final PasswordEncoder passwordEncoder;
    private final SortSanitizer sortSanitizer;
    private final CentralAudit centralAudit;
    private final CustomerServiceMapper customerServiceMapper;
    private final WishlistRepo wishlistRepo;
    private final DomainLookupService domainLookupService;

    // == Whitelisting & Constraints ==

    /** For DTOCustomerResponse */
    public static final Map<String, String> CUSTOMER_PROFILE_SORT_WHITELIST = Map.ofEntries(
            Map.entry("username", "username"),
            Map.entry("name", "name"),
            Map.entry("surname", "surname"),
            Map.entry("created_at", "createdAt")
    );

    /** For DTOCustomerOrderResponse */
    public static final Map<String, String> CUSTOMER_ORDERS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("total_outstanding", "totalOutstanding"),
            Map.entry("created_at", "orderCreatedAt"),
            Map.entry("completed_at", "orderCompletedAt")
    );

    /** For DTOCustomerCartItemResponse */
    public static final Map<String, String> CUSTOMER_CART_ITEMS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("quantity", "quantity"),
            Map.entry("price_at", "priceAt"),
            Map.entry("added_at", "addedAt")
    );

    /** For DTOCustomerAddressResponse */
    public static final Map<String, String> CUSTOMER_ADDRESS_SORT_WHITELIST = Map.ofEntries(
            Map.entry("country", "country"),
            Map.entry("street", "street"),
            Map.entry("city", "city"),
            Map.entry("postal_code", "postalCode")
    );

    // == Constructors ==
    @Autowired
    public CustomerServiceImpl(CustomerRepo customerRepo, OrderRepo orderRepo, CartRepo cartRepo, CartItemRepo cartItemRepo,
                               PasswordEncoder passwordEncoder, SortSanitizer sortSanitizer, CentralAudit centralAudit,
                               CustomerServiceMapper customerServiceMapper, WishlistRepo wishlistRepo, DomainLookupService domainLookupService) {

        this.customerRepo = customerRepo;
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.passwordEncoder = passwordEncoder;
        this.centralAudit = centralAudit;
        this.sortSanitizer = sortSanitizer;
        this.customerServiceMapper = customerServiceMapper;
        this.wishlistRepo = wishlistRepo;
        this.domainLookupService = domainLookupService;
    }

    // == Public Methods ==

    @Transactional
    @Override
    public DTOCustomerResponse createUser(DTOCustomerCreateUser dto) {

        // 1) Hash password
        final String hashed = passwordEncoder.encode(dto.password());

        // 2) Create aggregate
        final Customer customer = new Customer(
                dto.phoneNumber(),
                dto.email(),
                dto.userName(),
                hashed,
                dto.name(),
                dto.surname()
        );

        // 3) Persist customer
        try {
            customerRepo.saveAndFlush(customer);
        } catch (DataIntegrityViolationException dup) {
            // No stable UUID yet → don't attach a null customerId in audit context
            log.warn("CREATE_USER failed (duplicate/constraint) email={} phone={}", dto.email(), dto.phoneNumber(), dup);
            throw centralAudit.audit(dup, null, EndpointsNameMethods.CREATE_USER, AuditingStatus.ERROR, dup.toString());
        }

        // 4) Create cart (same TX → atomic)
        final Cart cart = new Cart(customer);
        try {
            cartRepo.saveAndFlush(cart);
        } catch (DataIntegrityViolationException dup) {
            throw centralAudit.audit(dup, customer.getCustomerId(), EndpointsNameMethods.CREATE_USER, AuditingStatus.ERROR, dup.toString());
        }

        // 5) Create wishlist
        final  Wishlist wishlist = new Wishlist(customer);
        try {
            wishlistRepo.saveAndFlush(wishlist);
        } catch (DataIntegrityViolationException dup){
            throw centralAudit.audit(dup, customer.getCustomerId(), EndpointsNameMethods.CREATE_USER, AuditingStatus.ERROR, dup.toString());
        }

        // 6) Success
        centralAudit.info(customer.getCustomerId(), EndpointsNameMethods.CREATE_USER,
                AuditingStatus.SUCCESSFUL, AuditMessage.CREATE_USER_SUCCESS.getMessage());
        return customerServiceMapper.toDtoCustomerRes(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOCustomerResponse getProfile(UUID customerId) {

        if (customerId == null) {
            IllegalArgumentException bad = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(bad, null, EndpointsNameMethods.GET_PROFILE_BY_ID, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        final Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID);
        centralAudit.info(customerId, EndpointsNameMethods.GET_PROFILE_BY_ID,
                AuditingStatus.SUCCESSFUL, AuditMessage.GET_PROFILE_SUCCESS.getMessage());
        return customerServiceMapper.toDtoCustomerRes(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public DTOCustomerResponse getProfile(String phoneOrEmail) {
        requireNotBlank(phoneOrEmail, null, EndpointsNameMethods.GET_PROFILE_BY_SEARCH,
                "MISSING_IDENTIFIER", "Missing phone/email identifier.");

        final String key = phoneOrEmail.trim();
        final Customer customer = domainLookupService.getCustomerByPhoneOrEmailOrThrow(key, EndpointsNameMethods.GET_PROFILE_BY_SEARCH);

        centralAudit.info(customer.getCustomerId(), EndpointsNameMethods.GET_PROFILE_BY_SEARCH,
                AuditingStatus.SUCCESSFUL, AuditMessage.GET_PROFILE_SUCCESS.getMessage());

        return customerServiceMapper.toDtoCustomerRes(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOCustomerOrderResponse> getOrders(UUID customerId, Pageable pageable) {
        Pageable p = sortSanitizer.sanitize(pageable, CUSTOMER_ORDERS_SORT_WHITELIST, 25);
        Page<Order> orders = orderRepo.findByCustomer_CustomerId(customerId, p);
        centralAudit.info(customerId, EndpointsNameMethods.GET_ORDERS,
                AuditingStatus.SUCCESSFUL, AuditMessage.GET_ORDERS_SUCCESS.getMessage());
        return orders.map(customerServiceMapper::toDtoCustomerOrder);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DTOCustomerCartItemResponse> getCartItems(UUID customerId, Pageable pageable) {
        final Cart cart = domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.GET_CART_ITEMS);

        Pageable p = sortSanitizer.sanitize(pageable, CUSTOMER_CART_ITEMS_SORT_WHITELIST, 25);
        Page<CartItem> cartItems = cartItemRepo.findByCart_CartId(cart.getCartId(), p);

        centralAudit.info(customerId, EndpointsNameMethods.GET_CART_ITEMS,
                AuditingStatus.SUCCESSFUL, AuditMessage.GET_CART_ITEMS_SUCCESS.getMessage());
        return cartItems.map(customerServiceMapper::toDtoCartItem);
    }

    @Transactional
    @Override
    public void updateName(UUID customerId, String password, String name) {
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.UPDATE_NAME, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        requireNotBlank(name, customerId, EndpointsNameMethods.UPDATE_NAME, "INVALID_NAME", "Name must not be blank.");

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_NAME);
        verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_NAME);

        String trimmed = name.trim();
        if (trimmed.equals(customer.getName())) {
            centralAudit.info(customerId, EndpointsNameMethods.UPDATE_NAME, AuditingStatus.SUCCESSFUL, "NO_CHANGE_SAME_NAME");
            return;
        }

        customer.setName(trimmed);
        saveAndAudit(customer, customerId, EndpointsNameMethods.UPDATE_NAME, AuditMessage.UPDATE_NAME_SUCCESS.getMessage());
    }

    @Transactional
    @Override
    public void updateSurname(UUID customerId, String password, String lastName) {
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.UPDATE_SURNAME, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        requireNotBlank(lastName, customerId, EndpointsNameMethods.UPDATE_SURNAME, "INVALID_SURNAME", "Surname must not be blank.");
        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_SURNAME);
        verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_SURNAME);

        String trimmed = lastName.trim();
        if (trimmed.equals(customer.getSurname())) {
            centralAudit.info(customerId, EndpointsNameMethods.UPDATE_SURNAME, AuditingStatus.SUCCESSFUL, "NO_CHANGE_SAME_SURNAME");
            return;
        }

        customer.setSurname(trimmed);
        saveAndAudit(customer, customerId, EndpointsNameMethods.UPDATE_SURNAME, AuditMessage.UPDATE_SURNAME_SUCCESS.getMessage());
    }

    @Transactional
    @Override
    public void updateFullName(UUID customerId, String password, String name, String lastName) {
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal,null, EndpointsNameMethods.UPDATE_FULLNAME, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        requireNotBlank(name, customerId, EndpointsNameMethods.UPDATE_FULLNAME, "INVALID_FULLNAME", "Name must not be blank.");
        requireNotBlank(lastName, customerId, EndpointsNameMethods.UPDATE_FULLNAME, "INVALID_FULLNAME", "Surname must not be blank.");

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_FULLNAME);
        verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_FULLNAME);

        String newName = name.trim();
        String newSurname = lastName.trim();

        boolean changed = false;
        if (!newName.equals(customer.getName())) { customer.setName(newName); changed = true; }
        if (!newSurname.equals(customer.getSurname())) { customer.setSurname(newSurname); changed = true; }

        if (!changed) {
            centralAudit.info(customerId, EndpointsNameMethods.UPDATE_FULLNAME, AuditingStatus.SUCCESSFUL, "NO_CHANGE_SAME_FULLNAME");
            return;
        }

        saveAndAudit(customer, customerId, EndpointsNameMethods.UPDATE_FULLNAME, AuditMessage.UPDATE_FULLNAME_SUCCESS.getMessage());
    }

    @Transactional
    @Override
    public void updateUserName(UUID customerId, String password, String userName) {
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.UPDATE_USERNAME, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }
        requireNotBlank(userName, customerId, EndpointsNameMethods.UPDATE_USERNAME, "INVALID_USERNAME", "Username must not be blank.");

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_USERNAME);
        verifyPasswordOrThrow(customer, password, customerId, EndpointsNameMethods.UPDATE_USERNAME);

        String trimmed = userName.trim();
        if (trimmed.equals(customer.getUsername())) {
            centralAudit.info(customerId, EndpointsNameMethods.UPDATE_USERNAME, AuditingStatus.SUCCESSFUL, "NO_CHANGE_SAME_USERNAME");
            return;
        }

        customer.setUsername(trimmed);
        saveAndAudit(customer, customerId, EndpointsNameMethods.UPDATE_USERNAME, AuditMessage.UPDATE_USERNAME_SUCCESS.getMessage());
    }

    @Transactional
    @Override
    public void updateUserPassword(UUID customerId, String currentPassword, String newPassword) {
        if (customerId == null) {
            IllegalArgumentException illegal = new IllegalArgumentException("Missing customerId.");
            throw centralAudit.audit(illegal, null, EndpointsNameMethods.UPDATE_PASSWORD, AuditingStatus.WARNING, "MISSING_CUSTOMER_ID");
        }

        requireNotBlank(currentPassword, customerId, EndpointsNameMethods.UPDATE_PASSWORD, "INVALID_INPUT", "Missing current password.");
        requireNotBlank(newPassword,     customerId, EndpointsNameMethods.UPDATE_PASSWORD, "INVALID_INPUT", "Missing new password.");

        // Reject easy passwords
        if (newPassword.length() < 8) {
            IllegalArgumentException illegal = new IllegalArgumentException("Password too short.");
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.UPDATE_PASSWORD, AuditingStatus.WARNING, "WEAK_PASSWORD");
        }

        Customer customer = domainLookupService.getCustomerOrThrow(customerId, EndpointsNameMethods.UPDATE_PASSWORD);
        verifyPasswordOrThrow(customer, currentPassword, customerId, EndpointsNameMethods.UPDATE_PASSWORD);

        // Prevent reuse
        if (passwordEncoder.matches(newPassword, customer.getPasswordHash())) {
            IllegalArgumentException illegal = new IllegalArgumentException("New password must be different from current.");
            throw centralAudit.audit(illegal, customerId, EndpointsNameMethods.UPDATE_PASSWORD, AuditingStatus.WARNING, "REUSED_PASSWORD");
        }

        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        saveAndAudit(customer, customerId, EndpointsNameMethods.UPDATE_PASSWORD, AuditMessage.UPDATE_PASSWORD_SUCCESS.getMessage());
    }

    // == Private Methods ==

    /** Fail if the value is blank. Audits with WARNING and throws 400 (IllegalArgumentException). */
    private void requireNotBlank(String val, UUID cid, String endpoint, String code, String msg) {
        if (val == null || val.isBlank()) {
            throw centralAudit.audit(new IllegalArgumentException(msg), cid, endpoint, AuditingStatus.WARNING, code);
        }
    }

    /** Verify password or audit+throw 401 (BadCredentialsException). */
    private void verifyPasswordOrThrow(Customer c, String raw, UUID cid, String method) {
        if (raw == null || !passwordEncoder.matches(raw, c.getPasswordHash())) {
            throw centralAudit.audit(new BadCredentialsException("Invalid password"), cid, method, AuditingStatus.WARNING, "INVALID_PASSWORD");
        }
    }

    /** Save a customer with success/error auditing. */
    private void saveAndAudit(Customer c, UUID cid, String method, String successMsg) {
        try {
            customerRepo.saveAndFlush(c);
            centralAudit.info(cid, method, AuditingStatus.SUCCESSFUL, successMsg);
        } catch (DataIntegrityViolationException dup) {
            throw centralAudit.audit(dup, cid, method, AuditingStatus.ERROR, dup.toString());
        }
    }
}
