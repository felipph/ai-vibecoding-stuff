# Custom Repositories with AbstractElSqlRepository

Use custom repositories when the standard `ListCrudRepository` is insufficient:
- Bulk update/delete without loading entities
- Dynamic queries via ElSql with `:IF`, `:WHERE`, and other conditional tags
- Batch operations using `NamedParameterJdbcTemplate`

**Recommended approach:** Extend `AbstractElSqlRepository` to eliminate boilerplate and automatically load ElSql bundles.

---

## Quick Start with AbstractElSqlRepository

`AbstractElSqlRepository` is a base class that:
- Automatically loads ElSql bundles based on package structure
- Provides `NamedParameterJdbcTemplate` for query execution
- Offers utility methods `getSql()` and `getSqlDinamico()`
- Validates `.elsql` file existence with clear error messages

### File Location Convention

The `.elsql` file follows the same package structure as the repository class:

```
Repository: com.exemplo.repo.ProductRepositoryImpl
ElSql file: src/main/resources/com/exemplo/repo/ProductRepositoryImpl.elsql
```

---

## Structure

1. **Custom interface** — Defines the additional methods
2. **Implementation class** — Extends `AbstractElSqlRepository`, implements custom interface
3. **Main repository** — Extends both `ListCrudRepository` and custom interface

---

## Full Example

### 1. Custom Interface

```java
public interface ProductRepositoryCustom {
    List<ProductEntity> search(ProductSearchCriteria criteria);
    int bulkUpdateStatus(ProductStatus from, ProductStatus to);
    void bulkInsert(List<ProductEntity> products);
}
```

### 2. Implementation (with AbstractElSqlRepository)

```java
@Repository
class ProductRepositoryImpl extends AbstractElSqlRepository
                            implements ProductRepositoryCustom {

    public ProductRepositoryImpl(DataSource dataSource) {
        super(dataSource);  // Bundle and template created automatically
    }

    @Override
    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        var params = new MapSqlParameterSource()
            .addValue("name",     criteria.name())
            .addValue("status",   criteria.status() != null ? criteria.status().name() : null)
            .addValue("minPrice", criteria.minPrice())
            .addValue("maxPrice", criteria.maxPrice());

        var sql = getSqlDinamico("SearchProducts", params);
        return namedJdbc.query(sql.getSqlString(), sql.getParameters(), productRowMapper());
    }

    @Override
    @Transactional
    public int bulkUpdateStatus(ProductStatus from, ProductStatus to) {
        var params = new MapSqlParameterSource()
            .addValue("fromStatus", from.name())
            .addValue("toStatus",   to.name());

        var sql = getSqlDinamico("BulkUpdateStatus", params);
        return namedJdbc.update(sql.getSqlString(), sql.getParameters());
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

        namedJdbc.batchUpdate(
            getSql("BulkInsertProducts"),
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

---

## The `.elsql` File

Location: `src/main/resources/com/exemplo/repo/ProductRepositoryImpl.elsql`

```sql
-- :name SearchProducts
-- Dynamic search with optional filters
SELECT id, code, name, price, status, created_at
FROM products
:where
  :and(:name, name ILIKE :name)
  :and(:status, status = :status)
  :and(:minPrice, price >= :minPrice)
  :and(:maxPrice, price <= :maxPrice)
ORDER BY name

-- :name BulkUpdateStatus
-- Update status for all products matching criteria
UPDATE products
SET    status     = :toStatus,
       updated_at = NOW()
WHERE  status = :fromStatus

-- :name BulkInsertProducts
-- Simple bulk insert without ElSql dynamic tags
INSERT INTO products (code, name, price, status)
VALUES (:code, :name, :price, :status)

-- :name DeleteByStatus
-- Delete all products with given status
DELETE FROM products
WHERE status = :status
```

---

## Dynamic Queries with ElSql

`AbstractElSqlRepository.getSqlDinamico()` is perfect for queries with conditional filters:

```java
public List<ProductEntity> search(ProductSearchCriteria criteria) {
    var params = new MapSqlParameterSource();

    // Always add parameters — ElSql :IF checks the value, not presence
    params.addValue("name", criteria.name());
    params.addValue("status", criteria.status() != null ? criteria.status().name() : null);
    params.addValue("minPrice", criteria.minPrice());
    params.addValue("maxPrice", criteria.maxPrice());

    var sql = getSqlDinamico("SearchProducts", params);
    return namedJdbc.query(sql.getSqlString(), sql.getParameters(), productRowMapper());
}
```

**Corresponding `.elsql` file:**

```sql
-- :name SearchProducts
SELECT * FROM products
:where
  :and(:name, name ILIKE :name)
  :and(:status, status = :status)
  :and(:minPrice, price >= :minPrice)
  :and(:maxPrice, price <= :maxPrice)
ORDER BY name
```

**How it works:**
- `:where` — Adds `WHERE` if any `:and()` conditions match
- `:and(condition, sql)` — Includes `sql` only if `condition` is not null
- Even if all parameters are null, `:where` produces valid SQL (no WHERE clause)

---

## Batch Update with `batchUpdate`

```java
@Transactional
public void updatePrices(Map<Long, BigDecimal> idToPrice) {
    SqlParameterSource[] params = idToPrice.entrySet().stream()
        .map(e -> new MapSqlParameterSource()
            .addValue("id",    e.getKey())
            .addValue("price", e.getValue()))
        .toArray(SqlParameterSource[]::new);

    namedJdbc.batchUpdate(
        "UPDATE products SET price = :price WHERE id = :id",
        params
    );
}
```

**When to use `batchUpdate`:**
- Large bulk updates (100+ rows)
- Better performance than individual `update()` calls
- Single transaction, one round-trip to database

---

## Upsert (Insert or Update)

PostgreSQL ON CONFLICT clause:

**`.elsql` file:**

```sql
-- :name UpsertProduct
INSERT INTO products (code, name, price, status)
VALUES (:code, :name, :price, :status)
ON CONFLICT (code) DO UPDATE
SET name   = EXCLUDED.name,
    price  = EXCLUDED.price,
    status = EXCLUDED.status
```

**Repository method:**

```java
@Transactional
public void upsert(ProductEntity p) {
    var params = new MapSqlParameterSource()
        .addValue("code",   p.code())
        .addValue("name",   p.name())
        .addValue("price",  p.price())
        .addValue("status", p.status().name());

    var sql = getSqlDinamico("UpsertProduct", params);
    namedJdbc.update(sql.getSqlString(), sql.getParameters());
}
```

---

## Delete with Return Count

```java
@Transactional
public int deleteByStatus(ProductStatus status) {
    var params = new MapSqlParameterSource("status", status.name());
    var sql = getSqlDinamico("DeleteByStatus", params);
    return namedJdbc.update(sql.getSqlString(), sql.getParameters());
}
```

---

## When NOT to Use AbstractElSqlRepository

The base class is ideal for most cases, but consider the manual approach when:

1. **Different SQL dialect** — `AbstractElSqlRepository` uses `POSTGRES` dialect. For MySQL, Oracle, or others, use manual approach with `ElSqlConfig.MYSQL`, `ElSqlConfig.ORACLE`, etc.

2. **Multiple ElSql bundles per repository** — If you need to load queries from multiple `.elsql` files, manual approach gives more control.

3. **Custom dialect switching** — When you need runtime dialect selection based on environment.

4. **No `.elsql` file needed** — For very simple inline SQL without ElSql features, standard `@Query` or `JdbcTemplate` may be sufficient.

**Manual approach example:**

```java
@Repository
class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    ProductRepositoryImpl(DataSource dataSource) {
        // MySQL dialect instead of default POSTGRES
        this.bundle = ElSqlBundle.of(ElSqlConfig.MYSQL, getClass());
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    // ... methods using bundle.getSql() directly
}
```

---

## Best Practices

1. **Naming:** Implementation must be named `<Repository>Impl` (Spring's convention)
2. **Transactions:** Add `@Transactional` to write methods in `Impl`. Read methods inherit `readOnly=true` from the service layer
3. **ElSql for all SQL:** Even simple UPDATE/DELETE should live in the `.elsql` file for consistency
4. **Bulk ops bypass lifecycle:** Bulk UPDATE/DELETE does not trigger Spring Data JDBC events (`@BeforeSave`, etc.)
5. **Use `batchUpdate` for large inserts:** Never call `save()` in a loop
6. **Always add parameters:** For `getSqlDinamico()`, always add params even if null. ElSql `:IF` checks the value, not presence
7. **File location:** Follow the package convention for `.elsql` files to avoid `IllegalArgumentException`

---

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

**No test configuration needed:** `AbstractElSqlRepository` automatically loads the `.elsql` file from the classpath.

---

## Migration from Manual Approach

If you have existing custom repositories using the manual approach:

**Before (manual):**
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

    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        // ... manual bundle.getSql() calls
    }
}
```

**After (with AbstractElSqlRepository):**
```java
@Repository
class ProductRepositoryImpl extends AbstractElSqlRepository
                            implements ProductRepositoryCustom {

    public ProductRepositoryImpl(DataSource dataSource) {
        super(dataSource);  // Simpler!
    }

    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        // ... getSqlDinamico() calls
    }
}
```

**Benefits:**
- ✅ Less boilerplate code
- ✅ No `@Qualifier` needed
- ✅ Automatic `.elsql` file location
- ✅ Clear error message if file not found

---

## See Also

- **`assets/abstract-elsql-repository.java`** — Complete base class implementation
- **SKILL.md** — Main documentation with "Abstract Base Class" section
- **references/elsql-syntax.md** — Full ElSql tag reference (`:IF`, `:WHERE`, `:AND`, etc.)
- **references/query-patterns.md** — Fixed and dynamic query patterns
