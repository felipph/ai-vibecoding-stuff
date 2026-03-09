# Performance Optimization Guide

## Aggregate Size

**Problem:** `repository.save(order)` performs DELETE + INSERT on all child rows.
Large `@MappedCollection` sets make this very expensive.

**Solutions:**
- Keep owned collections small (< 50 items as a guideline)
- Use a custom repository with targeted SQL for partial updates:

```sql
-- :name UpdateOrderItemQuantity
UPDATE order_items
SET quantity = :quantity
WHERE id = :itemId AND order_id = :orderId
```

- Consider whether the collection is truly part of the aggregate or should be a standalone aggregate

## Batch Inserts

### batchUpdate (recommended)

```java
SqlParameterSource[] params = products.stream()
    .map(p -> new MapSqlParameterSource()
        .addValue("code",   p.code())
        .addValue("name",   p.name())
        .addValue("price",  p.price())
        .addValue("status", p.status().name()))
    .toArray(SqlParameterSource[]::new);

jdbc.batchUpdate(
    "INSERT INTO products (code, name, price, status) VALUES (:code, :name, :price, :status)",
    params
);
```

### Configure JDBC batch size

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
  jdbc:
    template:
      fetch-size: 100
      # For batch operations, Spring Data JDBC uses the driver default
      # Configure at driver level or use batchUpdate directly
```

### saveAll() for aggregate roots

```java
// Spring Data JDBC uses batch inserts for new entities in saveAll()
List<ProductEntity> products = createManyProducts();
repository.saveAll(products);
// Emits one INSERT per entity but in a single transaction batch
```

## Avoiding N+1 Reads

Spring Data JDBC loads full aggregates (including `@MappedCollection`) in one or more
queries. For **read views**, use a CQRS Query Service instead:

**Problem:** Loading 100 Orders to display a summary list also loads all OrderItems:
```java
// ❌ Loads all order items for all 100 orders
List<Order> orders = orderRepository.findByUserId(userId);
```

**Solution:** Query Service returns flat View Models:
```sql
-- :name FindOrderSummariesByUser
SELECT o.id, o.order_number, o.total, o.status,
       COUNT(oi.id) AS item_count
FROM orders o
LEFT JOIN order_items oi ON oi.order_id = o.id
WHERE o.user_id = :userId
GROUP BY o.id, o.order_number, o.total, o.status
ORDER BY o.created_at DESC
```

## Read vs Write Transactions

```java
@Service
@Transactional(readOnly = true)   // class-level default for reads
public class ProductQueryService {

    public List<ProductVM> findActive() { /* ... */ }

    // No override needed — already readOnly
    public Optional<ProductVM> findById(Long id) { /* ... */ }
}

@Service
@Transactional(readOnly = true)
public class ProductService {

    public ProductVM getById(Long id) { /* read */ }

    @Transactional    // override for writes
    public void create(CreateProductCommand cmd) { /* ... */ }

    @Transactional
    public void deactivate(Long id) { /* ... */ }
}
```

`readOnly = true` lets the driver skip flush checks and allows the connection pool to
route reads to replicas when using routing data sources.

## Pagination

Always paginate large result sets. Use `:offsetfetch` in ElSql:

```sql
-- :name FindActive
SELECT id, code, name, price
FROM products
WHERE status = 'ACTIVE'
ORDER BY created_at DESC
:offsetfetch(:offset, :fetch)
```

```java
MapSqlParameterSource params = new MapSqlParameterSource()
    .addValue("offset", (long) page * size)
    .addValue("fetch",  size);
```

**Keyset pagination (better performance for large offsets):**

```sql
-- :name FindActiveKeyset
SELECT id, code, name, price, created_at
FROM products
WHERE status = 'ACTIVE'
  AND (created_at, id) < (:lastCreatedAt, :lastId)   -- keyset condition
ORDER BY created_at DESC, id DESC
LIMIT :fetch
```

## Connection Pool Sizing

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10          # CPU cores × 2 + disk spindles
      minimum-idle: 5
      connection-timeout: 20000      # ms
      idle-timeout: 300000           # 5 min
      max-lifetime: 1200000          # 20 min
```

## Indexes

Create indexes aligned with your most frequent ElSql filter conditions:

```sql
-- Covering index for common active product list
CREATE INDEX idx_products_status_created
    ON products (status, created_at DESC)
    INCLUDE (id, code, name, price);

-- Partial index for active products only
CREATE INDEX idx_products_active
    ON products (created_at DESC)
    WHERE status = 'ACTIVE';
```

## Fetch Size for Large Reads

When streaming large result sets, configure fetch size:

```java
// Via JdbcTemplate directly
jdbcTemplate.setFetchSize(500);

// Or per-query using StatementCreator
jdbc.query(
    con -> {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setFetchSize(500);
        return ps;
    },
    rowMapper
);
```

## Observability

Enable SQL logging during development:

```yaml
logging:
  level:
    org.springframework.jdbc.core: DEBUG
    org.springframework.jdbc.core.StatementCreatorUtils: TRACE
```

For production, use Micrometer with a JDBC `DataSource` proxy:

```xml
<dependency>
    <groupId>net.ttddyy.observation</groupId>
    <artifactId>datasource-micrometer-spring-boot</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Performance Checklist

- [ ] Aggregate collections are small (< 50 items)
- [ ] No `repository.findAll()` without pagination
- [ ] Read views use Query Service, not repositories loading full aggregates
- [ ] `@Transactional(readOnly = true)` on all read-only services
- [ ] `batchUpdate` for bulk inserts (not `save()` in a loop)
- [ ] Indexes aligned with ElSql `:and` filter conditions
- [ ] Connection pool sized correctly for workload
- [ ] Fetch size configured for large streaming queries
- [ ] COUNT query paired with every paginated SELECT
- [ ] Keyset pagination considered for high-offset queries
