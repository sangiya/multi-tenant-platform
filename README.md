# Multi-Tenant Platform

Production-grade multi-tenancy implementation using `X-Tenant-ID` header routing and per-tenant data isolation via `AbstractRoutingDataSource`. Each tenant operates on a completely isolated data store with no cross-tenant data leakage.

## Architecture

```
HTTP Request
    │
    ▼
TenantFilter (Order 1)
    ├── Reads X-Tenant-ID header
    ├── Validates against TenantRegistry
    ├── Sets TenantContext (ThreadLocal)
    └── Clears context in finally block
         │
         ▼
ProductController → ProductService → ProductRepository
                                          │
                                          ▼
                               TenantRoutingDataSource
                                    │           │
                               alpha DB      beta DB
                              (H2 mem)      (H2 mem)
```

## Tenant Isolation Guarantees

| Isolation Level | Mechanism |
|----------------|-----------|
| Request routing | `X-Tenant-ID` header, validated per request |
| Data isolation | Separate DataSource instance per tenant |
| Context leakage prevention | `finally` block clears `ThreadLocal` on every request |
| Unknown tenant rejection | 403 before any DB access |

## Production Upgrade Path

For PostgreSQL, replace each tenant's H2 `EmbeddedDatabase` with a `DataSourceBuilder` pointing to the tenant's schema:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/platform
# Each tenant connects with SET search_path = tenant_alpha
```

Use Hibernate's `SCHEMA` multi-tenancy strategy with `MultiTenantConnectionProvider` to set `search_path` per connection, enabling true schema-per-tenant isolation on a single PostgreSQL instance.

## API

All endpoints require the `X-Tenant-ID` header.

```
POST   /api/v1/products              Create product
GET    /api/v1/products              List all products
GET    /api/v1/products?search=term  Search by name
GET    /api/v1/products/{id}         Get product by ID
PUT    /api/v1/products/{id}         Update product
DELETE /api/v1/products/{id}         Delete product
```

### Example

```bash
# Create product for tenant alpha
curl -X POST http://localhost:8080/api/v1/products \
  -H "X-Tenant-ID: alpha" \
  -H "Content-Type: application/json" \
  -d '{"name":"Widget","description":"A widget","price":9.99,"stockQuantity":100}'

# Read from beta — returns 404 (isolated)
curl http://localhost:8080/api/v1/products/{id} \
  -H "X-Tenant-ID: beta"
```

## Running Locally

```bash
mvn spring-boot:run
```

Tenants configured in `application.yml`:
```yaml
multitenancy:
  tenants: alpha,beta,gamma
  default-tenant: alpha
```

## Testing

```bash
mvn test
```

Test coverage:
- `TenantContextTest` — ThreadLocal set/get/clear lifecycle
- `TenantRegistryTest` — tenant validation and case normalization
- `TenantFilterTest` — missing header (400), unknown tenant (403), non-API path bypass, context cleanup
- `ProductServiceTest` — CRUD logic with mocked repository
- `ProductControllerTest` — validation, 404 error mapping via `MockMvcBuilders.standaloneSetup`
- `MultiTenantIsolationTest` — full-stack isolation proof: alpha product invisible to beta, independent catalogs per tenant

## Tech Stack

- Java 21, Spring Boot 3.3.5
- Spring Data JPA + `AbstractRoutingDataSource`
- H2 in-memory databases (one per tenant)
- Spring Boot Actuator: `/actuator/health`, `/actuator/metrics`
