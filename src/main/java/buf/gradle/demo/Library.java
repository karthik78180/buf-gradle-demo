/*
 * Proto Binary Processing Library
 * Handles serialization/deserialization of Protocol Buffer messages
 */
package buf.gradle.demo;

import com.example.payments.v1.*;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Library for processing Protocol Buffer messages.
 * Accepts binary proto data and returns serialized responses.
 */
public class Library {

    /**
     * Processes a GetTransactionRequest from binary proto data.
     * Takes storeName as input and returns updated Transaction with dummy values.
     *
     * @param requestBytes Binary encoded GetTransactionRequest
     * @return Binary encoded GetTransactionResponse
     * @throws InvalidProtocolBufferException if binary data is invalid
     */
    public byte[] processGetTransactionRequest(byte[] requestBytes)
            throws InvalidProtocolBufferException {

        GetTransactionRequest request = GetTransactionRequest.parseFrom(requestBytes);
        String storeName = request.getStoreName();

        // Create a transaction with dummy values based on storeName
        Transaction transaction = Transaction.newBuilder()
                .setId("TXN-" + System.currentTimeMillis())
                .setStoreName(storeName)
                .setAmount(99.99)
                .setCurrency("USD")
                .setLocationZip("12345")
                .build();

        GetTransactionResponse response = GetTransactionResponse.newBuilder()
                .setTransaction(transaction)
                .build();

        return response.toByteArray();
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
