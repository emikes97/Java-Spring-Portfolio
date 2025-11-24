package commerce.eshop.core.application.customerTest.factory;

import commerce.eshop.core.application.customer.factory.CustomerFactory;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.web.dto.requests.Customer.DTOCustomerCreateUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerFactoryTest {

    private PasswordEncoder encoder;
    private CustomerFactory customerFactory;

    @BeforeEach
    void setUp() {
        encoder = mock(PasswordEncoder.class);

        customerFactory = new CustomerFactory(encoder);
    }

    @Test
    void createFrom() {
        // Arrange
        DTOCustomerCreateUser dto = mock(DTOCustomerCreateUser.class);
        when(dto.phoneNumber()).thenReturn("1234567890");
        when(dto.email()).thenReturn("test@example.com");
        when(dto.userName()).thenReturn("testerUser");
        when(dto.password()).thenReturn("rawPassword");
        when(dto.name()).thenReturn("John");
        when(dto.surname()).thenReturn("Doe");

        // Mock encoded password
        when(encoder.encode("rawPassword")).thenReturn("hashedPassword");

        // Act
        Customer result = customerFactory.createFrom(dto);

        // Assert – verify fields copied correctly
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testerUser", result.getUsername());
        assertEquals("John", result.getName());
        assertEquals("Doe", result.getSurname());

        // Assert – verify password *encoded* and stored
        assertEquals("hashedPassword", result.getPasswordHash());
        verify(encoder, times(1)).encode("rawPassword");

        // Extra safety — ensure no null entity returned
        assertNotNull(result);
    }
}