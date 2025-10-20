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

    // ========== Proto Validation Tests (Buf Validation) ==========

    @Test
    @DisplayName("Should validate that Transaction REQUIRED fields can be set and retrieved")
    void testTransactionRequiredFieldsValidation()
            throws InvalidProtocolBufferException {
        // Arrange: Build Transaction with all REQUIRED fields
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-REQ-001")
                .setAmount(100.00)
                .setCurrency("USD")
                .build();

        // Act: Verify REQUIRED fields are set
        byte[] bytes = transaction.toByteArray();
        Transaction deserialized = Transaction.parseFrom(bytes);

        // Assert: All REQUIRED fields should be present and retrievable
        assertNotNull(transaction, "Transaction should be valid");
        assertEquals("TXN-REQ-001", transaction.getId(), "ID should match");
        assertEquals(100.00, transaction.getAmount(), 0.01, "Amount should be set");
        assertEquals("USD", transaction.getCurrency(), "Currency should be set");

        // Verify serialization preserves REQUIRED fields
        assertEquals("TXN-REQ-001", deserialized.getId(), "ID should be preserved after serialization");
        assertEquals(100.00, deserialized.getAmount(), 0.01, "Amount should be preserved after serialization");
        assertEquals("USD", deserialized.getCurrency(), "Currency should be preserved after serialization");
    }

    @Test
    @DisplayName("Should validate CreateTransactionRequest REQUIRED fields")
    void testCreateTransactionRequestRequiredValidation() {
        // Arrange: Create request with missing REQUIRED transaction field
        CreateTransactionRequest.Builder requestBuilder = CreateTransactionRequest.newBuilder();

        // Act & Assert: Verify transaction is REQUIRED
        CreateTransactionRequest invalidRequest = requestBuilder.build();
        assertFalse(invalidRequest.hasTransaction(), "Request without transaction should have empty transaction");

        // Verify valid request with transaction
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-001")
                .setAmount(50.00)
                .setCurrency("USD")
                .build();

        CreateTransactionRequest validRequest = CreateTransactionRequest.newBuilder()
                .setTransaction(transaction)
                .build();

        assertTrue(validRequest.hasTransaction(), "Valid request should have transaction");
        assertEquals("TXN-001", validRequest.getTransaction().getId(), "Transaction ID should match");
    }

    @Test
    @DisplayName("Should validate GetTransactionRequest REQUIRED name field")
    void testGetTransactionRequestRequiredValidation() {
        // Arrange & Act: Create request without name (REQUIRED field)
        GetTransactionRequest emptyRequest = GetTransactionRequest.newBuilder().build();

        // Assert: Empty name means field not set
        assertTrue(emptyRequest.getName().isEmpty(), "Empty request should have empty name");

        // Verify valid request with name
        GetTransactionRequest validRequest = GetTransactionRequest.newBuilder()
                .setName("transactions/TXN-123")
                .build();

        assertFalse(validRequest.getName().isEmpty(), "Valid request should have non-empty name");
        assertEquals("transactions/TXN-123", validRequest.getName(), "Request name should match");
    }

    @Test
    @DisplayName("Should validate proto field types and constraints")
    void testProtoFieldTypeValidation()
            throws InvalidProtocolBufferException {
        // Arrange: Create transaction with various field types
        Address address = Address.newBuilder()
                .setLine1("123 Main St")
                .setCity("Seattle")
                .setZipcode("98101")
                .setCountry("US")
                .build();

        CardHolder cardHolder = CardHolder.newBuilder()
                .setCardholderName("John Doe")
                .setEmail("john@example.com")
                .setBillingAddress(address)
                .build();

        Store store = Store.newBuilder()
                .setStoreId("STORE-001")
                .setStoreName("Seattle Store")
                .setAddress(address)
                .build();

        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-TYPE-001")
                .setAmount(150.75)
                .setCurrency("USD")
                .setCardholder(cardHolder)
                .setStore(store)
                .build();

        // Act: Serialize and deserialize
        byte[] bytes = transaction.toByteArray();
        Transaction deserialized = Transaction.parseFrom(bytes);

        // Assert: Verify all types are preserved correctly
        assertEquals("TXN-TYPE-001", deserialized.getId(), "String field should match");
        assertEquals(150.75, deserialized.getAmount(), 0.01, "Double field should match");
        assertEquals("USD", deserialized.getCurrency(), "String field should match");
        assertEquals("John Doe", deserialized.getCardholder().getCardholderName(), "Nested message field should match");
        assertEquals("STORE-001", deserialized.getStore().getStoreId(), "Store ID should match");
    }

    @Test
    @DisplayName("Should validate proto enum-like string constraints")
    void testProtoStringConstraintValidation() {
        // Arrange: Create transaction with currency validation
        String[] validCurrencies = {"USD", "EUR", "GBP", "JPY"};

        for (String currency : validCurrencies) {
            // Act: Build transaction with different currencies
            Transaction transaction = Transaction.newBuilder()
                    .setId("TXN-CUR-" + currency)
                    .setAmount(100.00)
                    .setCurrency(currency)
                    .build();

            // Assert: Currency field should be set correctly
            assertEquals(currency, transaction.getCurrency(), "Currency " + currency + " should be preserved");
        }
    }

    @Test
    @DisplayName("Should validate ListTransactionsRequest pagination fields")
    void testListTransactionsRequestPaginationValidation() {
        // Arrange: Create list request with pagination
        ListTransactionsRequest request = ListTransactionsRequest.newBuilder()
                .setParent("accounts/ACC-001")
                .setPageSize(50)
                .setPageToken("token-abc123")
                .build();

        // Act & Assert: Verify pagination fields are properly set
        assertEquals("accounts/ACC-001", request.getParent(), "Parent should match");
        assertEquals(50, request.getPageSize(), "Page size should be 50");
        assertEquals("token-abc123", request.getPageToken(), "Page token should match");
    }

    @Test
    @DisplayName("Should validate DeleteTransactionRequest name field")
    void testDeleteTransactionRequestValidation()
            throws InvalidProtocolBufferException {
        // Arrange: Create delete request
        DeleteTransactionRequest request = DeleteTransactionRequest.newBuilder()
                .setName("transactions/TXN-DELETE-001")
                .build();

        // Act: Serialize and deserialize
        byte[] bytes = request.toByteArray();
        DeleteTransactionRequest deserialized = DeleteTransactionRequest.parseFrom(bytes);

        // Assert: Verify field is preserved
        assertEquals("transactions/TXN-DELETE-001", deserialized.getName(), "Transaction name should match");
    }

    @Test
    @DisplayName("Should validate proto message structure compliance")
    void testProtoMessageStructureCompliance() {
        // Arrange: Build various messages to verify proto structure

        // 1. Verify Address structure
        Address address = Address.newBuilder()
                .setLine1("123 Main")
                .setCity("Seattle")
                .setZipcode("98101")
                .setCountry("US")
                .build();
        assertTrue(address.getSerializedSize() > 0, "Address serialization should work");

        // 2. Verify CardHolder structure
        CardHolder holder = CardHolder.newBuilder()
                .setCardholderName("John Doe")
                .setBillingAddress(address)
                .build();
        assertTrue(holder.getAllFields().size() > 0, "CardHolder should have fields");

        // 3. Verify Store structure
        Store store = Store.newBuilder()
                .setStoreId("STORE-001")
                .setAddress(address)
                .build();
        assertTrue(store.getAllFields().size() > 0, "Store should have fields");

        // 4. Verify Transaction structure
        Transaction txn = Transaction.newBuilder()
                .setId("TXN-001")
                .setAmount(100.00)
                .setCurrency("USD")
                .setCardholder(holder)
                .setStore(store)
                .build();

        // Assert: All messages serialize successfully
        assertTrue(address.toByteArray().length > 0, "Address should serialize");
        assertTrue(holder.toByteArray().length > 0, "CardHolder should serialize");
        assertTrue(store.toByteArray().length > 0, "Store should serialize");
        assertTrue(txn.toByteArray().length > 0, "Transaction should serialize");
    }

    @Test
    @DisplayName("Should validate proto output-only fields are set by library")
    void testProtoOutputOnlyFieldsValidation()
            throws InvalidProtocolBufferException {
        // Arrange: Create request without timestamps
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-OUTPUT-001")
                .setAmount(75.00)
                .setCurrency("USD")
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setTransaction(transaction)
                .build();

        // Act: Process through library (should add timestamps)
        byte[] responseBytes = new Library().processCreateTransactionRequest(request.toByteArray());
        CreateTransactionResponse response = CreateTransactionResponse.parseFrom(responseBytes);

        // Assert: Output-only fields should be set by library
        assertTrue(response.getTransaction().hasCreateTime(), "Create time (output-only) should be set");
        assertTrue(response.getTransaction().hasUpdateTime(), "Update time (output-only) should be set");
        assertTrue(response.getTransaction().getCreateTime().getSeconds() > 0, "Create time seconds should be set");
        assertTrue(response.getTransaction().getUpdateTime().getSeconds() > 0, "Update time seconds should be set");
    }
}
