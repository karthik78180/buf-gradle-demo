/*
 * Integration Test for Node.js Client
 * Tests Node client against Java server using binary proto format
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:8080';
const PROTO_RESOURCES = path.join(__dirname, '..', 'src', 'test', 'resources');

/**
 * Make HTTP POST request
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
          reject(new Error(`HTTP ${res.statusCode}`));
        }
      });
    });

    req.on('error', reject);
    req.write(binaryData);
    req.end();
  });
}

/**
 * Test helper
 */
async function testOperation(name, filePath) {
  try {
    const endpoint = `${BASE_URL}/api/transactions/get`;
    const binaryData = fs.readFileSync(filePath);

    const response = await makeRequest(endpoint, binaryData);

    console.log(`✅ ${name.toUpperCase()} successful!`);
    console.log(`   Request size:  ${binaryData.length}B`);
    console.log(`   Response size: ${response.size}B`);

    return true;
  } catch (error) {
    console.error(`❌ ${name.toUpperCase()} failed: ${error.message}`);
    return false;
  }
}

/**
 * Check if server is running
 */
function checkServer() {
  return new Promise((resolve) => {
    const req = http.request(
      `${BASE_URL}/api/transactions/get`,
      { method: 'POST', headers: { 'Content-Type': 'application/x-protobuf' } },
      () => resolve(true)
    );
    req.on('error', () => resolve(false));
    req.end();
  });
}

/**
 * Run integration tests
 */
async function runTests() {
  console.log('🧪 Integration Test: Node.js Client + Binary Proto');
  console.log('================================================\n');

  // Check server
  console.log('✓ Checking if Java server is running on localhost:8080...');
  const serverRunning = await checkServer();

  if (!serverRunning) {
    console.error('❌ Server not responding. Start Java server first:');
    console.error('   ../../../gradlew run');
    process.exit(1);
  }

  console.log('✅ Server is running!\n');

  // List resources
  console.log('📁 Proto resource files:');
  const files = fs.readdirSync(PROTO_RESOURCES).filter(f => f.startsWith('get_transaction_request') && f.endsWith('.pb'));
  files.forEach(f => {
    const filePath = path.join(PROTO_RESOURCES, f);
    const size = fs.statSync(filePath).size;
    console.log(`   ${f} (${size}B)`);
  });
  console.log('');

  // Run tests
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  console.log('Test 1: GET Transaction (Starbucks)');
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  const test1Ok = await testOperation('get', path.join(PROTO_RESOURCES, 'get_transaction_request.pb'));
  console.log('');

  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  console.log("Test 2: GET Transaction (McDonald's)");
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  const test2Ok = await testOperation('get', path.join(PROTO_RESOURCES, 'get_transaction_request_mcdonalds.pb'));
  console.log('');

  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  console.log('Test 3: GET Transaction (Walmart)');
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  const test3Ok = await testOperation('get', path.join(PROTO_RESOURCES, 'get_transaction_request_walmart.pb'));
  console.log('');

  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  console.log('Test 4: GET Transaction (Amazon)');
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
  const test4Ok = await testOperation('get', path.join(PROTO_RESOURCES, 'get_transaction_request_amazon.pb'));
  console.log('');

  // Summary
  if (test1Ok && test2Ok && test3Ok && test4Ok) {
    console.log('═══════════════════════════════════════════');
    console.log('✅ ALL INTEGRATION TESTS PASSED!');
    console.log('═══════════════════════════════════════════');
    console.log('');
    console.log('Summary:');
    console.log('  ✓ Node.js HTTP client working');
    console.log('  ✓ Binary proto format (application/x-protobuf)');
    console.log('  ✓ GET operations working');
    console.log('  ✓ Request/Response serialization');
    console.log('');
  } else {
    console.log('═══════════════════════════════════════════');
    console.log('❌ SOME TESTS FAILED');
    console.log('═══════════════════════════════════════════');
    process.exit(1);
  }
}

// Run tests
runTests().catch(error => {
  console.error('Test error:', error);
  process.exit(1);
});
