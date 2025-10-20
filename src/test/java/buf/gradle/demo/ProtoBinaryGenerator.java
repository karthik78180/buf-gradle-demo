/*
 * Proto Binary File Generator
 * Generates test proto binary files for manual testing and Postman
 */
package buf.gradle.demo;

import com.example.payments.v1.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generates proto binary files for manual testing.
 * Run this class to generate .pb files in src/test/resources/
 * Use these files with Postman or curl for API testing.
 */
@DisplayName("Proto Binary File Generator")
class ProtoBinaryGenerator {

    private static final Path RESOURCES_DIR = Paths.get("src/test/resources");

    @BeforeAll
    static void createResourcesDirectory() throws Exception {
        Files.createDirectories(RESOURCES_DIR);
    }

    @Test
    @DisplayName("Generate CreateTransactionRequest binary file")
    void generateCreateTransactionRequestBinary() throws Exception {
        // Create full transaction with all fields
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-001")
                .setAmount(150.00)
                .setCurrency("USD")
                .setCardholder(CardHolder.newBuilder()
                        .setCardholderName("Alice Smith")
                        .setEmail("alice@example.com")
                        .setBillingAddress(Address.newBuilder()
                                .setLine1("123 Main St")
                                .setCity("Seattle")
                                .setZipcode("98101")
                                .setCountry("US")
                                .build())
                        .build())
                .setStore(Store.newBuilder()
                        .setStoreId("STORE-001")
                        .setStoreName("Seattle Store")
                        .setAddress(Address.newBuilder()
                                .setLine1("456 Pine Ave")
                                .setCity("Seattle")
                                .setZipcode("98102")
                                .setCountry("US")
                                .build())
                        .build())
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setParent("merchants/M-001")
                .setTransaction(transaction)
                .build();

        saveBinaryFile("create_transaction_request.pb", request.toByteArray());
        System.out.println("✅ Created: create_transaction_request.pb");
    }

    @Test
    @DisplayName("Generate GetTransactionRequest binary file")
    void generateGetTransactionRequestBinary() throws Exception {
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setName("transactions/TXN-001")
                .build();

        saveBinaryFile("get_transaction_request.pb", request.toByteArray());
        System.out.println("✅ Created: get_transaction_request.pb");
    }

    @Test
    @DisplayName("Generate ListTransactionsRequest binary file")
    void generateListTransactionsRequestBinary() throws Exception {
        ListTransactionsRequest request = ListTransactionsRequest.newBuilder()
                .setParent("accounts/ACC-001")
                .setPageSize(10)
                .setPageToken("")
                .build();

        saveBinaryFile("list_transactions_request.pb", request.toByteArray());
        System.out.println("✅ Created: list_transactions_request.pb");
    }

    @Test
    @DisplayName("Generate DeleteTransactionRequest binary file")
    void generateDeleteTransactionRequestBinary() throws Exception {
        DeleteTransactionRequest request = DeleteTransactionRequest.newBuilder()
                .setName("transactions/TXN-001")
                .build();

        saveBinaryFile("delete_transaction_request.pb", request.toByteArray());
        System.out.println("✅ Created: delete_transaction_request.pb");
    }

    @Test
    @DisplayName("Generate sample transaction for testing")
    void generateSampleTransaction() throws Exception {
        // Create sample transaction
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-SAMPLE")
                .setAmount(99.99)
                .setCurrency("USD")
                .setCardholder(CardHolder.newBuilder()
                        .setCardholderName("Bob Johnson")
                        .build())
                .setStore(Store.newBuilder()
                        .setStoreId("STORE-002")
                        .build())
                .build();

        // Create and save CreateTransactionRequest
        CreateTransactionRequest createReq = CreateTransactionRequest.newBuilder()
                .setTransaction(transaction)
                .build();

        byte[] createBinary = createReq.toByteArray();
        saveBinaryFile("create_transaction_request_sample.pb", createBinary);

        System.out.println("✅ Created: create_transaction_request_sample.pb");
    }

    @Test
    @DisplayName("Generate comprehensive transaction for all operations")
    void generateComprehensiveTransaction() throws Exception {
        // Full transaction with all optional fields populated
        Address billingAddr = Address.newBuilder()
                .setLine1("789 Oak St")
                .setLine2("Suite 100")
                .setCity("Portland")
                .setState("OR")
                .setZipcode("97201")
                .setCountry("US")
                .build();

        CardHolder cardholder = CardHolder.newBuilder()
                .setCardholderName("Carol White")
                .setEmail("carol@example.com")
                .setBillingAddress(billingAddr)
                .build();

        Address storeAddr = Address.newBuilder()
                .setLine1("321 Commerce Dr")
                .setCity("Portland")
                .setState("OR")
                .setZipcode("97202")
                .setCountry("US")
                .build();

        Store store = Store.newBuilder()
                .setStoreId("STORE-PORT")
                .setStoreName("Portland Store")
                .setAddress(storeAddr)
                .build();

        Transaction fullTransaction = Transaction.newBuilder()
                .setId("TXN-COMPREHENSIVE")
                .setAmount(299.99)
                .setCurrency("USD")
                .setCardholder(cardholder)
                .setStore(store)
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setParent("merchants/M-comprehensive")
                .setTransaction(fullTransaction)
                .build();

        byte[] binary = request.toByteArray();
        saveBinaryFile("comprehensive_transaction_request.pb", binary);

        System.out.println("✅ Created: comprehensive_transaction_request.pb");
    }

    @Test
    @DisplayName("Generate minimal transaction (only required fields)")
    void generateMinimalTransaction() throws Exception {
        // Minimal transaction with only REQUIRED fields
        Transaction minimalTransaction = Transaction.newBuilder()
                .setId("TXN-MIN")
                .setAmount(10.00)
                .setCurrency("USD")
                .setCardholder(CardHolder.newBuilder()
                        .setCardholderName("Minimal Holder")
                        .build())
                .setStore(Store.newBuilder()
                        .setStoreId("STORE-MIN")
                        .build())
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
                .setTransaction(minimalTransaction)
                .build();

        byte[] binary = request.toByteArray();
        saveBinaryFile("minimal_transaction_request.pb", binary);

        System.out.println("✅ Created: minimal_transaction_request.pb");
    }

    /**
     * Save binary data to .pb file
     */
    private static void saveBinaryFile(String filename, byte[] data) throws Exception {
        Path filePath = RESOURCES_DIR.resolve(filename);
        Files.write(filePath, data);
        System.out.printf("  File: %s%n  Size: %d bytes%n", filePath, data.length);
    }
}
