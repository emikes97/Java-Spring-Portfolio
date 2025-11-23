package commerse.eshop.core.application.cartTest.writer;

import commerce.eshop.core.application.cart.writer.CartWriter;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Product;
import commerce.eshop.core.repository.CartItemRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CartWriterTest {

    private CartItemRepo cartItemRepo;
    private CentralAudit centralAudit;
    private CartWriter cartWriter;

    @BeforeEach
    void setUp() {
        cartItemRepo = mock(CartItemRepo.class);
        centralAudit = mock(CentralAudit.class);

        cartWriter = new CartWriter(cartItemRepo, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void bumpQuantity_successPath() {
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        long productId = 10L;
        int inc = 3;
        int maxQty = 20;

        when(cartItemRepo.bumpQuantity(cartId, productId, inc, maxQty)).thenReturn(1);

        int rows = cartWriter.bumpQuantity(cartId, productId, inc, maxQty, customerId);

        assertEquals(1, rows);
        verify(cartItemRepo, times(1)).bumpQuantity(cartId, productId, inc, maxQty);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void bumpQuantity_dataIntegrityViolationPath() {
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        long productId = 10L;
        int inc = 3;
        int maxQty = 20;
        DataIntegrityViolationException dup =
                new DataIntegrityViolationException("duplicate key");

        when(cartItemRepo.bumpQuantity(cartId, productId, inc, maxQty))
                .thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> cartWriter.bumpQuantity(cartId, productId, inc, maxQty, customerId)
        );

        assertSame(dup, ex);

        verify(centralAudit, times(1)).audit(
                any(DataIntegrityViolationException.class),
                eq(customerId),
                eq(EndpointsNameMethods.CART_ADD_ITEM),
                eq(AuditingStatus.ERROR),
                anyString()
        );
    }

    @Test
    void saveItem_successPath() {
        CartItem item = mock(CartItem.class);
        int maxQty = 10;

        when(cartItemRepo.saveAndFlush(item)).thenReturn(item);

        CartItem result = cartWriter.saveItem(item, maxQty);

        assertSame(item, result);
        verify(cartItemRepo, times(1)).saveAndFlush(item);
        verify(cartItemRepo, never()).bumpQuantity(any(), anyLong(), anyInt(), anyInt());
        verify(cartItemRepo, never()).getCartItemByCartIdAndProductId(any(), anyLong());
        verifyNoInteractions(centralAudit);
    }

    @Test
    void saveItem_dataIntegrityViolation_bumpsQuantityAndLoadsExistingItem(){
        CartItem newItem = mock(CartItem.class);
        Cart cart = mock(Cart.class);
        Product product = mock(Product.class);
        UUID cartId = UUID.randomUUID();
        long productId = 99L;
        int qty = 3;
        int maxQty = 10;
        CartItem existingItem = mock(CartItem.class);
        DataIntegrityViolationException dup = new DataIntegrityViolationException("duplicate");

        when(newItem.getCart()).thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(newItem.getProduct()).thenReturn(product);
        when(product.getProductId()).thenReturn(productId);
        when(newItem.getQuantity()).thenReturn(qty);
        when(cartItemRepo.saveAndFlush(newItem)).thenThrow(dup);
        when(cartItemRepo.getCartItemByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.of(existingItem));

        CartItem result = cartWriter.saveItem(newItem, maxQty);

        assertSame(existingItem, result);

        verify(cartItemRepo, times(1))
                .saveAndFlush(newItem);
        verify(cartItemRepo, times(1))
                .bumpQuantity(cartId, productId, qty, maxQty);
        verify(cartItemRepo, times(1))
                .getCartItemByCartIdAndProductId(cartId, productId);

        verifyNoInteractions(centralAudit);
    }

    @Test
    void save_successPath_savesAndReturnsItem() {
        CartItem item = mock(CartItem.class);
        UUID customerId = UUID.randomUUID();
        String endpoint = "EP";

        when(cartItemRepo.saveAndFlush(item)).thenReturn(item);

        CartItem result = cartWriter.save(item, customerId, endpoint);

        assertSame(item, result);
        verify(cartItemRepo, times(1)).saveAndFlush(item);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void save_dataIntegrityViolationPath() {
        CartItem item = mock(CartItem.class);
        UUID customerId = UUID.randomUUID();
        String endpoint = "EP";

        mockAuditReturnSame(centralAudit);

        DataIntegrityViolationException dup =
                new DataIntegrityViolationException("duplicate");

        when(cartItemRepo.saveAndFlush(item)).thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> cartWriter.save(item, customerId, endpoint)
        );

        assertSame(dup, ex);

        verify(centralAudit, times(1)).audit(
                any(DataIntegrityViolationException.class),
                eq(customerId),
                eq(endpoint),
                eq(AuditingStatus.ERROR)
        );
    }

    @Test
    void clear_cartHasItems_successPath() {
        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String endpoint = "EP";

        when(cart.getCartId()).thenReturn(cartId);
        when(cart.getCustomer()).thenReturn(customer);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(cartItemRepo.clearCart(cartId)).thenReturn(3);

        cartWriter.clear(cart, endpoint);

        verify(cartItemRepo, times(1)).clearCart(cartId);
        verify(centralAudit, never()).warn(any(), anyString(), any(), anyString());
    }

    @Test
    void clear_cartAlreadyEmptyPath(){
        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String endpoint = "EP";

        when(cart.getCartId()).thenReturn(cartId);
        when(cart.getCustomer()).thenReturn(customer);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(cartItemRepo.clearCart(cartId)).thenReturn(0);

        assertDoesNotThrow(() -> cartWriter.clear(cart, endpoint));

        verify(cartItemRepo, times(1)).clearCart(cartId);
        verify(centralAudit, times(1)).warn(
                eq(customerId),
                eq(endpoint),
                eq(AuditingStatus.WARNING),
                eq("CART_IS_ALREADY_EMPTY")
        );
    }

    @Test
    void delete_itemExists_deletesRowAndDoesNotAudit() {
        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        long productId = 25L;
        String endpoint = "EP";

        when(cart.getCartId()).thenReturn(cartId);
        when(cart.getCustomer()).thenReturn(customer);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(cartItemRepo.deleteItemByCartIdAndProductId(cartId, productId))
                .thenReturn(1);

        assertDoesNotThrow(() -> cartWriter.delete(cart, productId, endpoint));

        verify(cartItemRepo, times(1))
                .deleteItemByCartIdAndProductId(cartId, productId);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void delete_itemMissingPath(){
        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);

        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        long productId = 50L;
        String endpoint = "EP";

        when(cart.getCartId()).thenReturn(cartId);
        when(cart.getCustomer()).thenReturn(customer);
        when(customer.getCustomerId()).thenReturn(customerId);
        when(cartItemRepo.deleteItemByCartIdAndProductId(cartId, productId))
                .thenReturn(0);

        mockAuditReturnSame(centralAudit);

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> cartWriter.delete(cart, productId, endpoint)
        );

        assertTrue(ex.getMessage().contains("Cart item not found for productId=" + productId));

        verify(cartItemRepo, times(1))
                .deleteItemByCartIdAndProductId(cartId, productId);
        verify(centralAudit, times(1)).audit(
                any(NoSuchElementException.class),
                eq(customerId),
                eq(endpoint),
                eq(AuditingStatus.WARNING),
                anyString()
        );
    }

        // == Private Method ==
    static <E extends RuntimeException> void mockAuditReturnSame(CentralAudit auditMock) {
        // 5-arg overload
        when(auditMock.audit(
                any(RuntimeException.class),
                any(UUID.class),
                anyString(),
                any(AuditingStatus.class),
                anyString()
        )).thenAnswer(inv -> inv.getArgument(0));

        // 4-arg overload
        when(auditMock.audit(
                any(RuntimeException.class),
                any(UUID.class),
                anyString(),
                any(AuditingStatus.class)
        )).thenAnswer(inv -> inv.getArgument(0));
    }
}