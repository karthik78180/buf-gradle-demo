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

    // ========== Legacy Method Tests ==========

    @Test
    @DisplayName("Should return true for legacy someLibraryMethod")
    void testSomeLibraryMethodReturnsTrue() {
        assertTrue(library.someLibraryMethod(), "someLibraryMethod should return 'true'");
    }

    // ========== CreateTransactionRequest Tests ==========

    @Test
    @DisplayName("Should process valid CreateTransactionRequest and return response")
    void testProcessCreateTransactionRequestSuccess()
            throws InvalidProtocolBufferException {

        // Arrange: Create a transaction request
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-123")
                .setAmount(100.00)
                .setCurrency("USD")
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setTransaction(transaction)
                .build();

        byte[] requestBytes = request.toByteArray();

        // Act: Process the request
        byte[] responseBytes = library.processCreateTransactionRequest(requestBytes);

        // Assert: Verify response can be parsed
        assertNotNull(responseBytes, "Response bytes should not be null");
        assertTrue(responseBytes.length > 0, "Response bytes should not be empty");

        CreateTransactionResponse response = CreateTransactionResponse.parseFrom(responseBytes);
        assertNotNull(response.getTransaction(), "Transaction should not be null");
        assertEquals("TXN-123", response.getTransaction().getId(), "Transaction ID should match");
        assertEquals(100.00, response.getTransaction().getAmount(), 0.01, "Amount should match");
        assertTrue(response.getTransaction().hasCreateTime(), "Create time should be set");
        assertTrue(response.getTransaction().hasUpdateTime(), "Update time should be set");
    }

    @Test
    @DisplayName("Should throw exception for invalid CreateTransactionRequest binary")
    void testProcessCreateTransactionRequestInvalidBinary() {
        // Arrange: Create invalid binary data
        byte[] invalidBytes = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};

        // Act & Assert: Should throw InvalidProtocolBufferException
        assertThrows(InvalidProtocolBufferException.class, () -> {
            library.processCreateTransactionRequest(invalidBytes);
        }, "Should throw exception for invalid binary data");
    }

    @Test
    @DisplayName("Should validate correct CreateTransactionRequest")
    void testIsValidCreateTransactionRequestValid() {
        // Arrange: Create valid request
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-456")
                .setAmount(50.00)
                .setCurrency("USD")
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setTransaction(transaction)
                .build();

        // Act: Validate
        boolean isValid = library.isValidCreateTransactionRequest(request.toByteArray());

        // Assert
        assertTrue(isValid, "Valid request should be validated successfully");
    }

    @Test
    @DisplayName("Should identify invalid CreateTransactionRequest binary")
    void testIsValidCreateTransactionRequestInvalid() {
        // Arrange: Create invalid binary data
        byte[] invalidBytes = new byte[]{(byte) 0xFF, (byte) 0xFE};

        // Act: Validate
        boolean isValid = library.isValidCreateTransactionRequest(invalidBytes);

        // Assert
        assertFalse(isValid, "Invalid request should fail validation");
    }

    // ========== GetTransactionRequest Tests ==========

    @Test
    @DisplayName("Should process GetTransactionRequest and return mock response")
    void testProcessGetTransactionRequestSuccess()
            throws InvalidProtocolBufferException {

        // Arrange: Create get request
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setName("transactions/TXN-789")
                .build();

        byte[] requestBytes = request.toByteArray();

        // Act: Process the request
        byte[] responseBytes = library.processGetTransactionRequest(requestBytes);

        // Assert: Verify response
        assertNotNull(responseBytes, "Response bytes should not be null");
        GetTransactionResponse response = GetTransactionResponse.parseFrom(responseBytes);
        assertNotNull(response.getTransaction(), "Transaction should not be null");
        assertEquals(99.99, response.getTransaction().getAmount(), 0.01, "Should return mock amount");
        assertEquals("USD", response.getTransaction().getCurrency(), "Should return USD currency");
    }

    @Test
    @DisplayName("Should throw exception for invalid GetTransactionRequest binary")
    void testProcessGetTransactionRequestInvalidBinary() {
        byte[] invalidBytes = new byte[]{(byte) 0xAA, (byte) 0xBB};

        assertThrows(InvalidProtocolBufferException.class, () -> {
            library.processGetTransactionRequest(invalidBytes);
        }, "Should throw exception for invalid binary data");
    }

    // ========== ListTransactionsRequest Tests ==========

    @Test
    @DisplayName("Should process ListTransactionsRequest and return multiple transactions")
    void testProcessListTransactionsRequestSuccess()
            throws InvalidProtocolBufferException {

        // Arrange: Create list request
        ListTransactionsRequest request = ListTransactionsRequest.newBuilder()
                .setParent("accounts/ACC-123")
                .setPageSize(10)
                .build();

        byte[] requestBytes = request.toByteArray();

        // Act: Process the request
        byte[] responseBytes = library.processListTransactionsRequest(requestBytes);

        // Assert: Verify response
        assertNotNull(responseBytes, "Response bytes should not be null");
        ListTransactionsResponse response = ListTransactionsResponse.parseFrom(responseBytes);
        assertEquals(2, response.getTransactionsCount(), "Should return 2 mock transactions");

        Transaction txn1 = response.getTransactions(0);
        assertEquals("TXN-001", txn1.getId(), "First transaction ID should be TXN-001");
        assertEquals(50.00, txn1.getAmount(), 0.01, "First transaction amount should be 50.00");

        Transaction txn2 = response.getTransactions(1);
        assertEquals("TXN-002", txn2.getId(), "Second transaction ID should be TXN-002");
        assertEquals(75.50, txn2.getAmount(), 0.01, "Second transaction amount should be 75.50");
    }

    @Test
    @DisplayName("Should throw exception for invalid ListTransactionsRequest binary")
    void testProcessListTransactionsRequestInvalidBinary() {
        byte[] invalidBytes = new byte[]{(byte) 0x11, (byte) 0x22, (byte) 0x33};

        assertThrows(InvalidProtocolBufferException.class, () -> {
            library.processListTransactionsRequest(invalidBytes);
        }, "Should throw exception for invalid binary data");
    }

    // ========== Response Size Tests ==========

    @Test
    @DisplayName("Should correctly calculate response size")
    void testGetResponseSize()
            throws InvalidProtocolBufferException {

        // Arrange: Create a response
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-999")
                .setAmount(150.00)
                .setCurrency("USD")
                .build();

        CreateTransactionResponse response = CreateTransactionResponse.newBuilder()
                .setTransaction(transaction)
                .build();

        byte[] responseBytes = response.toByteArray();

        // Act: Get size
        int size = library.getResponseSize(responseBytes);

        // Assert
        assertEquals(responseBytes.length, size, "Response size should match byte array length");
        assertTrue(size > 0, "Response size should be greater than 0");
    }

    @Test
    @DisplayName("Should throw exception for invalid response binary when calculating size")
    void testGetResponseSizeInvalidBinary() {
        byte[] invalidBytes = new byte[]{(byte) 0xCC, (byte) 0xDD};

        assertThrows(InvalidProtocolBufferException.class, () -> {
            library.getResponseSize(invalidBytes);
        }, "Should throw exception for invalid binary data");
    }

    // ========== Serialization/Deserialization Tests ==========

    @Test
    @DisplayName("Should handle round-trip serialization/deserialization")
    void testRoundTripSerialization()
            throws InvalidProtocolBufferException {

        // Arrange: Create and serialize a transaction
        Transaction original = Transaction.newBuilder()
                .setId("TXN-RTT")
                .setAmount(200.00)
                .setCurrency("EUR")
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setTransaction(original)
                .build();

        byte[] serialized = request.toByteArray();

        // Act: Deserialize
        CreateTransactionRequest deserialized = CreateTransactionRequest.parseFrom(serialized);

        // Assert
        assertEquals(original.getId(), deserialized.getTransaction().getId(), "ID should match");
        assertEquals(original.getAmount(), deserialized.getTransaction().getAmount(), 0.01, "Amount should match");
        assertEquals(original.getCurrency(), deserialized.getTransaction().getCurrency(), "Currency should match");
    }

    @Test
    @DisplayName("Should preserve field values through processing pipeline")
    void testFieldPreservationThroughProcessing()
            throws InvalidProtocolBufferException {

        // Arrange: Create request with all fields
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-PRESERVE")
                .setAmount(999.99)
                .setCurrency("JPY")
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setParent("merchants/M-123")
                .setTransaction(transaction)
                .build();

        byte[] requestBytes = request.toByteArray();

        // Act: Process through library
        byte[] responseBytes = library.processCreateTransactionRequest(requestBytes);
        CreateTransactionResponse response = CreateTransactionResponse.parseFrom(responseBytes);

        // Assert: Verify fields are preserved
        Transaction responseTransaction = response.getTransaction();
        assertEquals("TXN-PRESERVE", responseTransaction.getId(), "ID should be preserved");
        assertEquals(999.99, responseTransaction.getAmount(), 0.01, "Amount should be preserved");
        assertEquals("JPY", responseTransaction.getCurrency(), "Currency should be preserved");
    }

    @Test
    @DisplayName("Should not modify input transaction when processing")
    void testNonDestructiveProcessing()
            throws InvalidProtocolBufferException {

        // Arrange: Create transaction without timestamps
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-INPUT")
                .setAmount(75.00)
                .setCurrency("GBP")
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setTransaction(transaction)
                .build();

        byte[] requestBytes = request.toByteArray();

        // Act: Process
        library.processCreateTransactionRequest(requestBytes);
        CreateTransactionRequest parsedAgain = CreateTransactionRequest.parseFrom(requestBytes);

        // Assert: Original should not have timestamps
        assertFalse(parsedAgain.getTransaction().hasCreateTime(), "Original transaction should not have timestamps");
        assertFalse(parsedAgain.getTransaction().hasUpdateTime(), "Original transaction should not have timestamps");
    }
}
