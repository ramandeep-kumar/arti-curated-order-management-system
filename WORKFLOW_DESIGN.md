This document provides comprehensive technical details on ArtiCurated's state machines, messaging flows, database schema, APIs, and operational guidance, including enhanced diagrams and implementation specifics.

1) Order State Machine
   Mermaid Diagram
   text
   stateDiagram-v2
   [*] --> PENDING_PAYMENT
   PENDING_PAYMENT --> PAID : PAYMENT_RECEIVED
   PENDING_PAYMENT --> CANCELLED : CANCEL_ORDER
   PAID --> PROCESSING_IN_WAREHOUSE : START_PROCESSING
   PAID --> CANCELLED : CANCEL_ORDER
   PROCESSING_IN_WAREHOUSE --> SHIPPED : SHIP_ORDER<br/>(GenerateInvoiceEvent)
   SHIPPED --> DELIVERED : DELIVER_ORDER
   DELIVERED --> [*]
   CANCELLED --> [*]
   ASCII Compact View
   text
   PENDING_PAYMENT
   ├─(PAYMENT_RECEIVED)─► PAID
   └─(CANCEL_ORDER)──────► CANCELLED

PAID
├─(START_PROCESSING)──► PROCESSING_IN_WAREHOUSE
└─(CANCEL_ORDER)──────► CANCELLED

PROCESSING_IN_WAREHOUSE
└─(SHIP_ORDER)────────► SHIPPED (GenerateInvoiceEvent)

SHIPPED
└─(DELIVER_ORDER)─────► DELIVERED (terminal; returns enabled)
Technical Details:

Configured via OrderStateMachineConfig.java using Spring Statemachine

shipOrderAction executes post-transition: applicationEventPublisher.publishEvent(new GenerateInvoiceEvent(orderId))

Validation: OrderServiceImpl#validateTransition(orderId, event) checks business rules before delegation to state machine

CANCELLED is absorbing state; no further transitions allowed

2) Return State Machine
   Mermaid Diagram
   text
   stateDiagram-v2
   [*] --> REQUESTED
   REQUESTED --> APPROVED : APPROVE (manager)
   REQUESTED --> REJECTED : REJECT (manager)
   APPROVED --> IN_TRANSIT : SHIP_BACK
   IN_TRANSIT --> RECEIVED : RECEIVE_ITEM
   RECEIVED --> COMPLETED : PROCESS_REFUND<br/>(ProcessRefundEvent)
   REJECTED --> [*]
   COMPLETED --> [*]
   ASCII Compact View
   text
   REQUESTED
   ├─(APPROVE)───► APPROVED   (manager)
   └─(REJECT)───► REJECTED   (manager)

APPROVED
└─(SHIP_BACK)─► IN_TRANSIT

IN_TRANSIT
└─(RECEIVE_ITEM)► RECEIVED

RECEIVED
└─(PROCESS_REFUND)► COMPLETED (ProcessRefundEvent; terminal)
Technical Details:

Creation guard: ReturnServiceImpl#createReturn() → orderService.findById(orderId).canBeReturned() (DELIVERED + time window)

Manager actions require @PreAuthorize("hasRole('MANAGER')") on controller methods

processRefundAction → ProcessRefundEvent(returnId) post-persistence

3) Messaging & Background Jobs
   Detailed Invoice Flow (Sequence Diagram Style)
   text
   Order(SHIPPED) ── shipOrderAction ──► ApplicationEventPublisher
   │
   ▼ @Async
   ApplicationEventHandler ──► GenerateInvoiceEventMapper
   │
   ▼ supports(GenerateInvoiceEvent)
   MessageProducer.sendInvoiceGenerationMessage(orderId)
   │
   ▼ RabbitMQ
   articurated.exchange[invoice.generate] ──► InvoiceMessageConsumer
   │
   ▼ @RabbitListener
   Generate PDF ──► Send Email (customer)
   Detailed Refund Flow
   text
   Return(COMPLETED) ── processRefundAction ──► ApplicationEventPublisher
   │
   ▼ @Async
   ApplicationEventHandler ──► ProcessRefundEventMapper
   │
   ▼ RabbitMQ
   articurated.exchange[refund.process] ──► RefundMessageConsumer
   │
   ▼
   Simulate PaymentGateway.refund(returnId, amount)
   EventMapper Interface:

java
public interface EventMapper {
boolean supports(Object event);
void mapAndSend(Object event);
}
RabbitMQ Config (RabbitMQConfig.java):

text
exchange: articurated.exchange (direct)
queues:
- invoice.generate.queue (durable, bound to invoice.generate)
- refund.process.queue (durable, bound to refund.process)
4) Database Schema & State History
   Enhanced ERD (Mermaid)
   text
   erDiagram
   orders ||--o{ order_items : contains
   orders ||--o{ order_state_history : "tracks"
   orders ||--o{ returns : "has"
   returns ||--o{ return_state_history : "tracks"

   orders {
   uuid id PK
   string order_number
   string customer_email
   decimal subtotal
   decimal tax
   decimal shipping
   decimal total
   string current_state
   timestamp created_at
   timestamp updated_at
   }

   order_items {
   uuid id PK
   uuid order_id FK
   string product_name
   decimal price
   int quantity
   decimal total
   }
   State History Transaction Pattern
   sql
   -- Pseudocode transaction
   BEGIN;
   SELECT current_state FROM orders WHERE id = ? FOR UPDATE;
   -- Validate transition via state machine
   UPDATE orders SET current_state = 'SHIPPED', updated_at = NOW() WHERE id = ?;
   INSERT INTO order_state_history
   (order_id, from_state, to_state, changed_by, reason, changed_at, metadata)
   VALUES (?, 'PROCESSING_IN_WAREHOUSE', 'SHIPPED', ?, ?, NOW(), ?::jsonb);
   COMMIT;
   -- Post-commit: publish GenerateInvoiceEvent
   Recommended Indexes:

sql
CREATE INDEX idx_order_history_timeline ON order_state_history(order_id, changed_at DESC);
CREATE INDEX idx_return_history_timeline ON return_state_history(return_id, changed_at DESC);
5) API Contract & Controllers
   Order Controller (OrderController.java)
   java
   @RestController
   @RequestMapping("/api/orders")
   public class OrderController {
   @PostMapping → OrderService.createOrder(CreateOrderRequest)
   @GetMapping("/{id}") → OrderDto
   @PutMapping("/{id}/transition")
   public ResponseEntity<?> transition(@PathVariable UUID id, 
                                       @RequestParam String event,
                                       @AuthenticationPrincipal User user) {
        // Delegates to OrderService.applyTransition(id, event, user)
    }
    @GetMapping("/{id}/history") → List<OrderStateHistoryDto>
}
Return Controller (Manager Endpoints)
java
@PutMapping("/{id}/approve")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<?> approve(@PathVariable UUID id,
   @RequestParam String approvedBy) {
   returnService.approveReturn(id, approvedBy);
   }
   Postman Collection: docs/POSTMAN_COLLECTION_FULL.json (includes auth headers, payloads)

6) Integration Testing Setup
   Testcontainers Compose
   text
   services:
   postgres:
   image: postgres:15
   environment:
   POSTGRES_DB: articurated
   rabbitmq:
   image: rabbitmq:3-management
   ports: ["15672:15672"]
   Test Class Example:

java
@SpringBootTest
@Testcontainers
class OrderIntegrationTest {
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void orderFullLifecycle() {
        // Create → Pay → Process → Ship → Verify RabbitMQ message
    }
}
7) Production Operations
   Monitoring & Alerts
   State Transition Lag: Query MAX(updated_at) - MAX(changed_at) on history tables

Message Backlog: RabbitMQ queue depth metrics

Dead Letter Handling: articurated.dlx exchange for failed messages

Scaling Recommendations
text
High Volume:
├── Shard orders by customer_id or region
├── Separate history tables (append-only → ClickHouse/S3)
├── Async state machine transitions via dedicated queue
└── Read replicas for history queries
