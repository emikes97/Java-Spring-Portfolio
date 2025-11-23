package commerse.eshop.core.application.cartTest.commands;

import commerce.eshop.core.application.cart.commands.AddCartItem;
import commerce.eshop.core.application.cart.factory.CartItemFactory;
import commerce.eshop.core.application.cart.validation.AuditedCartValidation;
import commerce.eshop.core.application.cart.writer.CartWriter;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AddCartItemTest {

    private AuditedCartValidation auditedCartValidation;
    private DomainLookupService domainLookupService;
    private CartWriter cartWriter;
    private CartItemFactory cartItemFactory;
    private AddCartItem addCartItem;

    @BeforeEach
    void setUp() {
        auditedCartValidation = mock(AuditedCartValidation.class);
        domainLookupService = mock(DomainLookupService.class);
        cartWriter = mock(CartWriter.class);
        cartItemFactory = mock(CartItemFactory.class);

        addCartItem = new AddCartItem(auditedCartValidation, domainLookupService, cartWriter, cartItemFactory);
    }

    @Test
    void handle_successInsert() {
        UUID customerId = UUID.randomUUID();
        long productId = 10L;
        int quantity = 3;
        Product product = mock(Product.class);
        Cart cart = mock(Cart.class);
        UUID cartId = UUID.randomUUID();
        CartItem newItem = mock(CartItem.class);
        CartItem savedItem = mock(CartItem.class);

        when(product.getProductName()).thenReturn("Laptop");
        when(product.getPrice()).thenReturn(BigDecimal.TEN);
        when(domainLookupService.getProductOrThrow(customerId, productId, EndpointsNameMethods.CART_ADD_ITEM))
                .thenReturn(product);
        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_ADD_ITEM))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(cartWriter.bumpQuantity(cartId, productId, quantity, 99, customerId))
                .thenReturn(0);
        when(cartItemFactory.newCartItem(
                eq(cart),
                eq(product),
                anyString(),
                eq(quantity),
                any()
        )).thenReturn(newItem);
        when(cartWriter.saveItem(newItem, 99)).thenReturn(savedItem);

        CartItem result = addCartItem.handle(customerId, productId, quantity);

        assertSame(savedItem, result);


        verify(auditedCartValidation, times(1))
                .checkValidQuantity(quantity, customerId, 99);
        verify(domainLookupService, times(1))
                .getProductOrThrow(customerId, productId, EndpointsNameMethods.CART_ADD_ITEM);
        verify(domainLookupService, times(1))
                .getCartOrThrow(customerId, EndpointsNameMethods.CART_ADD_ITEM);
        verify(cartWriter, times(1))
                .bumpQuantity(cartId, productId, quantity, 99, customerId);
        verify(cartItemFactory, times(1)).newCartItem(
                eq(cart),
                eq(product),
                anyString(),
                eq(quantity),
                any()
        );
        verify(cartWriter, times(1))
                .saveItem(newItem, 99);

        verify(domainLookupService, never())
                .getCartItemOrThrow(any(), anyLong(), any(), anyString());
    }

    @Test
    void handle_updatePath_returnsExistingItem() {
        UUID customerId = UUID.randomUUID();
        long productId = 20L;
        int quantity = 1;
        Product product = mock(Product.class);
        Cart cart = mock(Cart.class);
        UUID cartId = UUID.randomUUID();
        CartItem existingItem = mock(CartItem.class);

        when(domainLookupService.getProductOrThrow(customerId, productId, EndpointsNameMethods.CART_ADD_ITEM))
                .thenReturn(product);
        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_ADD_ITEM))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        // bumpQuantity returns > 0 → update path
        when(cartWriter.bumpQuantity(cartId, productId, quantity, 99, customerId))
                .thenReturn(1);
        when(domainLookupService.getCartItemOrThrow(
                cartId,
                productId,
                customerId,
                EndpointsNameMethods.CART_ADD_ITEM
        )).thenReturn(existingItem);

        CartItem result = addCartItem.handle(customerId, productId, quantity);

        assertSame(existingItem, result);

        verify(auditedCartValidation, times(1))
                .checkValidQuantity(quantity, customerId, 99);
        verify(domainLookupService, times(1))
                .getProductOrThrow(customerId, productId, EndpointsNameMethods.CART_ADD_ITEM);
        verify(domainLookupService, times(1))
                .getCartOrThrow(customerId, EndpointsNameMethods.CART_ADD_ITEM);
        verify(cartWriter, times(1))
                .bumpQuantity(cartId, productId, quantity, 99, customerId);
        // update path: should fetch the existing item, not create a new one
        verify(domainLookupService, times(1))
                .getCartItemOrThrow(cartId, productId, customerId, EndpointsNameMethods.CART_ADD_ITEM);
        verify(cartItemFactory, never())
                .newCartItem(any(), any(), anyString(), anyInt(), any());
        verify(cartWriter, never())
                .saveItem(any(CartItem.class), anyInt());
    }

    @Test
    void handle_invalidQuantity_throwsAndDoesNotCallDependencies() {
        UUID customerId = UUID.randomUUID();
        long productId = 30L;
        int quantity = 0;

        doThrow(new IllegalArgumentException("invalid qty"))
                .when(auditedCartValidation)
                .checkValidQuantity(quantity, customerId, 99);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> addCartItem.handle(customerId, productId, quantity)
        );

        assertEquals("invalid qty", ex.getMessage());

        // No further calls should be made if validation fails
        verify(domainLookupService, never()).getProductOrThrow(any(), anyLong(), anyString());
        verify(domainLookupService, never()).getCartOrThrow(any(), anyString());
        verify(cartWriter, never()).bumpQuantity(any(), anyLong(), anyInt(), anyInt(), any());
        verify(cartItemFactory, never()).newCartItem(any(), any(), anyString(), anyInt(), any());
        verify(cartWriter, never()).saveItem(any(CartItem.class), anyInt());
    }
}