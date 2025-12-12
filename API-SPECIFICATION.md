# API-SPECIFICATION.md

This document provides human-readable examples and sample payloads for the main API endpoints implemented in this repository. Use these examples for manual testing, Postman, or integration test scripts.

Base URL: `http://localhost:8080` (adjust `base_url` when using Docker or other environments)

## Endpoints covered

- POST /api/orders — create an order
- GET /api/orders/{id} — fetch order by id
- PUT /api/orders/{id}/transition?event=... — apply state machine event
- GET /api/orders/{id}/history — fetch order state history
- POST /api/returns — create a return request
- PUT /api/returns/{id}/approve — approve a return
- GET /actuator/health — health check

---

## Create Order — example

Description: Creates a new order. The server will persist the order with the initial state `PENDING_PAYMENT`.

Request (JSON):

```json
{
  "customerEmail": "customer@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "items": [
    { "productName": "Coffee Mug", "price": 12.5, "quantity": 2 }
  ],
  "address": {
    "street": "123 Main St",
    "city": "Springfield",
    "state": "IL",
    "zipCode": "62704",
    "country": "USA"
  }
}
```

cURL example:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerEmail":"customer@example.com","firstName":"John","lastName":"Doe","items":[{"productName":"Coffee Mug","price":12.5,"quantity":2}],"address":{"street":"123 Main St","city":"Springfield","state":"IL","zipCode":"62704","country":"USA"}}'
```

Expected response (201 Created):

```json
{
  "id": 101,
  "orderNumber": "ARTI-000101",
  "customerEmail": "customer@example.com",
  "items": [ { "productName": "Coffee Mug", "price": 12.5, "quantity": 2, "total": 25.0 } ],
  "subtotal": 25.0,
  "tax": 2.25,
  "shipping": 5.0,
  "total": 32.25,
  "currentState": "PENDING_PAYMENT",
  "createdAt": "2025-08-19T12:00:00Z"
}
```

Notes:
- After creating the order you can call transition endpoints to move the order through the state machine (PAYMENT_RECEIVED, START_PROCESSING, SHIP_ORDER, DELIVER_ORDER, CANCEL_ORDER).

---

## Apply a state transition (example)

Endpoint: `PUT /api/orders/{id}/transition?event=SHIP_ORDER`

This will ask the order state machine to transition the order with the SHIP_ORDER event. If the transition succeeds the API returns the updated `Order` resource.

cURL example:

```bash
curl -X PUT "http://localhost:8080/api/orders/101/transition?event=SHIP_ORDER"
```

Successful response (200): updated `Order` JSON with `currentState: "SHIPPED"`.

---

## Create Return — example

Description: Create a return for a delivered order. Business rule: order must be in `DELIVERED` state (service will validate eligibility).

Request (JSON):

```json
{
  "orderId": 101,
  "reason": "Item arrived damaged"
}
```

cURL example:

```bash
curl -X POST http://localhost:8080/api/returns \
  -H "Content-Type: application/json" \
  -d '{"orderId":101,"reason":"Item arrived damaged"}'
```

Expected response (201 Created):

```json
{
  "id": 11,
  "returnNumber": "RET-00011",
  "orderId": 101,
  "reason": "Item arrived damaged",
  "currentState": "REQUESTED",
  "createdAt": "2025-08-19T13:00:00Z"
}
```

## Approve a return (manager)

Endpoint: `PUT /api/returns/{id}/approve?approvedBy=manager@example.com`

cURL example:

```bash
curl -X PUT "http://localhost:8080/api/returns/11/approve?approvedBy=manager@example.com"
```

Response (200): Updated `Return` resource with `currentState: "APPROVED"`.

---

## Invoice endpoints (examples)

Create invoice (manual/testing endpoint)

Request (JSON):

```json
{
  "orderId": 101
}
```

cURL example:

```bash
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -d '{"orderId":101}'
```

Expected response (201):

```json
{
  "id": 55,
  "orderId": 101,
  "status": "CREATED",
  "pdfUrl": null,
  "createdAt": "2025-08-19T12:10:00Z"
}
```

Mark invoice paid (convenience)

```bash
curl -X PUT "http://localhost:8080/api/invoices/55/pay?paidBy=acct@example.com"
```

Expected response (200): invoice with `status: "PAID"`.

Cancel invoice (convenience)

```bash
curl -X PUT "http://localhost:8080/api/invoices/55/cancel?reason=CustomerRequest"
```

Expected response (200): invoice with `status: "CANCELLED"`.

---

## Extended return flows (examples)

Mark return in-transit (supply tracking number)

```bash
curl -X PUT "http://localhost:8080/api/returns/11/in-transit?trackingNumber=TRK12345"
```

Mark return received (warehouse)

```bash
curl -X PUT "http://localhost:8080/api/returns/11/received"
```

Complete return (process refund)

```bash
curl -X PUT "http://localhost:8080/api/returns/11/complete"
```

Get refund status

```bash
curl http://localhost:8080/api/returns/11/refund
```

Expected response (200):

```json
{
  "status": "PROCESSING|COMPLETED|FAILED",
  "processedAt": "2025-08-19T13:10:00Z"
}
```

## Check health

```bash
curl http://localhost:8080/actuator/health
```

---

## Notes and tips

- Use the `POSTMAN_COLLECTION.json` file in this repo (compact collection) to import into Postman or Newman. Set the collection variable `base_url` to your runtime (e.g., `http://localhost:8080` or `http://host.docker.internal:8080`).
- When running via Docker Compose, use the compose file's credentials for Postgres and RabbitMQ (see `docker/docker-compose.yml`).
- For end-to-end tests, run the application and either rely on the embedded listeners or run a worker-only jar (`-Dspring.main.web-application-type=none`) to process messages.

If you want, I can also generate a full Postman collection with test scripts (save responses to variables, poll for invoices) matching `POSTMAN_COLLECTION_FULL.json` — tell me if you want that exact copy added as `POSTMAN_COLLECTION_FULL.json` as well.
