package commerce.eshop.core.tests.application.wishlistTest.writer;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.wishlist.writer.WishlistWriter;
import commerce.eshop.core.model.entity.Wishlist;
import commerce.eshop.core.model.entity.WishlistItem;
import commerce.eshop.core.repository.WishlistItemRepo;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WishlistWriterTest {

    // == Fields ==
    private WishlistWriter wishlistWriter;
    private CentralAudit centralAudit;
    private WishlistItemRepo wishlistItemRepo;

    // == Tests ==

    @BeforeEach
    void setUp() {
        this.centralAudit = mock(CentralAudit.class);
        this.wishlistItemRepo = mock(WishlistItemRepo.class);

        wishlistWriter = new WishlistWriter(wishlistItemRepo, centralAudit);
        mockAuditReturnSame(centralAudit);
    }

    @Test
    void save_success() {
        UUID customerId = UUID.randomUUID();
        WishlistItem item = mock(WishlistItem.class);
        WishlistItem persisted = mock(WishlistItem.class);

        when(wishlistItemRepo.saveAndFlush(item)).thenReturn(persisted);

        WishlistItem result = wishlistWriter.save(item, customerId, "EP");

        assertSame(persisted, result);
        verify(wishlistItemRepo, times(1)).saveAndFlush(item);
        verifyNoInteractions(centralAudit);
    }

    @Test
    void save_duplicate_throw() {
        UUID customerId = UUID.randomUUID();
        WishlistItem item = mock(WishlistItem.class);
        DataIntegrityViolationException dup =
                new DataIntegrityViolationException("duplicate");

        when(wishlistItemRepo.saveAndFlush(item)).thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> wishlistWriter.save(item, customerId, "EP")
        );

        assertSame(dup, ex);

        verify(centralAudit, times(1)).audit(
                eq(dup),
                eq(customerId),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
    }

    @Test
    void delete_success() {
        UUID customerId = UUID.randomUUID();
        WishlistItem item = mock(WishlistItem.class);

        wishlistWriter.delete(item, customerId, "EP");

        verify(wishlistItemRepo, times(1)).delete(item);
        verify(wishlistItemRepo, times(1)).flush();
        verifyNoInteractions(centralAudit);
    }

    @Test
    void delete_duplicate_throws() {
        UUID customerId = UUID.randomUUID();
        WishlistItem item = mock(WishlistItem.class);
        DataIntegrityViolationException dup =
                new DataIntegrityViolationException("delete fail");

        doThrow(dup).when(wishlistItemRepo).delete(item);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> wishlistWriter.delete(item, customerId, "EP")
        );

        assertSame(dup, ex);

        verify(centralAudit, times(1)).audit(
                eq(dup),
                eq(customerId),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
        verify(wishlistItemRepo, never()).flush();
    }

    @Test
    void clear_success() {
        UUID wishlistId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Wishlist wishlist = mock(Wishlist.class);

        when(wishlist.getWishlistId()).thenReturn(wishlistId);
        when(wishlistItemRepo.countWishlistItems(wishlistId)).thenReturn(3);
        when(wishlistItemRepo.clearWishlist(wishlistId)).thenReturn(3);

        wishlistWriter.clear(wishlist, customerId, "EP");

        verify(wishlistItemRepo, times(1)).countWishlistItems(wishlistId);
        verify(wishlistItemRepo, times(1)).clearWishlist(wishlistId);
        verify(wishlistItemRepo, times(1)).flush();
        verifyNoInteractions(centralAudit);
    }

    @Test
    void clear_dataMismatch_throw() {
        UUID wishlistId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Wishlist wishlist = mock(Wishlist.class);

        when(wishlist.getWishlistId()).thenReturn(wishlistId);
        when(wishlistItemRepo.countWishlistItems(wishlistId)).thenReturn(5);
        when(wishlistItemRepo.clearWishlist(wishlistId)).thenReturn(3);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> wishlistWriter.clear(wishlist, customerId, "EP")
        );

        assertEquals("Data mismatch", ex.getMessage());

        verify(centralAudit, times(1)).audit(
                any(IllegalStateException.class),
                eq(customerId),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                eq("DATA_MISMATCH")
        );
        verify(wishlistItemRepo, never()).flush();
    }

    @Test
    void clear_duplicate_throw() {
        UUID wishlistId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Wishlist wishlist = mock(Wishlist.class);
        DataIntegrityViolationException dup =
                new DataIntegrityViolationException("constraint");

        when(wishlist.getWishlistId()).thenReturn(wishlistId);
        when(wishlistItemRepo.countWishlistItems(wishlistId)).thenReturn(3);
        when(wishlistItemRepo.clearWishlist(wishlistId)).thenThrow(dup);

        DataIntegrityViolationException ex = assertThrows(
                DataIntegrityViolationException.class,
                () -> wishlistWriter.clear(wishlist, customerId, "EP")
        );

        assertSame(dup, ex);

        verify(centralAudit, times(1)).audit(
                eq(dup),
                eq(customerId),
                eq("EP"),
                eq(AuditingStatus.ERROR),
                eq(dup.toString())
        );
        verify(wishlistItemRepo, never()).flush();
    }

    // == Private Methods ==
    static <E extends RuntimeException> void mockAuditReturnSame(CentralAudit auditMock) {
        // 5-arg overload
        when(auditMock.audit(
                any(RuntimeException.class),
                nullable(UUID.class),
                anyString(),
                any(AuditingStatus.class),
                anyString()
        )).thenAnswer(inv -> inv.getArgument(0));

        // 4-arg overload
        when(auditMock.audit(
                any(RuntimeException.class),
                nullable(UUID.class),
                anyString(),
                any(AuditingStatus.class)
        )).thenAnswer(inv -> inv.getArgument(0));
    }
}