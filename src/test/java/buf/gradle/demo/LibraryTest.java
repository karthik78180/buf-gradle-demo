/*
 * Unit tests for Library - Proto Binary Processing
 */
package buf.gradle.demo;

import com.example.payments.v1.*;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Library - Proto Binary Processing Tests")
class LibraryTest {

    private Library library;

    @BeforeEach
    void setUp() {
        library = new Library();
    }

    @Test
    @DisplayName("Should return true for legacy someLibraryMethod")
    void testSomeLibraryMethodReturnsTrue() {
        assertTrue(library.someLibraryMethod(), "someLibraryMethod should return 'true'");
    }

    // ========== GetTransactionRequest Tests ==========

    @Test
    @DisplayName("Should process valid GetTransactionRequest with Starbucks and return Transaction")
    void testProcessGetTransactionRequestStarbucks()
            throws InvalidProtocolBufferException {

        // Arrange: Create a GetTransactionRequest with store_name
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setStoreName("Starbucks")
                .build();

        byte[] requestBytes = request.toByteArray();

        // Act: Process the request
        byte[] responseBytes = library.processGetTransactionRequest(requestBytes);

        // Assert: Verify response can be parsed
        assertNotNull(responseBytes, "Response bytes should not be null");
        assertTrue(responseBytes.length > 0, "Response bytes should not be empty");

        GetTransactionResponse response = GetTransactionResponse.parseFrom(responseBytes);
        assertNotNull(response.getTransaction(), "Transaction should not be null");
        assertEquals("Starbucks", response.getTransaction().getStoreName(), "Store name should match");
        assertEquals("USD", response.getTransaction().getCurrency(), "Currency should be USD");
        assertEquals(99.99, response.getTransaction().getAmount(), 0.01, "Amount should be 99.99");
        assertEquals("12345", response.getTransaction().getLocationZip(), "Location zip should be 12345");
        assertTrue(response.getTransaction().getId().startsWith("TXN-"), "Transaction ID should have TXN- prefix");
    }

    @Test
    @DisplayName("Should process valid GetTransactionRequest with McDonald's")
    void testProcessGetTransactionRequestMcDonalds()
            throws InvalidProtocolBufferException {

        // Arrange
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setStoreName("McDonald's")
                .build();

        // Act
        byte[] responseBytes = library.processGetTransactionRequest(request.toByteArray());

        // Assert
        GetTransactionResponse response = GetTransactionResponse.parseFrom(responseBytes);
        assertEquals("McDonald's", response.getTransaction().getStoreName(), "Store name should be McDonald's");
    }

    @Test
    @DisplayName("Should process valid GetTransactionRequest with Walmart")
    void testProcessGetTransactionRequestWalmart()
            throws InvalidProtocolBufferException {

        // Arrange
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setStoreName("Walmart")
                .build();

        // Act
        byte[] responseBytes = library.processGetTransactionRequest(request.toByteArray());

        // Assert
        GetTransactionResponse response = GetTransactionResponse.parseFrom(responseBytes);
        assertEquals("Walmart", response.getTransaction().getStoreName(), "Store name should be Walmart");
    }

    @Test
    @DisplayName("Should throw exception for invalid GetTransactionRequest binary")
    void testProcessGetTransactionRequestInvalidBinary() {
        // Arrange: Create invalid binary data
        byte[] invalidBytes = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};

        // Act & Assert: Should throw InvalidProtocolBufferException
        assertThrows(InvalidProtocolBufferException.class, () -> {
            library.processGetTransactionRequest(invalidBytes);
        }, "Should throw exception for invalid binary data");
    }

    @Test
    @DisplayName("Should return all required Transaction fields in response")
    void testGetTransactionResponseHasAllFields()
            throws InvalidProtocolBufferException {

        // Arrange
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setStoreName("TestStore")
                .build();

        // Act
        byte[] responseBytes = library.processGetTransactionRequest(request.toByteArray());
        GetTransactionResponse response = GetTransactionResponse.parseFrom(responseBytes);
        Transaction transaction = response.getTransaction();

        // Assert: Verify all fields are present
        assertFalse(transaction.getId().isEmpty(), "ID should be set");
        assertFalse(transaction.getStoreName().isEmpty(), "Store name should be set");
        assertTrue(transaction.getAmount() > 0, "Amount should be set");
        assertFalse(transaction.getCurrency().isEmpty(), "Currency should be set");
        assertFalse(transaction.getLocationZip().isEmpty(), "Location zip should be set");
    }
}
