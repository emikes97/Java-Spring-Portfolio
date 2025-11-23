package commerse.eshop.core.application.cartTest.commands;

import commerce.eshop.core.application.cart.commands.RemoveCartItem;
import commerce.eshop.core.application.cart.validation.AuditedCartValidation;
import commerce.eshop.core.application.cart.writer.CartWriter;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.model.entity.Cart;
import commerce.eshop.core.model.entity.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RemoveCartItemTest {

    private CartWriter cartWriter;
    private DomainLookupService domainLookupService;
    private AuditedCartValidation auditedCartValidation;
    private RemoveCartItem removeCartItem;

    @BeforeEach
    void setUp() {
        cartWriter = mock(CartWriter.class);
        domainLookupService = mock(DomainLookupService.class);
        auditedCartValidation = mock(AuditedCartValidation.class);

        removeCartItem = new RemoveCartItem(cartWriter, domainLookupService, auditedCartValidation);
    }

    @Test
    void handle_successQuantityNull_clearWholeItem() {
        UUID customerId = UUID.randomUUID();
        long productId = 10L;
        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);
        UUID cartId = UUID.randomUUID();

        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_REMOVE))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(domainLookupService.getCartItemOrThrow(cartId, productId, customerId, EndpointsNameMethods.CART_REMOVE))
                .thenReturn(item);

        // quantity NULL
        removeCartItem.handle(customerId, productId, null);

        verify(cartWriter, times(1))
                .delete(cart, productId, EndpointsNameMethods.CART_REMOVE);
        verify(auditedCartValidation, never()).checkValidQuantity(anyInt(), any());
        verify(cartWriter, never()).save(any(), any(), anyString());
    }

    @Test
    void handle_deleteWholeItem_quantityGreaterOrEqual() {
        UUID customerId = UUID.randomUUID();
        long productId = 20L;
        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);
        UUID cartId = UUID.randomUUID();

        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_REMOVE))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(domainLookupService.getCartItemOrThrow(cartId, productId, customerId, EndpointsNameMethods.CART_REMOVE))
                .thenReturn(item);
        when(item.getQuantity()).thenReturn(5);

        // quantity >= existing → delete
        removeCartItem.handle(customerId, productId, 5);

        verify(cartWriter, times(1))
                .delete(cart, productId, EndpointsNameMethods.CART_REMOVE);
        verify(auditedCartValidation, never()).checkValidQuantity(anyInt(), any());
        verify(cartWriter, never()).save(any(), any(), anyString());
    }

    @Test
    void handle_partialRemove_success() {
        UUID customerId = UUID.randomUUID();
        long productId = 30L;
        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);
        UUID cartId = UUID.randomUUID();

        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_REMOVE))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(domainLookupService.getCartItemOrThrow(cartId, productId, customerId, EndpointsNameMethods.CART_REMOVE))
                .thenReturn(item);
        when(item.getQuantity()).thenReturn(10);

        removeCartItem.handle(customerId, productId, 3);

        verify(auditedCartValidation, times(1))
                .checkValidQuantity(3, customerId);
        // quantity must be updated
        verify(item, times(1)).setQuantity(10 - 3);
        verify(cartWriter, times(1))
                .save(item, customerId, EndpointsNameMethods.CART_REMOVE);
        verify(cartWriter, never())
                .delete(any(), anyLong(), anyString());
    }


    @Test
    void handle_partialRemove_invalidPath_throw() {
        UUID customerId = UUID.randomUUID();
        long productId = 40L;
        Cart cart = mock(Cart.class);
        CartItem item = mock(CartItem.class);
        UUID cartId = UUID.randomUUID();

        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_REMOVE))
                .thenReturn(cart);
        when(cart.getCartId()).thenReturn(cartId);
        when(domainLookupService.getCartItemOrThrow(cartId, productId, customerId, EndpointsNameMethods.CART_REMOVE))
                .thenReturn(item);
        when(item.getQuantity()).thenReturn(10);

        int quantity = 0; // <--- partial path (10 > 0), but invalid for validation

        doThrow(new IllegalArgumentException("invalid qty"))
                .when(auditedCartValidation)
                .checkValidQuantity(quantity, customerId);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> removeCartItem.handle(customerId, productId, quantity)
        );

        assertEquals("invalid qty", ex.getMessage());

        verify(cartWriter, never()).delete(any(), anyLong(), anyString());
        verify(cartWriter, never()).save(any(), any(), anyString());
    }

    @Test
    void handle_clearCart() {
        UUID customerId = UUID.randomUUID();
        Cart cart = mock(Cart.class);

        when(domainLookupService.getCartOrThrow(customerId, EndpointsNameMethods.CART_CLEAR))
                .thenReturn(cart);

        removeCartItem.handle(customerId);

        verify(cartWriter, times(1))
                .clear(cart, EndpointsNameMethods.CART_CLEAR);
        verify(domainLookupService, times(1))
                .getCartOrThrow(customerId, EndpointsNameMethods.CART_CLEAR);
    }
}