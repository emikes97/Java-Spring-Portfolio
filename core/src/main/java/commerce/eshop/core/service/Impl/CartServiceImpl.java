package commerce.eshop.core.service.Impl;

import commerce.eshop.core.application.cart.commands.AddCartItem;
import commerce.eshop.core.application.cart.commands.RemoveCartItem;
import commerce.eshop.core.application.cart.queries.CartQueries;
import commerce.eshop.core.model.entity.CartItem;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditMessage;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.service.CartService;
import commerce.eshop.core.web.dto.response.Cart.DTOCartItemResponse;
import commerce.eshop.core.web.mapper.CartServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class CartServiceImpl implements CartService {



    // == Fields ==
    private final AddCartItem addCartItem;
    private final RemoveCartItem removeCartItem;
    private final CartQueries cartQueries;
    private final CentralAudit centralAudit;
    private final CartServiceMapper cartServiceMapper;

    // == Constructors ==
    @Autowired
    public CartServiceImpl(AddCartItem addCartItem, RemoveCartItem removeCartItem, CartQueries cartQueries,
                           CentralAudit centralAudit,CartServiceMapper cartServiceMapper){
        this.addCartItem = addCartItem;
        this.removeCartItem = removeCartItem;
        this.cartQueries = cartQueries;
        this.centralAudit = centralAudit;
        this.cartServiceMapper = cartServiceMapper;
    }

    // == Public Methods ==
    @Override
    public Page<DTOCartItemResponse> viewAllCartItems(UUID customerId, Pageable pageable) {
        Page<CartItem> items = cartQueries.getPagedCartItems(customerId, pageable);
        centralAudit.info(customerId, EndpointsNameMethods.CART_VIEW_ALL, AuditingStatus.SUCCESSFUL, AuditMessage.CART_VIEW_ALL_SUCCESS.getMessage());
        return items.map(cartServiceMapper::toDto);
    }

    @Override
    public DTOCartItemResponse findItem(UUID customerId, long productId) {
        CartItem cartItem = cartQueries.getCartItem(customerId, productId);
        centralAudit.info(customerId, EndpointsNameMethods.CART_FIND_ITEM, AuditingStatus.SUCCESSFUL, AuditMessage.CART_FIND_ITEM_SUCCESS.getMessage());
        return cartServiceMapper.toDto(cartItem);
    }

    @Override
    public DTOCartItemResponse addCartItem(UUID customerId, long productId, int quantity) {
        final CartItem item = addCartItem.handle(customerId, productId, quantity);
        centralAudit.info(customerId, EndpointsNameMethods.CART_ADD_ITEM, AuditingStatus.SUCCESSFUL,
                AuditMessage.CART_ADD_ITEM_SUCCESS.getMessage());
        return cartServiceMapper.toDto(item);
    }

    @Override
    public void removeCartItem(UUID customerId, long productId, Integer quantity) {
        removeCartItem.handle(customerId, productId, quantity);
        centralAudit.info(customerId, EndpointsNameMethods.CART_REMOVE, AuditingStatus.SUCCESSFUL, AuditMessage.CART_REMOVE_SUCCESS.getMessage());
    }

    @Override
    public void clearCart(UUID customerId) {
        removeCartItem.handle(customerId);
        centralAudit.info(customerId, EndpointsNameMethods.CART_CLEAR, AuditingStatus.SUCCESSFUL, AuditMessage.CART_CLEAR_SUCCESS.getMessage());
    }
}
