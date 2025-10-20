module go.example.com/payments-client

go 1.21

require (
	go.example.com/payments-client/gen v0.0.0-00010101000000-000000000000
	google.golang.org/protobuf v1.31.0
)

require golang.org/x/xerrors v0.0.0-20220907171357-04be3eba64a2 // indirect

// Local proto bindings generated in gen/ directory
replace go.example.com/payments-client/gen => ./gen/go.example.com/payments-client/gen
