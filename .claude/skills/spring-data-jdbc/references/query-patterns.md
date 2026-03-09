# Query Patterns Reference

## Derived Query Methods

Use for simple lookups (1-2 fixed properties). Spring Data JDBC generates the SQL automatically.

```java
public interface ProductRepository extends ListCrudRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByCode(String code);

    List<ProductEntity> findByStatus(ProductStatus status);

    boolean existsByCode(String code);

    long countByStatus(ProductStatus status);
}
```

**When to use:** Only for trivial lookups. Do not chain long property paths — use `@Query` instead.

## @Query — Fixed SQL

Use when you have a small number of non-trivial but **fixed** queries (no dynamic conditions).

```java
public interface OrderRepository extends ListCrudRepository<OrderEntity, Long> {

    @Query("""
        SELECT o.id, o.order_number, o.user_id, o.status, o.total, o.created_at
        FROM orders o
        WHERE o.user_id = :userId
        ORDER BY o.created_at DESC
        """)
    List<OrderEntity> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT o.id, o.order_number, o.user_id, o.status, o.total, o.created_at
        FROM orders o
        WHERE o.status = :status
          AND o.created_at >= :since
        ORDER BY o.created_at DESC
        LIMIT :limit
        """)
    List<OrderEntity> findRecentByStatus(
        @Param("status") OrderStatus status,
        @Param("since") Instant since,
        @Param("limit") int limit
    );
}
```

**Limitations of @Query:**
- Cannot be conditional (no dynamic WHERE)
- SQL is embedded in source code (harder to review/tune)
- No reuse between fragments

**Alternative:** Move to an `.elsql` file as soon as you need anything conditional.

## ElSql Queries — Fixed SQL in External File

Same as `@Query` but SQL lives in an `.elsql` file. Preferred even for fixed queries.

**`.elsql` file:**
```sql
-- :name FindOrdersByUser
SELECT o.id, o.order_number, o.user_id, o.status, o.total, o.created_at
FROM orders o
WHERE o.user_id = :userId
ORDER BY o.created_at DESC
```

**Java:**
```java
@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    public List<OrderVM> findByUser(Long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        SqlFragments sql = bundle.getSql("FindOrdersByUser", params);
        return jdbc.query(sql.getSqlString(), sql.getParameters(), orderVMMapper());
    }
}
```

## ElSql Queries — Dynamic Conditions with :where/:and

Use `:where` + `:and(param, condition)` for optional filters.

**`.elsql` file:**
```sql
-- :name SearchProducts
SELECT id, code, name, price, status
FROM products
:where
  :and(:code, code = :code)
  :and(:name, name ILIKE :name)
  :and(:status, status = :status)
  :and(:minPrice, price >= :minPrice)
  :and(:maxPrice, price <= :maxPrice)
  :and(:categoryId, category_id = :categoryId)
ORDER BY name

-- :name CountSearchProducts
SELECT COUNT(*)
FROM products
:where
  :and(:code, code = :code)
  :and(:name, name ILIKE :name)
  :and(:status, status = :status)
  :and(:minPrice, price >= :minPrice)
  :and(:maxPrice, price <= :maxPrice)
  :and(:categoryId, category_id = :categoryId)
```

**Java:**
```java
public List<ProductVM> search(ProductSearchCriteria criteria) {
    MapSqlParameterSource params = buildParams(criteria);
    SqlFragments sql = bundle.getSql("SearchProducts", params);
    return jdbc.query(sql.getSqlString(), sql.getParameters(), productVMMapper());
}

private MapSqlParameterSource buildParams(ProductSearchCriteria criteria) {
    return new MapSqlParameterSource()
        .addValue("code",       criteria.code())
        .addValue("name",       criteria.name() != null ? "%" + criteria.name() + "%" : null)
        .addValue("status",     criteria.status() != null ? criteria.status().name() : null)
        .addValue("minPrice",   criteria.minPrice())
        .addValue("maxPrice",   criteria.maxPrice())
        .addValue("categoryId", criteria.categoryId());
    // null values → :and blocks are excluded
}
```

## Pagination with :offsetfetch

**`.elsql` file:**
```sql
-- :name FindActivePaged
SELECT id, code, name, price, status, created_at
FROM products
WHERE status = 'ACTIVE'
ORDER BY created_at DESC
:offsetfetch(:offset, :fetch)

-- :name CountActive
SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'
```

**Java:**
```java
public Page<ProductVM> findActivePage(int page, int size) {
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("offset", (long) page * size)
        .addValue("fetch",  size);

    Long total = jdbc.queryForObject(
        bundle.getSql("CountActive", new MapSqlParameterSource()).getSqlString(),
        new MapSqlParameterSource(),
        Long.class
    );

    List<ProductVM> content = jdbc.query(
        bundle.getSql("FindActivePaged", params).getSqlString(),
        params,
        productVMMapper()
    );

    return new PageImpl<>(content, PageRequest.of(page, size), total != null ? total : 0);
}
```

## IN Clause

Use a `List` parameter — Spring's `NamedParameterJdbcTemplate` expands it automatically:

**`.elsql` file:**
```sql
-- :name FindByIds
SELECT id, code, name, price FROM products
WHERE id IN (:ids)
```

**Java:**
```java
MapSqlParameterSource params = new MapSqlParameterSource("ids", List.of(1L, 2L, 3L));
SqlFragments sql = bundle.getSql("FindByIds", params);
List<ProductVM> results = jdbc.query(sql.getSqlString(), sql.getParameters(), mapper);
```

## Aggregations

**`.elsql` file:**
```sql
-- :name StatsPerCategory
SELECT
    c.name     AS category_name,
    COUNT(p.id) AS product_count,
    AVG(p.price) AS avg_price,
    MIN(p.price) AS min_price,
    MAX(p.price) AS max_price
FROM products p
JOIN categories c ON c.id = p.category_id
WHERE p.status = 'ACTIVE'
GROUP BY c.id, c.name
ORDER BY product_count DESC
```

## RowMapper Patterns

### Lambda mapper (simple, inline)

```java
RowMapper<ProductVM> mapper = (rs, rowNum) -> new ProductVM(
    rs.getLong("id"),
    rs.getString("code"),
    rs.getString("name"),
    rs.getBigDecimal("price")
);
```

### Named class mapper (reusable)

```java
class ProductVMMapper implements RowMapper<ProductVM> {
    @Override
    public ProductVM mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProductVM(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBigDecimal("price"),
            ProductStatus.valueOf(rs.getString("status"))
        );
    }
}
```

### Static factory method on the record

```java
public record ProductVM(Long id, String code, String name, BigDecimal price) {

    public static RowMapper<ProductVM> rowMapper() {
        return (rs, rowNum) -> new ProductVM(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBigDecimal("price")
        );
    }
}
```

## Convenience Wrapper for Missing Entities

```java
public Optional<ProductEntity> getByCode(String code) {
    return repository.findByCode(code);
}

public ProductEntity requireByCode(String code) {
    return repository.findByCode(code)
        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + code));
}
```

## When to Use What

| Scenario | Pattern |
|----------|---------|
| 1-2 fixed filter properties | Derived query method |
| Fixed SQL, no conditions | `@Query` or ElSql |
| Dynamic/optional filters | ElSql `:where`/`:and` |
| Paginated results | ElSql `:offsetfetch` |
| Cross-aggregate JOINs | CQRS Query Service + ElSql |
| Bulk update/delete | Custom Repository + ElSql |
| IN with variable list | `NamedParameterJdbcTemplate` IN expansion |
