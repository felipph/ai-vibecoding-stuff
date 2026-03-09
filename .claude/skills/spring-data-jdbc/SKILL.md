---
name: spring-data-jdbc
description: Implement Spring Data JDBC repositories, aggregates, and queries following modern best practices with SQL externalized via ElSql. Use for creating repositories (only for aggregate roots), writing queries (derived methods, ElSql bundles), custom repositories (NamedParameterJdbcTemplate + ElSql), CQRS query services, aggregate relationships (AggregateReference, @MappedCollection), and performance optimization. Covers patterns from simple repositories to advanced CQRS with externalized dynamic SQL.
---

# Spring Data JDBC Implementation with ElSql

## Critical Rules

**NEVER create repositories for every table. ALWAYS create repositories only for aggregate roots.**

**NEVER inline SQL strings in Java code. ALWAYS externalize queries to `.elsql` files.**

**NEVER use JPA annotations (@Entity, @ManyToOne, lazy loading). ALWAYS use Spring Data JDBC annotations (@Table, @MappedCollection, AggregateReference).**

**NEVER rely on lazy loading — it does not exist in Spring Data JDBC. ALWAYS design aggregates to be loaded eagerly or use a CQRS query service.**

**NEVER navigate across aggregate boundaries via object references. ALWAYS use AggregateReference<T, ID> for cross-aggregate links.**

## Step 1: Identify Repository Needs

Ask:
1. **Is this an aggregate root?** — Only aggregate roots get `ListCrudRepository`
2. **Query complexity?** — Simple lookup or dynamic multi-filter search?
3. **Read vs Write?** — Command (write) or query (read)?
4. **Cross-aggregate read?** — Use a CQRS Query Service, not a repository
5. **Performance critical?** — Large datasets or complex JOINs → CQRS + ElSql

## Step 2: Choose Pattern

| Pattern | When | Read |
|---------|------|------|
| **Simple Repository** | Basic CRUD, 1-2 fixed queries | - |
| **@Query Repository** | Few fixed SQL queries, no dynamics | `references/query-patterns.md` |
| **ElSql Repository** | Dynamic filtering, reusable SQL blocks | `references/query-patterns.md` |
| **Custom Repository** | Bulk ops, complex dynamic SQL | `references/custom-repositories.md` |
| **CQRS Query Service** | Cross-aggregate reads, reporting, projections | `references/cqrs-query-service.md` |

**Decision criteria:**

| Need | Simple | @Query | ElSql | Custom | CQRS |
|------|--------|--------|-------|--------|------|
| Basic CRUD | ✅ | ✅ | ❌ | ✅ | ✅ |
| Fixed custom SQL | ❌ | ✅ | ✅ | ✅ | ✅ |
| Dynamic filters | ❌ | ❌ | ✅✅ | ✅ | ✅ |
| Cross-aggregate JOIN | ❌ | ❌ | ✅ | ✅ | ✅✅ |
| Best read performance | ❌ | ✅ | ✅ | ✅ | ✅✅ |
| Bulk operations | ❌ | ❌ | ❌ | ✅ | ❌ |
| Read/Write Separation | ❌ | ❌ | ❌ | ❌ | ✅✅ |

## Step 3: Implement Repository

### Simple Repository

For basic CRUD and trivial lookups (1-2 fixed properties):

```java
public interface ProductRepository extends ListCrudRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByCode(String code);
    List<ProductEntity> findByStatus(ProductStatus status);
}
```

**No SQL needed.** Spring Data JDBC derives the query from the method name.

### @Query Repository

For a small number of fixed SQL queries. **Read:** `references/query-patterns.md`

```java
public interface OrderRepository extends ListCrudRepository<OrderEntity, Long> {

    @Query("""
        SELECT o.id, o.order_number, o.user_id, o.status, o.created_at
        FROM orders o
        WHERE o.user_id = :userId
        ORDER BY o.created_at DESC
        """)
    List<OrderEntity> findByUserId(@Param("userId") Long userId);
}
```

### ElSql Repository (Recommended for non-trivial queries)

For dynamic filtering, reusable SQL blocks, and externalized queries.
**Read:** `references/query-patterns.md` and `references/elsql-syntax.md`

```java
// SQL file: src/main/resources/sql/ProductQueries.elsql
// -- :name FindActive
// SELECT id, code, name, price, status FROM products
// WHERE status = 'ACTIVE'
// ORDER BY created_at DESC

@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    public List<ProductVM> findActive() {
        SqlFragments sql = bundle.getSql("FindActive", new MapSqlParameterSource());
        return jdbc.query(sql.getSqlString(), sql.getParameters(), productVMMapper());
    }
}
```

**Asset:** `assets/elsql-queries.elsql` — Complete `.elsql` template with all tag examples

**Asset:** `assets/elsql-config.java` — `ElSqlBundle` bean configuration

### Custom Repository

For bulk operations and complex dynamic SQL. **Read:** `references/custom-repositories.md`

```java
// 1. Custom interface
public interface ProductRepositoryCustom {
    int bulkDeactivate(List<Long> ids);
    List<ProductEntity> search(ProductSearchCriteria criteria);
}

// 2. Implementation (must be named <Repository>Impl)
@Repository
class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("name", criteria.name())
            .addValue("minPrice", criteria.minPrice())
            .addValue("status", criteria.status());
        SqlFragments sql = bundle.getSql("SearchProducts", params);
        return jdbc.query(sql.getSqlString(), sql.getParameters(), productRowMapper());
    }
}

// 3. Main repository extends both
public interface ProductRepository extends ListCrudRepository<ProductEntity, Long>,
                                           ProductRepositoryCustom {
    Optional<ProductEntity> findByCode(String code);
}
```

**Asset:** `assets/custom-repository.java` — Complete pattern

### Abstract Base Class (Recommended for Custom Repositories)

For custom repositories with ElSql, use `AbstractElSqlRepository` to eliminate boilerplate.
This base class automatically loads the ElSql bundle and provides utility methods.

**When to use:**
- Custom repository implementations with ElSql
- Dynamic queries with `:IF`, `:WHERE`, and other ElSql tags
- Need for both SQL execution and template access

**Before (manual approach):**
```java
@Repository
class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    public ProductRepositoryImpl(DataSource ds) {
        this.bundle = ElSqlBundle.of(ElSqlConfig.POSTGRES, ProductQueries.class);
        this.jdbc = new NamedParameterJdbcTemplate(ds);
    }

    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("name", criteria.name())
            .addValue("minPrice", criteria.minPrice());
        SqlFragments sql = bundle.getSql("SearchProducts", params);
        return jdbc.query(sql.getSqlString(), sql.getParameters(), productRowMapper());
    }
}
```

**After (with AbstractElSqlRepository):**
```java
@Repository
class ProductRepositoryImpl extends AbstractElSqlRepository
                            implements ProductRepositoryCustom {

    public ProductRepositoryImpl(DataSource ds) {
        super(ds);  // Bundle and template created automatically
    }

    public List<ProductEntity> search(ProductSearchCriteria criteria) {
        var params = new MapSqlParameterSource()
            .addValue("name", criteria.name())
            .addValue("minPrice", criteria.minPrice());
        var sql = getSqlDinamico("SearchProducts", params);
        return namedJdbc.query(sql.getSqlString(), sql.getParameters(), productRowMapper());
    }
}
```

**File location convention:**
- Repository class: `com.exemplo.repo.ProdutoRepositoryImpl`
- ElSql file: `src/main/resources/com/exemplo/repo/ProdutoRepositoryImpl.elsql`

**Important notes:**
- Uses `ElSqlConfig.POSTGRES` dialect (LIMIT/OFFSET pagination)
- Only `NamedParameterJdbcTemplate` is exposed (not `JdbcTemplate`)
- Throws `IllegalArgumentException` if .elsql file is not found on classpath

**Asset:** `assets/abstract-elsql-repository.java` — Complete base class with JavaDoc

### CQRS Query Service

For cross-aggregate reads, reporting, and projections. **Read:** `references/cqrs-query-service.md`

```java
// Repository (package-private) — writes only
interface ProductRepository extends ListCrudRepository<ProductEntity, ProductId> {
    Optional<ProductEntity> findByCode(ProductCode code);
}

// QueryService (public) — reads only, uses ElSql
@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    public List<ProductVM> findAllActive() {
        SqlFragments sql = bundle.getSql("FindAllActive", new MapSqlParameterSource());
        return jdbc.query(sql.getSqlString(), sql.getParameters(),
            (rs, row) -> new ProductVM(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getBigDecimal("price")
            ));
    }
}
```

**Asset:** `assets/query-service.java` — Full CQRS pattern with ElSql + NamedParameterJdbcTemplate

## Step 4: Aggregate Design

**Read:** `references/aggregate-model.md` for detailed guidance

**Quick patterns:**

```java
// ✅ GOOD: Aggregate root with embedded collection
@Table("orders")
public class Order {
    @Id
    private Long id;

    @MappedCollection(idColumn = "order_id")
    private List<OrderItem> items;           // owned by this aggregate
}

// ✅ GOOD: Cross-aggregate reference (loose coupling)
@Table("order_items")
public class OrderItem {
    @Column("product_id")
    private AggregateReference<Product, Long> product; // ID reference only
    private int quantity;
}

// ❌ AVOID: Never load cross-aggregate associations as objects
// OrderItem should NOT have: @ManyToOne private Product product;

// ✅ GOOD: Value Object embedded inline
@Table("customers")
public class Customer {
    @Id private Long id;
    @Embedded.Nullable
    private Address address;
}
```

**Asset:** `assets/aggregate-entity.java` — All aggregate patterns with examples

## Step 5: Configure ElSql

**Read:** `references/elsql-syntax.md` for full tag reference

**Bean configuration:**

```java
@Configuration
public class SqlConfig {

    @Bean
    public ElSqlBundle productElSqlBundle() {
        return ElSqlBundle.of(ElSqlConfig.POSTGRES, ProductQueries.class);
        // Loads from: com/example/products/ProductQueries.elsql
    }
}
```

**`.elsql` file location:** place alongside the class that uses it, or in `src/main/resources/sql/`.

**Asset:** `assets/elsql-config.java` — Configuration for multiple bundles and dialect switching

## Step 6: Testing

```java
@DataJdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProductRepository repository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldFindProductByCode() {
        var product = new ProductEntity(null, "P001", "Widget", ProductStatus.ACTIVE);
        repository.save(product);

        Optional<ProductEntity> found = repository.findByCode("P001");

        assertThat(found).isPresent();
        assertThat(found.get().code()).isEqualTo("P001");
    }
}
```

**For CQRS Query Services in tests:**
- Use `@JdbcTest` (no Spring Data JDBC context overhead)
- Provide `ElSqlBundle` as a `@TestBean` or load from classpath
- Use `@Sql` to seed test data

## Anti-Patterns

| Don't | Do | Why |
|-------|-----|-----|
| Repository for every table | Only for aggregate roots | Maintains aggregate boundaries |
| Inline SQL strings in Java | Externalize to `.elsql` files | Maintainability, reuse, tooling |
| JPA annotations (@Entity, @ManyToOne) | Spring Data JDBC (@Table, AggregateReference) | Wrong module entirely |
| Expect lazy loading | Design aggregates correctly or use CQRS | JDBC has no session/proxy |
| Cross-aggregate object navigation | Use AggregateReference<T, ID> | Enforces boundaries |
| findAll() without pagination | Use LIMIT/OFFSET in ElSql | Memory issues |
| Fetch entities for read views | Use CQRS Query Service with View Models | N+1, unnecessary joins |
| @Transactional in repository | Put @Transactional in service layer | Proper boundaries |
| Custom @Query for dynamic SQL | Use ElSql :IF/:WHERE blocks | @Query cannot be conditional |
| Return entities from controllers | Return DTOs/VMs | Decouples API from storage |

## Common Pitfalls

### 1. Missing @MappedCollection idColumn
**Problem:** Child entity isn't linked to its parent

**Solution:** Always specify the FK column:
```java
@MappedCollection(idColumn = "order_id", keyColumn = "position")
private List<OrderItem> items;
```

### 2. Saving the whole aggregate on every write
**Problem:** `repository.save(order)` re-inserts all `OrderItem` rows (delete + insert)

**Solution:** Keep aggregates small. For large collections use a custom repository with targeted SQL updates.

### 3. ElSql param not found
**Problem:** `SqlFragments` throws on missing `:name` that is declared conditional

**Solution:** Always add the param to `MapSqlParameterSource`, even if `null`. ElSql `:IF` checks the value, not its presence.

### 4. Wrong ElSql dialect
**Problem:** OFFSET/FETCH pagination works on Postgres but not H2 in tests

**Solution:** Use `ElSqlConfig.HSQL` or `ElSqlConfig.H2` in test `@TestConfiguration`. See `assets/elsql-config.java`.

## Quick Reference

### When to Load References

- **Aggregate annotations, value objects, cross-aggregate links** → `references/aggregate-model.md`
- **ElSql tag syntax, :IF, :WHERE, pagination** → `references/elsql-syntax.md`
- **Fixed and dynamic query patterns** → `references/query-patterns.md`
- **CQRS read service with ElSql** → `references/cqrs-query-service.md`
- **Custom repository, bulk operations** → `references/custom-repositories.md`
- **N+1 avoidance, batch inserts, pagination** → `references/performance-guide.md`

### Available Assets

All templates in `assets/`:
- `abstract-elsql-repository.java` — Base class for custom repositories with ElSql
- `aggregate-entity.java` — Aggregate root, value objects, cross-aggregate refs
- `elsql-queries.elsql` — Complete `.elsql` template with all tag examples
- `elsql-config.java` — `ElSqlBundle` bean configuration with dialect switching
- `query-service.java` — CQRS query service with ElSql + NamedParameterJdbcTemplate
- `custom-repository.java` — Custom repository impl with ElSql and bulk ops

## Dependencies

```xml
<!-- Spring Data JDBC -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>

<!-- ElSql -->
<dependency>
    <groupId>com.opengamma.strata</groupId>
    <artifactId>strata-collect</artifactId>
    <version>2.12.3</version>
</dependency>
<!-- OR standalone elsql -->
<dependency>
    <groupId>com.opengamma</groupId>
    <artifactId>elsql</artifactId>
    <version>1.3</version>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

## References

Incorporates best practices from:
- [Spring Data JDBC Reference Documentation](https://docs.spring.io/spring-data/relational/reference/jdbc.html)
- [ElSql Documentation](https://github.com/OpenGamma/ElSql)
- [Oliver Drotbohm — Spring Data JDBC aggregates](https://spring.io/blog/2018/09/24/spring-data-jdbc-references-and-aggregates)
- [Thorben Janssen — Spring Data JDBC Tutorial](https://thorben-janssen.com/spring-data-jdbc-getting-started/)
