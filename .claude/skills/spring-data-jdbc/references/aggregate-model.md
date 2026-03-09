# Aggregate Model Reference

Spring Data JDBC is built around the **DDD Aggregate** concept. Each aggregate has exactly one root,
one repository, and owns its child entities completely. No lazy loading; no session; no proxy magic.

## Core Annotations

| Annotation | Purpose |
|-----------|---------|
| `@Table("table_name")` | Maps class to a table (inferred from class name if omitted) |
| `@Id` | Marks the aggregate's primary key |
| `@Column("col_name")` | Maps field to column (inferred from field name if omitted) |
| `@MappedCollection(idColumn)` | One-to-many: child rows owned by this aggregate |
| `@Embedded` | Inline value object columns into the same table row |
| `AggregateReference<T, ID>` | FK reference to another aggregate's root (ID only, no join) |
| `@Transient` | Field is not persisted to the database |

## Aggregate Root

```java
@Table("orders")
public class Order {

    @Id
    private Long id;

    @Column("order_number")
    private String orderNumber;

    @Column("user_id")
    private Long userId;          // plain FK — no object navigation

    private OrderStatus status;   // enum mapped to VARCHAR by default

    @Column("created_at")
    private Instant createdAt;

    // Owned child collection — Spring Data JDBC manages INSERT/DELETE
    @MappedCollection(idColumn = "order_id", keyColumn = "item_position")
    private List<OrderItem> items = new ArrayList<>();
}
```

## Owned Child Entity

Child entities inside `@MappedCollection` **do not have their own repository**.
They are saved and deleted automatically when the aggregate root is saved.

```java
// No @Table needed — inferred as "order_item"
public class OrderItem {

    @Id
    private Long id;

    // Cross-aggregate FK: ProductId only, never the Product object
    private AggregateReference<Product, Long> product;

    private int quantity;
    private BigDecimal unitPrice;
}
```

**Warning:** Every `repository.save(order)` performs DELETE + INSERT on all child rows.
Keep owned collections small (< 100 items) or use custom SQL for targeted updates.

## Cross-Aggregate Reference

Use `AggregateReference<T, ID>` to hold a FK to another aggregate root without loading it:

```java
@Table("order_items")
public class OrderItem {

    // Reference to Product aggregate (stores only the Long FK)
    private AggregateReference<Product, Long> product;

    public Long getProductId() {
        return product.getId();
    }
}
```

To resolve the referenced aggregate, load it via its own repository:

```java
Product product = productRepository.findById(item.getProductId())
    .orElseThrow(...);
```

## Embedded Value Objects

Use `@Embedded` to map a value object into the same table row:

```java
@Table("customers")
public class Customer {

    @Id
    private Long id;
    private String name;

    @Embedded.Nullable          // all columns may be null
    private Address address;

    @Embedded.Empty             // defaults to empty object if all null
    private ContactInfo contact;
}

// Value object — no @Table, no @Id
public record Address(
    @Column("street") String street,
    @Column("city")   String city,
    @Column("zip")    String zip
) {}
```

## Keyed vs. Unkeyed Collections

```java
// List with an explicit ordering column
@MappedCollection(idColumn = "order_id", keyColumn = "item_position")
private List<OrderItem> items;

// Set — no ordering column needed
@MappedCollection(idColumn = "tag_id")
private Set<ProductTag> tags;

// Map — keyColumn stores the map key
@MappedCollection(idColumn = "order_id", keyColumn = "attribute_key")
private Map<String, OrderAttribute> attributes;
```

## Strongly-Typed IDs

Prefer wrapping primitive IDs in a value object for type safety:

```java
public record ProductId(Long value) implements Serializable {}

@Table("products")
public class Product {
    @Id
    private ProductId id;
    // ...
}

public interface ProductRepository extends ListCrudRepository<Product, ProductId> {}
```

Register a `@ReadingConverter` and `@WritingConverter` if needed (Spring Data JDBC may
auto-detect for records with a single primitive field).

## What Spring Data JDBC Does on save()

| Operation | Behaviour |
|-----------|-----------|
| `save(root)` — new (id == null) | INSERT root row; INSERT all child rows |
| `save(root)` — existing | UPDATE root row; DELETE all child rows; INSERT all child rows |
| `delete(root)` | DELETE all child rows; DELETE root row |

This means the aggregate is always treated as a **unit**. Do not partially update child collections
via raw SQL and then call `save()` — you will lose changes. Use a custom repository for partial updates.

## Naming Conventions

Spring Data JDBC derives table and column names from class/field names using `NamingStrategy`.
Default: `camelCase` → `snake_case`.

```
class OrderItem      → table  order_item
field orderNumber    → column order_number
field userId         → column user_id
```

To override globally:

```java
@Bean
NamingStrategy namingStrategy() {
    return NamingStrategy.DEFAULT; // or a custom implementation
}
```

## Anti-Patterns

| Don't | Do |
|-------|-----|
| Repository for child entities | Only for aggregate roots |
| `AggregateReference` to a child entity | Child entities are owned, no refs needed |
| Large `@MappedCollection` (1000+ rows) | Use custom SQL or move to separate aggregate |
| Navigate across aggregates via objects | Use `AggregateReference` + repository lookup |
| Partial collection update then `save()` | Custom repository + targeted SQL |
