/*
 * Payment Transaction API Server
 * Simple HTTP server for proto binary request/response testing
 */

package com.example.app;

import buf.gradle.demo.Library;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple HTTP server that handles proto binary requests/responses
 */
public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private static final int PORT = 8080;
    private static final Library LIBRARY = new Library();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

        // Register endpoints
        server.createContext("/api/transactions/create", new TransactionHandler(LIBRARY, "create"));
        server.createContext("/api/transactions/get", new TransactionHandler(LIBRARY, "get"));
        server.createContext("/api/transactions/list", new TransactionHandler(LIBRARY, "list"));
        server.createContext("/api/transactions/delete", new TransactionHandler(LIBRARY, "delete"));

        server.setExecutor(null);
        server.start();

        LOGGER.info("✅ Payment Transaction Server started on http://localhost:" + PORT);
        LOGGER.info("   Endpoints:");
        LOGGER.info("   - POST /api/transactions/create (binary proto)");
        LOGGER.info("   - POST /api/transactions/get (binary proto)");
        LOGGER.info("   - POST /api/transactions/list (binary proto)");
        LOGGER.info("   - POST /api/transactions/delete (binary proto)");
        LOGGER.info("");
        LOGGER.info("Press Ctrl+C to stop...");
    }

    /**
     * HTTP handler for proto binary requests
     */
    static class TransactionHandler implements HttpHandler {
        private final Library library;
        private final String operation;

        TransactionHandler(Library library, String operation) {
            this.library = library;
            this.operation = operation;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Verify Content-Type
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                if (!contentType.equals("application/x-protobuf")) {
                    sendError(exchange, 400, "Invalid Content-Type. Expected: application/x-protobuf");
                    return;
                }

                // Read request body
                InputStream inputStream = exchange.getRequestBody();
                byte[] requestData = inputStream.readAllBytes();

                LOGGER.info("📨 [" + operation.toUpperCase() + "] Received " + requestData.length + " bytes");

                // Process based on operation
                byte[] responseData;
                try {
                    switch (operation) {
                        case "create":
                            responseData = library.processCreateTransactionRequest(requestData);
                            break;
                        case "get":
                            responseData = library.processGetTransactionRequest(requestData);
                            break;
                        case "list":
                            responseData = library.processListTransactionsRequest(requestData);
                            break;
                        case "delete":
                            responseData = library.processDeleteTransactionRequest(requestData);
                            break;
                        default:
                            sendError(exchange, 404, "Unknown operation: " + operation);
                            return;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Processing error", e);
                    sendError(exchange, 500, "Processing failed: " + e.getMessage());
                    return;
                }

                // Send response
                exchange.getResponseHeaders().set("Content-Type", "application/x-protobuf");
                exchange.sendResponseHeaders(200, responseData.length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(responseData);
                outputStream.close();

                LOGGER.info("📤 [" + operation.toUpperCase() + "] Sent " + responseData.length + " bytes");

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Handler error", e);
                try {
                    sendError(exchange, 500, "Server error: " + e.getMessage());
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Error sending error response", ex);
                }
            }
        }

        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(statusCode, message.length());
            exchange.getResponseBody().write(message.getBytes());
            exchange.getResponseBody().close();
        }
    }
}
