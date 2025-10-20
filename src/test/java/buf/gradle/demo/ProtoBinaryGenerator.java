/*
 * Proto Binary File Generator
 * Generates test proto binary files for GetTransactionRequest
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
    @DisplayName("Generate GetTransactionRequest for Starbucks")
    void generateGetTransactionRequestStarbucks() throws Exception {
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setStoreName("Starbucks")
                .build();

        saveBinaryFile("get_transaction_request.pb", request.toByteArray());
        System.out.println("✅ Created: get_transaction_request.pb (store_name: Starbucks)");
    }

    @Test
    @DisplayName("Generate GetTransactionRequest for McDonald's")
    void generateGetTransactionRequestMcDonalds() throws Exception {
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setStoreName("McDonald's")
                .build();

        saveBinaryFile("get_transaction_request_mcdonalds.pb", request.toByteArray());
        System.out.println("✅ Created: get_transaction_request_mcdonalds.pb (store_name: McDonald's)");
    }

    @Test
    @DisplayName("Generate GetTransactionRequest for Walmart")
    void generateGetTransactionRequestWalmart() throws Exception {
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setStoreName("Walmart")
                .build();

        saveBinaryFile("get_transaction_request_walmart.pb", request.toByteArray());
        System.out.println("✅ Created: get_transaction_request_walmart.pb (store_name: Walmart)");
    }

    @Test
    @DisplayName("Generate GetTransactionRequest for Amazon")
    void generateGetTransactionRequestAmazon() throws Exception {
        GetTransactionRequest request = GetTransactionRequest.newBuilder()
                .setStoreName("Amazon")
                .build();

        saveBinaryFile("get_transaction_request_amazon.pb", request.toByteArray());
        System.out.println("✅ Created: get_transaction_request_amazon.pb (store_name: Amazon)");
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
