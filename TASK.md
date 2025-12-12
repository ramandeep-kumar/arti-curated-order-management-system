## SOLID Refactor Plan for ArtiCurated

Summary
-------
This document captures a short analysis of the existing codebase and a phase-wise implementation plan to refactor the project toward SOLID principles. It lists hotspots, concrete transformations, acceptance criteria, and an incremental sequence of safe changes.

Requirements checklist
- Analyze the codebase and identify SOLID violations (Done — see Findings).
- Produce a phase-wise implementation plan and tasks (`TASK.md`) (Done — this file).

Findings (high level)
- Large, multi-responsibility service classes:
  - `src/main/java/com/articurated/order/service/impl/OrderServiceImpl.java` (contains business logic + calculations + persistence orchestration + nested helper class)
  - `src/main/java/com/articurated/returns/service/impl/ReturnServiceImpl.java`
  - `src/main/java/com/articurated/invoice/service/impl/InvoiceServiceImpl.java`
- Messaging and event coupling
  - `MessageProducer` and `ApplicationEventHandler` combine event creation, transformation and delivery details.
- Controllers mix orchestration, validation and mapping: `OrderController`, `ReturnController`, `InvoiceController`.
- State machines (`OrderStateMachineConfig`, `ReturnStateMachineConfig`) are configuration-heavy and sometimes mix transition logic and side-effects.
- Some DTOs and small value objects are fine; repositories appear reasonably separated but should be depended on by interfaces rather than concrete service classes.

Common SOLID issues observed
- SRP: methods and classes have multiple responsibilities (calculation + persistence + messaging + mapping).
- OCP: business rules implemented inline (hard to extend without modifying existing classes).
- ISP: service interfaces can become fat when read/write and other concerns are combined.
- DIP: higher-level modules sometimes depend on concrete classes (e.g., direct usage of messaging template or concrete repos) instead of abstractions.

Transformations (concrete suggestions)
- Extract calculation logic (e.g., order amount calculation) into a small `OrderAmountCalculator` interface + implementation.
- Introduce small domain services (domain-level behaviors) vs application services (coordination/orchestration).
- Create explicit ports and adapters:
  - Define repository ports (interfaces) and keep JPA repositories as adapters.
  - Define messaging ports (e.g., `EventPublisher`) used by services; implement adapters for RabbitMQ.
- Introduce strategies/handlers for state transition side-effects (e.g., `RefundProcessor`, `InvoiceGenerator`) to follow OCP.
- Split large service interfaces into focused ones (e.g., `OrderReadService` / `OrderWriteService` / `OrderStateService`).
- Move event handling (transform + publish) to dedicated classes, keep controllers thin (accept/validate/forward).

Phase-wise implementation plan

Phase 0 — Discovery & safety net (1-2 days)
- Tasks:
  - Add/ensure unit test coverage for critical flows (order creation, return processing, invoice generation). If tests are missing, add minimal unit tests to lock behavior.
  - Add static analysis/formatter rules if not present.
- Acceptance:
  - Project builds and current tests pass locally.

Phase 1 — Small, low-risk extractions (1-2 days)
- Tasks:
  - Extract `OrderAmountCalculation` inner helper into `OrderAmountCalculator` (new interface + impl) and inject into `OrderServiceImpl`.
  - Move any small private helper functions from `ReturnServiceImpl` / `InvoiceServiceImpl` into dedicated helper classes where present.
- Files touched:
  - `OrderServiceImpl.java`, new `OrderAmountCalculator.java`, `OrderAmountCalculatorImpl.java`.
- Acceptance:
  - All unit tests pass. No behavioral change.

Phase 2 — Introduce ports & adapters (2-4 days)
- Tasks:
  - Define repository ports (e.g., `OrderPort`, `ReturnPort`, `InvoicePort`) and adapt existing `*Repository` implementations.
  - Introduce `EventPublisher` abstraction and implement RabbitMQ adapter that wraps `MessageProducer`.
  - Update services to depend on these abstractions via constructor injection.
- Acceptance:
  - Codebase compiles. Integration tests for messaging run (or mock publishers used in unit tests).

Phase 3 — Separate domain vs application services (3-5 days)

Goal
----
Make a clear separation between domain behavior (business rules, invariants, state transitions) and application orchestration (transactions, persistence coordination, event publishing). This improves testability and prepares the codebase for the remaining phases (state-transition handlers, messaging decoupling).

Tasks (concrete)
-----------------
- Introduce `DomainOrderService` (domain service):
  - Responsibilities: state transitions, domain invariants (canBeReturned, validation of state change), domain-only calculations that affect business invariants.
  - Should be pure Java (no Spring annotations), accept domain entities and return modified domain objects or domain events.
- Introduce `OrderAppService` (application service):
  - Responsibilities: start/commit transactions, call `DomainOrderService`, persist aggregates via `OrderPersistencePort`, and publish integration events via `EventPublisher`.
  - Should be Spring-managed and injectable into controllers.
- Move mapping responsibilities to mappers (if not already):
  - `OrderResponseMapper` / `OrderRequestMapper` — DTO ↔ domain mapping.
  - Keep these mappers small and testable; inject them into controllers and app services (not domain services).
- Update callers:
  - Replace direct calls to `OrderServiceImpl` with calls to `OrderAppService` where orchestration is needed.
  - Internal domain flows should call `DomainOrderService` directly in unit tests.

Files & edits (example)
-----------------------
- Add new files:
  - `src/main/java/com/articurated/order/service/domain/DomainOrderService.java` (interface)
  - `src/main/java/com/articurated/order/service/domain/DomainOrderServiceImpl.java`
  - `src/main/java/com/articurated/order/service/app/OrderAppService.java` (interface)
  - `src/main/java/com/articurated/order/service/app/OrderAppServiceImpl.java`
- Modify:
  - `src/main/java/com/articurated/order/service/impl/OrderServiceImpl.java` — either convert to `OrderAppServiceImpl` or replace usages with the new app service.
  - Controllers that previously injected `OrderServiceImpl` should now inject `OrderAppService` (keep constructors/backwards-compatible where possible to reduce PR blast radius).

Contract (tiny)
---------------
- DomainOrderService#processPayment(Order order, PaymentInfo info) -> DomainResult (success/failure + domain events)
- OrderAppService#createOrder(CreateOrderRequest) -> OrderResponse (handles transaction, persistence, events)

Edge cases to cover
-------------------
- Null / missing related entities (order without items, return with null order) — domain service should validate and throw domain-specific exceptions.
- Concurrent state transitions — ensure domain service checks current state before applying transitions and return deterministic results (idempotency where needed).
- Partial failure during orchestration: domain service succeeded but persistence or event publication fails; `OrderAppService` must handle rollback semantics (transaction + compensating actions or retries).

Tests to add/update
-------------------
- Unit tests for `DomainOrderServiceImpl` covering:
  - Valid state transitions (happy path).
  - Illegal transitions (expect `IllegalStateException` or a domain exception).
  - Business-rule edge cases (e.g., `canBeReturned` windows).
- Unit tests for `OrderAppServiceImpl` (use mocks for `DomainOrderService`, `OrderPersistencePort`, `EventPublisher`) covering:
  - Orchestration happy path: domain service returns success -> persistence called -> event published.
  - Persistence failure: ensure transaction rollback and no event published.
  - Event publication failure: depending on policy, ensure either retry or rollback; tests should assert expected behavior.
- Controller tests: ensure controllers still return the same DTOs and status codes when calling `OrderAppService`.

Acceptance criteria
-------------------
- Behavior: existing end-to-end and unit tests pass with no behavior changes.
- API: public controller endpoints remain backward-compatible.
- Code structure: domain business logic lives in `DomainOrderService` and is covered by fast unit tests. `OrderAppService` contains orchestration and is tested with mocks.

Estimates & timeline
--------------------
- Estimated effort: 3–5 working days.
  - Day 1: scaffold interfaces and implementations, move a single simple flow (order creation) to the new services.
  - Day 2: finish moving remaining flows (payment, cancellation) and update controllers.
  - Day 3: add unit tests for domain service and app service, run integration tests.

Verification steps (quality gates)
--------------------------------
1. Build & tests: `mvn -DskipTests=false test` (all unit tests must pass).
2. Run a small integration scenario (if available) or run the existing integration tests.
3. Manual smoke: create an order, simulate payment, and verify events persisted/published (or rely on mocked verification in tests).

Risks & mitigations
-------------------
- Risk: large constructor and wiring changes in many places. Mitigation: keep temporary adapter constructors that accept the old type and delegate to new services for a transitional period.
- Risk: behavior changes due to split responsibilities. Mitigation: keep domain logic identical (copy then refactor) and rely on unit tests as a safety net.

Small implementation checklist (developer steps)
---------------------------------------------
1. Create `DomainOrderService` + `DomainOrderServiceImpl` and move pure business methods into it.
2. Create `OrderAppService` + `OrderAppServiceImpl` to orchestrate calling domain service, persisting via port, and publishing events.
3. Update controllers to inject `OrderAppService` instead of `OrderServiceImpl` (or add overloaded constructors to maintain compatibility).
4. Add unit tests for both new service layers.
5. Run `mvn test` and fix any regressions.

Notes
-----
This phase intentionally avoids large API changes; keep public contracts stable and prefer constructor deprecation / adapter layers when needed. After this phase the code will be ready to accept the `StateTransitionHandler` and messaging decoupling work in Phase 4–5.

Phase 4 — State transition strategy & OCP (3-6 days)
- Tasks:
  - Introduce `StateTransitionHandler` strategy interface for side-effects during state transitions.
  - Replace inline side-effects in state machine configs with handler registrations.
- Acceptance:
  - Can add new handlers without modifying existing core services.

Phase 5 — Messaging and event-driven decoupling (2-4 days)
- Tasks:
  - Replace direct usage of concrete message producers in services with `EventPublisher`.
  - Move event transformation into `EventMapper` implementations.
- Acceptance:
  - Services publish events via abstraction; messaging adapter unit-tested.

Phase 6 — Controller slimming and interface segregation (2-4 days)
- Tasks:
  - Make controllers thin: validation + mapping + call to app services.
  - Create smaller service interfaces used by controllers (read-only vs write operations).
- Acceptance:
  - Controllers unchanged in behavior; smaller public service interfaces.

Phase 7 — Cleanup, docs, and rollout (2 days)
- Tasks:
  - Remove deprecated classes, add Javadoc, update README with new module responsibilities.
  - Run full CI, integration tests, and run a smoke test.
- Acceptance:
  - CI green, no regressions in integration tests.

Quick wins (can be done early)
- Add or extract unit tests around `OrderServiceImpl` and `ReturnServiceImpl` critical methods.
- Replace nested helper classes with injectable beans to improve testability.

Quality gates (must pass before merging each phase)
- Build: `mvn -DskipTests=false clean package` — build passes.
- Lint/static analysis: (preferably run project's configured checks).
- Unit tests: added/updated tests pass.
- Smoke test: manual or scripted run for primary flows.

Risks & mitigations
- Risk: large refactors can introduce regressions. Mitigation: small incremental PRs per phase with tests.
- Risk: missing coverage on some flows. Mitigation: add targeted tests in Phase 0.

Next steps I can take for you
- If you'd like, I can implement Phase 1 now (add `OrderAmountCalculator` and update `OrderServiceImpl`), open a branch and a PR, and run unit tests.

Notes
- Estimates assume moderate familiarity with codebase and available tests. I made assumptions about responsibilities based on file locations and known class names; I can refine after deeper code edits.

Requirements coverage
- Analyze repo and propose transformations: Done (Findings + Transformations sections).
- Create phase-wise TASK.md: Done (this file).
