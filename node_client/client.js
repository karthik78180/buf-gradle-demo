/*
 * Node.js Client for Payment Transaction API
 * Demonstrates GetTransaction operation using Protocol Buffers
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:8080';

// Endpoint
const GET_ENDPOINT = `${BASE_URL}/api/transactions/get`;

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
 * GET Transaction by store name
 */
async function getTransaction(pbFilePath) {
  console.log('\n=== GET TRANSACTION ===');

  try {
    const binaryData = fs.readFileSync(pbFilePath);
    console.log(`Request size: ${binaryData.length} bytes`);

    const response = await makeRequest(GET_ENDPOINT, binaryData);
    console.log(`✅ GET successful!`);
    console.log(`   Response size: ${response.size} bytes`);

    return response;
  } catch (error) {
    console.error(`❌ GET failed: ${error.message}`);
    throw error;
  }
}

/**
 * Run GET Demo for multiple stores
 */
async function runGetDemo() {
  const resourceDir = path.join(__dirname, '..', 'src', 'test', 'resources');
  const stores = ['get_transaction_request.pb', 'get_transaction_request_mcdonalds.pb', 'get_transaction_request_walmart.pb', 'get_transaction_request_amazon.pb'];

  try {
    for (const storeFile of stores) {
      const filePath = path.join(resourceDir, storeFile);
      if (fs.existsSync(filePath)) {
        await getTransaction(filePath);
      }
    }

    console.log('\n✅ All GET operations completed successfully!');
  } catch (error) {
    console.error('\n❌ GET demo failed');
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
  node client.js           # Run GET demo for multiple stores
  node client.js --help    # Show this help

Expected Java Server:
  Running on http://localhost:8080
  With endpoint:
    POST /api/transactions/get (takes store_name, returns Transaction)

Operation:
  GET: Retrieves transaction by store_name and returns updated Transaction

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
  runGetDemo();
}

module.exports = {
  getTransaction,
  makeRequest
};
