# ElSql Syntax Reference

ElSql externalizes SQL into `.elsql` files with a minimal tag language for conditional blocks,
dynamic WHERE clauses, and pagination. It is designed to remain valid SQL with SQL-style comments.

## File Structure

Each `.elsql` file contains one or more **named SQL fragments**:

```sql
-- :name FragmentName
SELECT id, name, price
FROM products
WHERE status = 'ACTIVE'

-- :name AnotherFragment
SELECT ...
```

- Fragment names are case-sensitive
- A fragment ends where the next `-- :name` begins (or at end of file)
- Comments not starting with `-- :` are preserved as-is

## Loading a Bundle

```java
// Load from classpath relative to the given class
ElSqlBundle bundle = ElSqlBundle.of(ElSqlConfig.POSTGRES, MyQueries.class);
// Loads: com/example/mypackage/MyQueries.elsql

// Or by resource path
ElSqlBundle bundle = ElSqlBundle.of(ElSqlConfig.POSTGRES,
    MyQueries.class.getResource("/sql/MyQueries.elsql"));
```

## Executing a Fragment

```java
MapSqlParameterSource params = new MapSqlParameterSource()
    .addValue("status", "ACTIVE");

SqlFragments sql = bundle.getSql("FragmentName", params);

List<MyVM> results = jdbc.query(
    sql.getSqlString(),
    sql.getParameters(),
    rowMapper
);
```

## Dialects

| Constant | Database |
|----------|----------|
| `ElSqlConfig.POSTGRES` | PostgreSQL |
| `ElSqlConfig.MYSQL` | MySQL / MariaDB |
| `ElSqlConfig.ORACLE` | Oracle |
| `ElSqlConfig.MSSQL` | SQL Server |
| `ElSqlConfig.HSQL` | HSQLDB (useful for tests) |
| `ElSqlConfig.H2` | H2 (useful for tests) |

The dialect controls pagination syntax (LIMIT/OFFSET vs FETCH NEXT vs ROWNUM).

---

## Tag Reference

### `:name` — Fragment declaration

```sql
-- :name MyFragment
SELECT * FROM products
```

### `:and` — Appends `AND condition` only when the parameter is present and non-null

```sql
-- :name SearchProducts
SELECT id, name, price FROM products
WHERE 1=1
  :and(:name, name ILIKE :name)
  :and(:minPrice, price >= :minPrice)
  :and(:maxPrice, price <= :maxPrice)
  :and(:status, status = :status)
ORDER BY name
```

- `:and(paramName, SQL condition)` — adds `AND <condition>` if `paramName` is non-null
- If all `:and` blocks are inactive, `WHERE 1=1` keeps the SQL valid

### `:where` — Generates the `WHERE` keyword automatically

```sql
-- :name SearchProducts
SELECT id, name, price FROM products
:where
  :and(:name, name ILIKE :name)
  :and(:status, status = :status)
ORDER BY name
```

- `:where` + `:and(...)` is cleaner than `WHERE 1=1` when there might be no conditions
- Outputs nothing if no `:and` blocks match

### `:if` / `:end` — Conditional block

```sql
-- :name FindOrders
SELECT o.id, o.order_number, o.created_at
FROM orders o
:if(:includeUser)
  JOIN users u ON u.id = o.user_id
:end
:where
  :and(:userId, o.user_id = :userId)
  :and(:status, o.status = :status)
```

### `:if` / `:else` / `:end`

```sql
-- :name FindByPriority
SELECT id, name FROM tasks
ORDER BY
  :if(:sortByDeadline)
    deadline ASC
  :else
    priority DESC
  :end
```

### `:like` — Case-insensitive LIKE with automatic `%` wrapping

```sql
-- :name SearchByName
SELECT id, name FROM products
:where
  :and(:name, name LIKE :like(:name))
```

- Wraps the value with `%` on both sides automatically

### `:offsetfetch` — Dialect-aware pagination

```sql
-- :name FindPaged
SELECT id, name, price FROM products
WHERE status = 'ACTIVE'
ORDER BY created_at DESC
:offsetfetch(:offset, :fetch)
```

- On Postgres → `LIMIT :fetch OFFSET :offset`
- On Oracle → uses `ROWNUM` subquery
- On SQL Server → uses `OFFSET :offset ROWS FETCH NEXT :fetch ROWS ONLY`

Pass parameters:

```java
params.addValue("offset", page * size);
params.addValue("fetch", size);
```

### `:paging` — Alias for `:offsetfetch` in some versions

```sql
:paging(:offset, :fetch)
```

### `:include` — Include another fragment by name

```sql
-- :name ProductColumns
p.id, p.code, p.name, p.price, p.status

-- :name FindActive
SELECT :include(ProductColumns)
FROM products p
WHERE status = 'ACTIVE'
```

---

## Parameter Binding Rules

ElSql uses **named parameters** via `MapSqlParameterSource`:

```java
MapSqlParameterSource params = new MapSqlParameterSource();

// Simple value
params.addValue("status", "ACTIVE");

// Null value — :and block will be SKIPPED
params.addValue("name", null);

// Collection (IN clause)
params.addValue("ids", List.of(1L, 2L, 3L));
```

**Important:** Always add every parameter to the `MapSqlParameterSource`, even if null.
ElSql reads the keys to decide which `:and` blocks to include. A missing key is treated differently
from a null value in some versions — use `.addValue("key", null)` explicitly.

---

## Complete Example

```sql
-- :name SearchOrders
SELECT
    o.id,
    o.order_number,
    o.status,
    o.total,
    o.created_at,
    u.name as user_name
FROM orders o
JOIN users u ON u.id = o.user_id
:where
  :and(:userId, o.user_id = :userId)
  :and(:status, o.status = :status)
  :and(:fromDate, o.created_at >= :fromDate)
  :and(:toDate, o.created_at <= :toDate)
  :and(:minTotal, o.total >= :minTotal)
ORDER BY o.created_at DESC
:offsetfetch(:offset, :fetch)

-- :name CountOrders
SELECT COUNT(*)
FROM orders o
:where
  :and(:userId, o.user_id = :userId)
  :and(:status, o.status = :status)
  :and(:fromDate, o.created_at >= :fromDate)
  :and(:toDate, o.created_at <= :toDate)
  :and(:minTotal, o.total >= :minTotal)
```

Java side:

```java
public Page<OrderVM> searchOrders(OrderSearchCriteria criteria, int page, int size) {
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("userId",   criteria.userId())
        .addValue("status",   criteria.status() != null ? criteria.status().name() : null)
        .addValue("fromDate", criteria.fromDate())
        .addValue("toDate",   criteria.toDate())
        .addValue("minTotal", criteria.minTotal())
        .addValue("offset",   page * size)
        .addValue("fetch",    size);

    Long total = jdbc.queryForObject(
        bundle.getSql("CountOrders", params).getSqlString(), params, Long.class);

    List<OrderVM> content = jdbc.query(
        bundle.getSql("SearchOrders", params).getSqlString(),
        params,
        orderVMMapper()
    );

    return new PageImpl<>(content, PageRequest.of(page, size), total);
}
```

---

## Best Practices

1. **One `.elsql` file per aggregate or service** — keeps fragments cohesive and discoverable
2. **Name fragments clearly** — `FindActiveProducts`, `CountByStatus`, `SearchOrders`
3. **Always provide COUNT fragment** alongside pageable SELECT fragments
4. **Use `:include` for repeated column lists** — avoids drift between SELECT and COUNT
5. **Add all parameters explicitly** — even if null, to avoid runtime surprises
6. **Use `ILIKE` on Postgres for case-insensitive search** — or `LOWER(col) LIKE LOWER(:param)`
7. **Test with the real dialect** — H2/HSQL may behave differently than Postgres
