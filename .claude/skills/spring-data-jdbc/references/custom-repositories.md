# Custom Repositories Reference

Use custom repositories when the standard `ListCrudRepository` is insufficient:
- Bulk update/delete without loading entities
- Dynamic queries via ElSql
- Batch inserts using `NamedParameterJdbcTemplate`

## Structure

1. **Custom interface** — Define the additional methods
2. **Implementation class** — Named `<Repository>Impl`, wires ElSql + JDBC
3. **Main repository** — Extends both `ListCrudRepository` and custom interface

## Full Example

### 1. Custom Interface

```java
public interface ProductRepositoryCustom {
    List<ProductEntity> search(ProductSearchCriteria criteria);
    int bulkUpdateStatus(ProductStatus from, ProductStatus to);
    void bulkInsert(List<ProductEntity> products);
}
```

### 2. Implementation

```java
@Repository
class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    ProductRepositoryImpl(
            @Qualifier("productElSqlBundle") ElSqlBundle bundle,
            NamedParameterJdbcTemplate jdbc) {
        this.bundle = bundle;
        this.jdbc = jdbc;
    }

    @Override
    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("name",     criteria.name())
            .addValue("status",   criteria.status() != null ? criteria.status().name() : null)
            .addValue("minPrice", criteria.minPrice())
            .addValue("maxPrice", criteria.maxPrice());

        SqlFragments sql = bundle.getSql("SearchProducts", params);
        return jdbc.query(sql.getSqlString(), sql.getParameters(), productRowMapper());
    }

    @Override
    @Transactional
    public int bulkUpdateStatus(ProductStatus from, ProductStatus to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("fromStatus", from.name())
            .addValue("toStatus",   to.name());

        SqlFragments sql = bundle.getSql("BulkUpdateStatus", params);
        return jdbc.update(sql.getSqlString(), sql.getParameters());
    }

    @Override
    @Transactional
    public void bulkInsert(List<ProductEntity> products) {
        SqlParameterSource[] batchParams = products.stream()
            .map(p -> new MapSqlParameterSource()
                .addValue("code",   p.code())
                .addValue("name",   p.name())
                .addValue("price",  p.price())
                .addValue("status", p.status().name()))
            .toArray(SqlParameterSource[]::new);

        jdbc.batchUpdate(
            "INSERT INTO products (code, name, price, status) VALUES (:code, :name, :price, :status)",
            batchParams
        );
    }

    private RowMapper<ProductEntity> productRowMapper() {
        return (rs, rowNum) -> new ProductEntity(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBigDecimal("price"),
            ProductStatus.valueOf(rs.getString("status"))
        );
    }
}
```

### 3. Main Repository

```java
public interface ProductRepository
        extends ListCrudRepository<ProductEntity, Long>, ProductRepositoryCustom {

    Optional<ProductEntity> findByCode(String code);
    List<ProductEntity> findByStatus(ProductStatus status);
}
```

## The `.elsql` File

```sql
-- :name SearchProducts
SELECT id, code, name, price, status, created_at
FROM products
:where
  :and(:name, name ILIKE :name)
  :and(:status, status = :status)
  :and(:minPrice, price >= :minPrice)
  :and(:maxPrice, price <= :maxPrice)
ORDER BY name

-- :name BulkUpdateStatus
UPDATE products
SET    status     = :toStatus,
       updated_at = NOW()
WHERE  status = :fromStatus

-- :name DeleteByStatus
DELETE FROM products
WHERE status = :status
```

## Batch Update with batchUpdate

```java
@Transactional
public void updatePrices(Map<Long, BigDecimal> idToPrice) {
    SqlParameterSource[] params = idToPrice.entrySet().stream()
        .map(e -> new MapSqlParameterSource()
            .addValue("id",    e.getKey())
            .addValue("price", e.getValue()))
        .toArray(SqlParameterSource[]::new);

    jdbc.batchUpdate(
        "UPDATE products SET price = :price WHERE id = :id",
        params
    );
}
```

## Upsert (Insert or Update)

PostgreSQL ON CONFLICT:

```sql
-- :name UpsertProduct
INSERT INTO products (code, name, price, status)
VALUES (:code, :name, :price, :status)
ON CONFLICT (code) DO UPDATE
SET name   = EXCLUDED.name,
    price  = EXCLUDED.price,
    status = EXCLUDED.status
```

```java
@Transactional
public void upsert(ProductEntity p) {
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("code",   p.code())
        .addValue("name",   p.name())
        .addValue("price",  p.price())
        .addValue("status", p.status().name());

    SqlFragments sql = bundle.getSql("UpsertProduct", params);
    jdbc.update(sql.getSqlString(), sql.getParameters());
}
```

## Delete with Return Count

```java
@Transactional
public int deleteByStatus(ProductStatus status) {
    MapSqlParameterSource params = new MapSqlParameterSource("status", status.name());
    SqlFragments sql = bundle.getSql("DeleteByStatus", params);
    return jdbc.update(sql.getSqlString(), sql.getParameters());
}
```

## Best Practices

1. **Naming:** Implementation must be named `<Repository>Impl` (Spring's convention)
2. **Transactions:** Add `@Transactional` to write methods in `Impl`. Read methods inherit `readOnly=true` from the service layer
3. **ElSql for all SQL:** Even simple UPDATE/DELETE should live in the `.elsql` file
4. **Bulk ops bypass lifecycle:** Bulk UPDATE/DELETE does not trigger Spring Data JDBC events (`@BeforeSave`, etc.)
5. **Use `batchUpdate` for large inserts:** Never call `save()` in a loop
6. **Test with `@DataJdbcTest`:** Requires Spring Data JDBC context to test the integrated repository

## Testing Custom Repository

```java
@DataJdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryImplTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired ProductRepository repository;

    @Test
    void shouldBulkUpdateStatus() {
        repository.save(new ProductEntity(null, "P001", "Widget", new BigDecimal("9.99"), ProductStatus.ACTIVE));
        repository.save(new ProductEntity(null, "P002", "Gadget", new BigDecimal("19.99"), ProductStatus.ACTIVE));

        int updated = repository.bulkUpdateStatus(ProductStatus.ACTIVE, ProductStatus.DISCONTINUED);

        assertThat(updated).isEqualTo(2);
    }
}
```

## Context Configuration for Tests

The `ElSqlBundle` bean must be available in the test context.
Provide it via a `@TestConfiguration`:

```java
@TestConfiguration
static class TestSqlConfig {
    @Bean
    @Qualifier("productElSqlBundle")
    ElSqlBundle productElSqlBundle() {
        return ElSqlBundle.of(ElSqlConfig.H2, ProductQueries.class);
    }
}
```
