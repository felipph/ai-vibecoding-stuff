# CQRS Query Service Reference

Separate read operations from write operations for DDD/Hexagonal architectures with Spring Data JDBC.
All SQL lives in an external `.elsql` file — never inline.

## Pattern

- **Repository** (package-private): Write operations, returns aggregate entities  
- **QueryService** (public): Read operations, returns View Models via ElSql + NamedParameterJdbcTemplate  
- Each query service owns its `.elsql` file with all its fragments

## Wiring

```java
@Configuration
public class ProductSqlConfig {

    @Bean
    public ElSqlBundle productElSqlBundle() {
        // Loads ProductQueries.elsql from the same package as ProductQueries (marker class)
        return ElSqlBundle.of(ElSqlConfig.POSTGRES, ProductQueries.class);
    }
}

// Marker class — same package as the elsql file
final class ProductQueries {}
```

## Query Service Implementation

```java
@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    public ProductQueryService(
            @Qualifier("productElSqlBundle") ElSqlBundle bundle,
            NamedParameterJdbcTemplate jdbc) {
        this.bundle = bundle;
        this.jdbc = jdbc;
    }

    public List<ProductVM> findAllActive() {
        SqlFragments sql = bundle.getSql("FindAllActive", new MapSqlParameterSource());
        return jdbc.query(sql.getSqlString(), sql.getParameters(), ProductVM.rowMapper());
    }

    public Optional<ProductDetailsVM> findDetailsById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        SqlFragments sql = bundle.getSql("FindDetailsById", params);
        List<ProductDetailsVM> results = jdbc.query(
            sql.getSqlString(), sql.getParameters(), ProductDetailsVM.rowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Page<ProductVM> findPage(ProductSearchCriteria criteria, int page, int size) {
        MapSqlParameterSource params = buildSearchParams(criteria)
            .addValue("offset", (long) page * size)
            .addValue("fetch",  size);

        Long total = jdbc.queryForObject(
            bundle.getSql("CountSearch", params).getSqlString(), params, Long.class);

        List<ProductVM> content = jdbc.query(
            bundle.getSql("Search", params).getSqlString(),
            params,
            ProductVM.rowMapper()
        );

        return new PageImpl<>(content, PageRequest.of(page, size), total != null ? total : 0);
    }

    private MapSqlParameterSource buildSearchParams(ProductSearchCriteria criteria) {
        return new MapSqlParameterSource()
            .addValue("name",       criteria.name())
            .addValue("status",     criteria.status() != null ? criteria.status().name() : null)
            .addValue("minPrice",   criteria.minPrice())
            .addValue("maxPrice",   criteria.maxPrice())
            .addValue("categoryId", criteria.categoryId());
    }
}
```

## View Models (Records with built-in RowMapper)

```java
public record ProductVM(
    Long id,
    String code,
    String name,
    BigDecimal price,
    String status
) {
    public static RowMapper<ProductVM> rowMapper() {
        return (rs, rowNum) -> new ProductVM(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBigDecimal("price"),
            rs.getString("status")
        );
    }
}

public record ProductDetailsVM(
    Long id,
    String code,
    String name,
    String description,
    BigDecimal price,
    String status,
    CategoryVM category
) {
    public record CategoryVM(Long id, String name) {}

    public static RowMapper<ProductDetailsVM> rowMapper() {
        return (rs, rowNum) -> new ProductDetailsVM(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getString("status"),
            new CategoryVM(
                rs.getLong("category_id"),
                rs.getString("category_name")
            )
        );
    }
}
```

## The `.elsql` File

```sql
-- :name FindAllActive
SELECT id, code, name, price, status
FROM products
WHERE status = 'ACTIVE'
ORDER BY created_at DESC

-- :name FindDetailsById
SELECT
    p.id, p.code, p.name, p.description, p.price, p.status,
    c.id   AS category_id,
    c.name AS category_name
FROM products p
LEFT JOIN categories c ON c.id = p.category_id
WHERE p.id = :id

-- :name Search
SELECT id, code, name, price, status
FROM products
:where
  :and(:name, name ILIKE :name)
  :and(:status, status = :status)
  :and(:minPrice, price >= :minPrice)
  :and(:maxPrice, price <= :maxPrice)
  :and(:categoryId, category_id = :categoryId)
ORDER BY name
:offsetfetch(:offset, :fetch)

-- :name CountSearch
SELECT COUNT(*)
FROM products
:where
  :and(:name, name ILIKE :name)
  :and(:status, status = :status)
  :and(:minPrice, price >= :minPrice)
  :and(:maxPrice, price <= :maxPrice)
  :and(:categoryId, category_id = :categoryId)
```

## Complex Queries with JOINs

```java
public List<OrderSummaryVM> findTopOrdersByUser(Long userId, int limit) {
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("userId", userId)
        .addValue("fetch",  limit)
        .addValue("offset", 0L);

    SqlFragments sql = bundle.getSql("TopOrdersByUser", params);
    return jdbc.query(sql.getSqlString(), sql.getParameters(), OrderSummaryVM.rowMapper());
}
```

```sql
-- :name TopOrdersByUser
SELECT
    o.id,
    o.order_number,
    o.total,
    o.status,
    o.created_at,
    COUNT(oi.id)     AS item_count,
    SUM(oi.quantity) AS total_units
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
WHERE o.user_id = :userId
GROUP BY o.id, o.order_number, o.total, o.status, o.created_at
ORDER BY o.total DESC
:offsetfetch(:offset, :fetch)
```

## Write Side (Repository — package-private)

```java
// Package-private: only visible inside the domain package
interface ProductRepository extends ListCrudRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByCode(String code);
}
```

The repository is used only by the command handlers / domain services (write side).
The `ProductQueryService` bypasses it entirely and queries the DB directly via SQL.

## Multiple Bundles

When a service spans multiple aggregates, inject multiple bundles:

```java
@Service
@Transactional(readOnly = true)
public class DashboardQueryService {

    private final ElSqlBundle orderBundle;
    private final ElSqlBundle productBundle;
    private final NamedParameterJdbcTemplate jdbc;

    public DashboardQueryService(
            @Qualifier("orderElSqlBundle") ElSqlBundle orderBundle,
            @Qualifier("productElSqlBundle") ElSqlBundle productBundle,
            NamedParameterJdbcTemplate jdbc) {
        this.orderBundle = orderBundle;
        this.productBundle = productBundle;
        this.jdbc = jdbc;
    }
}
```

## Testing the Query Service

```java
@JdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductQueryServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    NamedParameterJdbcTemplate jdbc;

    @Sql("/sql/product-test-data.sql")  // seed data
    @Test
    void shouldFindActiveProducts() {
        ElSqlBundle bundle = ElSqlBundle.of(ElSqlConfig.POSTGRES, ProductQueries.class);
        ProductQueryService service = new ProductQueryService(bundle, jdbc);

        List<ProductVM> result = service.findAllActive();

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(p -> "ACTIVE".equals(p.status()));
    }
}
```
