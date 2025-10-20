/*
 * Node.js Client for Payment Transaction API
 * Demonstrates CRUD operations using Protocol Buffers
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:8080';

// CRUD Endpoints
const ENDPOINTS = {
  create: `${BASE_URL}/api/transactions/create`,
  get: `${BASE_URL}/api/transactions/get`,
  list: `${BASE_URL}/api/transactions/list`,
  delete: `${BASE_URL}/api/transactions/delete`
};

/**
 * Make HTTP POST request with binary proto
 */
function makeRequest(endpoint, binaryData) {
  return new Promise((resolve, reject) => {
    const url = new URL(endpoint);

    const options = {
      hostname: url.hostname,
      port: url.port || 80,
      path: url.pathname,
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-protobuf',
        'Content-Length': binaryData.length
      }
    };

    const req = http.request(options, (res) => {
      let data = Buffer.alloc(0);

      res.on('data', (chunk) => {
        data = Buffer.concat([data, chunk]);
      });

      res.on('end', () => {
        if (res.statusCode === 200) {
          resolve({
            statusCode: res.statusCode,
            data: data,
            size: data.length
          });
        } else {
          reject(new Error(`HTTP ${res.statusCode}: ${data.toString()}`));
        }
      });
    });

    req.on('error', reject);
    req.write(binaryData);
    req.end();
  });
}

/**
 * CREATE Transaction
 */
async function createTransaction(pbFilePath) {
  console.log('\n=== CREATE TRANSACTION ===');

  try {
    const binaryData = fs.readFileSync(pbFilePath);
    console.log(`Request size: ${binaryData.length} bytes`);

    const response = await makeRequest(ENDPOINTS.create, binaryData);
    console.log(`✅ CREATE successful!`);
    console.log(`   Response size: ${response.size} bytes`);

    return response;
  } catch (error) {
    console.error(`❌ CREATE failed: ${error.message}`);
    throw error;
  }
}

/**
 * GET Transaction
 */
async function getTransaction(pbFilePath) {
  console.log('\n=== GET TRANSACTION ===');

  try {
    const binaryData = fs.readFileSync(pbFilePath);
    console.log(`Request size: ${binaryData.length} bytes`);

    const response = await makeRequest(ENDPOINTS.get, binaryData);
    console.log(`✅ GET successful!`);
    console.log(`   Response size: ${response.size} bytes`);

    return response;
  } catch (error) {
    console.error(`❌ GET failed: ${error.message}`);
    throw error;
  }
}

/**
 * LIST Transactions
 */
async function listTransactions(pbFilePath) {
  console.log('\n=== LIST TRANSACTIONS ===');

  try {
    const binaryData = fs.readFileSync(pbFilePath);
    console.log(`Request size: ${binaryData.length} bytes`);

    const response = await makeRequest(ENDPOINTS.list, binaryData);
    console.log(`✅ LIST successful!`);
    console.log(`   Response size: ${response.size} bytes`);

    return response;
  } catch (error) {
    console.error(`❌ LIST failed: ${error.message}`);
    throw error;
  }
}

/**
 * DELETE Transaction
 */
async function deleteTransaction(pbFilePath) {
  console.log('\n=== DELETE TRANSACTION ===');

  try {
    const binaryData = fs.readFileSync(pbFilePath);
    console.log(`Request size: ${binaryData.length} bytes`);

    const response = await makeRequest(ENDPOINTS.delete, binaryData);
    console.log(`✅ DELETE successful!`);
    console.log(`   Response size: ${response.size} bytes`);

    return response;
  } catch (error) {
    console.error(`❌ DELETE failed: ${error.message}`);
    throw error;
  }
}

/**
 * Run CRUD Demo
 */
async function runCRUDDemo() {
  const resourceDir = path.join(__dirname, '..', 'src', 'test', 'resources');

  try {
    // CREATE
    await createTransaction(path.join(resourceDir, 'create_transaction_request.pb'));

    // GET
    await getTransaction(path.join(resourceDir, 'get_transaction_request.pb'));

    // LIST
    await listTransactions(path.join(resourceDir, 'list_transactions_request.pb'));

    // DELETE
    await deleteTransaction(path.join(resourceDir, 'delete_transaction_request.pb'));

    console.log('\n✅ All CRUD operations completed successfully!');
  } catch (error) {
    console.error('\n❌ CRUD demo failed');
    process.exit(1);
  }
}

/**
 * Show help
 */
function showHelp() {
  console.log(`
Payment Transaction Client - Node.js Client for Java Server

Usage:
  node client.js           # Run CRUD demo
  node client.js --help    # Show this help

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
  `);
}

// Main
if (process.argv.includes('--help') || process.argv.includes('help')) {
  showHelp();
} else {
  console.log('🚀 Payment Transaction Client (Node.js)');
  console.log('================================');
  runCRUDDemo();
}

module.exports = {
  createTransaction,
  getTransaction,
  listTransactions,
  deleteTransaction,
  makeRequest
};
