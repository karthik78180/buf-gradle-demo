/*
 * Go Client for Payment Transaction API
 * Demonstrates CRUD operations using Protocol Buffers
 */

package main

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"

	pb "go.example.com/payments-client/gen/payments/v1"
	"google.golang.org/protobuf/proto"
)

const (
	// Server endpoints
	createTransactionURL = "http://localhost:8080/api/transactions/create"
	getTransactionURL    = "http://localhost:8080/api/transactions/get"
	listTransactionsURL  = "http://localhost:8080/api/transactions/list"
	deleteTransactionURL = "http://localhost:8080/api/transactions/delete"
)

// Client for Payment Transaction API
type PaymentClient struct {
	baseURL string
	client  *http.Client
}

// NewPaymentClient creates a new payment client
func NewPaymentClient(baseURL string) *PaymentClient {
	return &PaymentClient{
		baseURL: baseURL,
		client:  &http.Client{},
	}
}

// CreateTransaction creates a new transaction
func (c *PaymentClient) CreateTransaction(txn *pb.Transaction) (*pb.CreateTransactionResponse, error) {
	fmt.Println("\n=== CREATE TRANSACTION ===")

	// Build request
	request := &pb.CreateTransactionRequest{
		Parent:      "merchants/M-001",
		Transaction: txn,
	}

	// Serialize to binary
	data, err := proto.Marshal(request)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal: %w", err)
	}

	fmt.Printf("Request size: %d bytes\n", len(data))
	fmt.Printf("Transaction ID: %s\n", txn.Id)
	fmt.Printf("Amount: $%.2f %s\n", txn.Amount, txn.Currency)

	// Send request
	resp, err := c.client.Post(
		createTransactionURL,
		"application/x-protobuf",
		ioutil.NopCloser(bytes.NewReader(data)),
	)
	if err != nil {
		return nil, fmt.Errorf("request failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := ioutil.ReadAll(resp.Body)
		return nil, fmt.Errorf("server error: %d - %s", resp.StatusCode, string(body))
	}

	// Read response
	respBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	// Unmarshal response
	response := &pb.CreateTransactionResponse{}
	if err := proto.Unmarshal(respBody, response); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}

	fmt.Println("✅ Transaction created successfully")
	if response.Transaction != nil {
		fmt.Printf("   Create Time: %v\n", response.Transaction.CreateTime)
		fmt.Printf("   Update Time: %v\n", response.Transaction.UpdateTime)
	}

	return response, nil
}

// GetTransaction retrieves a transaction by ID
func (c *PaymentClient) GetTransaction(transactionID string) (*pb.GetTransactionResponse, error) {
	fmt.Println("\n=== GET TRANSACTION ===")

	// Build request
	request := &pb.GetTransactionRequest{
		Name: fmt.Sprintf("transactions/%s", transactionID),
	}

	// Serialize to binary
	data, err := proto.Marshal(request)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal: %w", err)
	}

	fmt.Printf("Request size: %d bytes\n", len(data))
	fmt.Printf("Looking up: %s\n", request.Name)

	// Send request
	resp, err := c.client.Post(
		getTransactionURL,
		"application/x-protobuf",
		ioutil.NopCloser(bytes.NewReader(data)),
	)
	if err != nil {
		return nil, fmt.Errorf("request failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := ioutil.ReadAll(resp.Body)
		return nil, fmt.Errorf("server error: %d - %s", resp.StatusCode, string(body))
	}

	// Read response
	respBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	// Unmarshal response
	response := &pb.GetTransactionResponse{}
	if err := proto.Unmarshal(respBody, response); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}

	fmt.Println("✅ Transaction retrieved successfully")
	if response.Transaction != nil {
		fmt.Printf("   ID: %s\n", response.Transaction.Id)
		fmt.Printf("   Amount: $%.2f %s\n", response.Transaction.Amount, response.Transaction.Currency)
	}

	return response, nil
}

// ListTransactions lists all transactions
func (c *PaymentClient) ListTransactions(pageSize int32) (*pb.ListTransactionsResponse, error) {
	fmt.Println("\n=== LIST TRANSACTIONS ===")

	// Build request
	request := &pb.ListTransactionsRequest{
		Parent:   "accounts/ACC-001",
		PageSize: pageSize,
	}

	// Serialize to binary
	data, err := proto.Marshal(request)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal: %w", err)
	}

	fmt.Printf("Request size: %d bytes\n", len(data))
	fmt.Printf("Page size: %d\n", pageSize)

	// Send request
	resp, err := c.client.Post(
		listTransactionsURL,
		"application/x-protobuf",
		ioutil.NopCloser(bytes.NewReader(data)),
	)
	if err != nil {
		return nil, fmt.Errorf("request failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := ioutil.ReadAll(resp.Body)
		return nil, fmt.Errorf("server error: %d - %s", resp.StatusCode, string(body))
	}

	// Read response
	respBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	// Unmarshal response
	response := &pb.ListTransactionsResponse{}
	if err := proto.Unmarshal(respBody, response); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}

	fmt.Println("✅ Transactions listed successfully")
	fmt.Printf("   Total transactions: %d\n", len(response.Transactions))
	for i, txn := range response.Transactions {
		fmt.Printf("   [%d] ID: %s, Amount: $%.2f %s\n", i+1, txn.Id, txn.Amount, txn.Currency)
	}

	return response, nil
}

// DeleteTransaction deletes a transaction
func (c *PaymentClient) DeleteTransaction(transactionID string) (*pb.DeleteTransactionResponse, error) {
	fmt.Println("\n=== DELETE TRANSACTION ===")

	// Build request
	request := &pb.DeleteTransactionRequest{
		Name: fmt.Sprintf("transactions/%s", transactionID),
	}

	// Serialize to binary
	data, err := proto.Marshal(request)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal: %w", err)
	}

	fmt.Printf("Request size: %d bytes\n", len(data))
	fmt.Printf("Deleting: %s\n", request.Name)

	// Send request
	resp, err := c.client.Post(
		deleteTransactionURL,
		"application/x-protobuf",
		ioutil.NopCloser(bytes.NewReader(data)),
	)
	if err != nil {
		return nil, fmt.Errorf("request failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := ioutil.ReadAll(resp.Body)
		return nil, fmt.Errorf("server error: %d - %s", resp.StatusCode, string(body))
	}

	// Read response
	respBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	// Unmarshal response
	response := &pb.DeleteTransactionResponse{}
	if err := proto.Unmarshal(respBody, response); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}

	fmt.Println("✅ Transaction deleted successfully")

	return response, nil
}

// CRUD Demo
func runCRUDDemo(client *PaymentClient) error {
	// CREATE - Create a new transaction
	createResp, err := client.CreateTransaction(&pb.Transaction{
		Id:       "TXN-001",
		Amount:   150.00,
		Currency: "USD",
		Cardholder: &pb.CardHolder{
			CardholderName: "Alice Smith",
			Email:          "alice@example.com",
			BillingAddress: &pb.Address{
				Line1:    "123 Main St",
				City:     "Seattle",
				Zipcode:  "98101",
				Country:  "US",
			},
		},
		Store: &pb.Store{
			StoreId:   "STORE-001",
			StoreName: "Seattle Store",
			Address: &pb.Address{
				Line1:    "456 Pine Ave",
				City:     "Seattle",
				Zipcode:  "98102",
				Country:  "US",
			},
		},
	})
	if err != nil {
		return fmt.Errorf("create failed: %w", err)
	}

	// READ - Get the transaction we just created
	if createResp.Transaction != nil {
		if _, err := client.GetTransaction(createResp.Transaction.Id); err != nil {
			return fmt.Errorf("get failed: %w", err)
		}
	}

	// LIST - List all transactions
	if _, err := client.ListTransactions(10); err != nil {
		return fmt.Errorf("list failed: %w", err)
	}

	// DELETE - Delete the transaction
	if createResp.Transaction != nil {
		if _, err := client.DeleteTransaction(createResp.Transaction.Id); err != nil {
			return fmt.Errorf("delete failed: %w", err)
		}
	}

	return nil
}

func main() {
	if len(os.Args) > 1 && os.Args[1] == "help" {
		fmt.Println(`
Payment Transaction Client - Go Client for Java Server

Usage:
  go run main.go           # Run CRUD demo
  go run main.go help      # Show this help

Expected Java Server:
  Running on http://localhost:8080
  With endpoints:
    POST /api/transactions/create
    POST /api/transactions/get
    POST /api/transactions/list
    POST /api/transactions/delete

CRUD Operations:
  CREATE: Creates a new transaction
  READ:   Gets an existing transaction
  LIST:   Lists all transactions
  DELETE: Deletes a transaction

Proto Format:
  All requests/responses use Protocol Buffer binary format
  Content-Type: application/x-protobuf
		`)
		return
	}

	fmt.Println("🚀 Payment Transaction Client (Go)")
	fmt.Println("================================")

	client := NewPaymentClient("http://localhost:8080")

	if err := runCRUDDemo(client); err != nil {
		log.Fatalf("❌ Error: %v", err)
	}

	fmt.Println("\n✅ All CRUD operations completed successfully!")
}
