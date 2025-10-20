/*
 * Go Client for Payment Transaction API
 * Demonstrates GetTransaction operation using Protocol Buffers
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
	// Server endpoint
	getTransactionURL = "http://localhost:8080/api/transactions/get"
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

// GetTransaction retrieves a transaction by store_name
func (c *PaymentClient) GetTransaction(storeName string) (*pb.GetTransactionResponse, error) {
	fmt.Println("\n=== GET TRANSACTION ===")

	// Build request
	request := &pb.GetTransactionRequest{
		StoreName: storeName,
	}

	// Serialize to binary
	data, err := proto.Marshal(request)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal: %w", err)
	}

	fmt.Printf("Request size: %d bytes\n", len(data))
	fmt.Printf("Looking up store: %s\n", storeName)

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

	fmt.Printf("Response size: %d bytes\n", len(respBody))
	fmt.Println("✅ Transaction retrieved successfully")
	if response.Transaction != nil {
		fmt.Printf("   ID: %s\n", response.Transaction.Id)
		fmt.Printf("   Store Name: %s\n", response.Transaction.StoreName)
		fmt.Printf("   Amount: $%.2f %s\n", response.Transaction.Amount, response.Transaction.Currency)
		fmt.Printf("   Location Zip: %s\n", response.Transaction.LocationZip)
	}

	return response, nil
}

// Demo: Test with different store names
func runDemo(client *PaymentClient) error {
	stores := []string{"Starbucks", "McDonald's", "Walmart", "Amazon"}

	for _, store := range stores {
		if _, err := client.GetTransaction(store); err != nil {
			return fmt.Errorf("get transaction failed for %s: %w", store, err)
		}
	}

	return nil
}

func main() {
	if len(os.Args) > 1 && os.Args[1] == "help" {
		fmt.Println(`
Payment Transaction Client - Go Client for Java Server

Usage:
  go run main.go           # Run GET demo for multiple stores
  go run main.go help      # Show this help

Expected Java Server:
  Running on http://localhost:8080
  With endpoint:
    POST /api/transactions/get (takes store_name, returns Transaction)

Operation:
  GET: Retrieves transaction by store_name and returns updated Transaction

Proto Format:
  All requests/responses use Protocol Buffer binary format
  Content-Type: application/x-protobuf
	`)
		return
	}

	fmt.Println("🚀 Payment Transaction Client (Go)")
	fmt.Println("================================")

	client := NewPaymentClient("http://localhost:8080")

	if err := runDemo(client); err != nil {
		log.Fatalf("❌ Error: %v", err)
	}

	fmt.Println("\n✅ All GET operations completed successfully!")
}
