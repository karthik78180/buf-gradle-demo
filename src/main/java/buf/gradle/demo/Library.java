/*
 * Proto Binary Processing Library
 * Handles serialization/deserialization of Protocol Buffer messages
 */
package buf.gradle.demo;

import com.example.payments.v1.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import java.time.Instant;

/**
 * Library for processing Protocol Buffer messages.
 * Accepts binary proto data and returns serialized responses.
 */
public class Library {

    /**
     * Processes a CreateTransactionRequest from binary proto data.
     *
     * @param requestBytes Binary encoded CreateTransactionRequest
     * @return Binary encoded CreateTransactionResponse
     * @throws InvalidProtocolBufferException if binary data is invalid
     */
    public byte[] processCreateTransactionRequest(byte[] requestBytes)
            throws InvalidProtocolBufferException {

        // Deserialize the incoming request
        CreateTransactionRequest request = CreateTransactionRequest.parseFrom(requestBytes);

        // Extract transaction details
        Transaction incomingTransaction = request.getTransaction();

        // Create response with transaction enriched with timestamps
        Transaction responseTransaction = Transaction.newBuilder(incomingTransaction)
                .setCreateTime(getCurrentTimestamp())
                .setUpdateTime(getCurrentTimestamp())
                .build();

        CreateTransactionResponse response = CreateTransactionResponse.newBuilder()
                .setTransaction(responseTransaction)
                .build();

        // Serialize and return response
        return response.toByteArray();
    }

    /**
     * Processes a GetTransactionRequest from binary proto data.
     * Returns a mock transaction for demonstration.
     *
     * @param requestBytes Binary encoded GetTransactionRequest
     * @return Binary encoded GetTransactionResponse
     * @throws InvalidProtocolBufferException if binary data is invalid
     */
    public byte[] processGetTransactionRequest(byte[] requestBytes)
            throws InvalidProtocolBufferException {

        GetTransactionRequest request = GetTransactionRequest.parseFrom(requestBytes);

        // Create a mock transaction for demonstration
        Transaction mockTransaction = Transaction.newBuilder()
                .setId("TXN-" + System.currentTimeMillis())
                .setAmount(99.99)
                .setCurrency("USD")
                .setCreateTime(getCurrentTimestamp())
                .setUpdateTime(getCurrentTimestamp())
                .build();

        GetTransactionResponse response = GetTransactionResponse.newBuilder()
                .setTransaction(mockTransaction)
                .build();

        return response.toByteArray();
    }

    /**
     * Processes a ListTransactionsRequest from binary proto data.
     * Returns a mock list of transactions for demonstration.
     *
     * @param requestBytes Binary encoded ListTransactionsRequest
     * @return Binary encoded ListTransactionsResponse
     * @throws InvalidProtocolBufferException if binary data is invalid
     */
    public byte[] processListTransactionsRequest(byte[] requestBytes)
            throws InvalidProtocolBufferException {

        ListTransactionsRequest request = ListTransactionsRequest.parseFrom(requestBytes);

        // Create mock transactions
        Transaction txn1 = Transaction.newBuilder()
                .setId("TXN-001")
                .setAmount(50.00)
                .setCurrency("USD")
                .setCreateTime(getCurrentTimestamp())
                .setUpdateTime(getCurrentTimestamp())
                .build();

        Transaction txn2 = Transaction.newBuilder()
                .setId("TXN-002")
                .setAmount(75.50)
                .setCurrency("USD")
                .setCreateTime(getCurrentTimestamp())
                .setUpdateTime(getCurrentTimestamp())
                .build();

        ListTransactionsResponse response = ListTransactionsResponse.newBuilder()
                .addTransactions(txn1)
                .addTransactions(txn2)
                .build();

        return response.toByteArray();
    }

    /**
     * Validates a CreateTransactionRequest binary data.
     * Checks if the binary data can be properly deserialized.
     *
     * @param requestBytes Binary encoded CreateTransactionRequest
     * @return true if valid, false otherwise
     */
    public boolean isValidCreateTransactionRequest(byte[] requestBytes) {
        try {
            CreateTransactionRequest.parseFrom(requestBytes);
            return true;
        } catch (InvalidProtocolBufferException e) {
            return false;
        }
    }

    /**
     * Gets the size of a serialized CreateTransactionResponse.
     *
     * @param responseBytes Binary encoded CreateTransactionResponse
     * @return Size in bytes
     * @throws InvalidProtocolBufferException if binary data is invalid
     */
    public int getResponseSize(byte[] responseBytes)
            throws InvalidProtocolBufferException {
        CreateTransactionResponse.parseFrom(responseBytes);
        return responseBytes.length;
    }

    /**
     * Helper method to get current timestamp in proto format.
     *
     * @return Current Timestamp
     */
    private Timestamp getCurrentTimestamp() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }

    /**
     * Legacy method for backward compatibility.
     *
     * @return true
     */
    public boolean someLibraryMethod() {
        return true;
    }
}
