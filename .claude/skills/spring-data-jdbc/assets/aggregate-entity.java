package {{PACKAGE}}.{{MODULE}}.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// ============================================================
// AGGREGATE ROOT
// Table name inferred as "order" (camelCase → snake_case).
// Override with @Table("orders") when the name differs.
// ============================================================

@Table("orders")
public class Order {

    @Id
    private Long id;

    @Column("order_number")
    private String orderNumber;

    // Plain FK — no object navigation across aggregate boundary
    @Column("user_id")
    private Long userId;

    private OrderStatus status;          // enum → VARCHAR via default converter

    private BigDecimal total;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    // ----------------------------------------------------------
    // OWNED COLLECTION (one-to-many within the same aggregate)
    //   idColumn   = FK column in the child table pointing back here
    //   keyColumn  = ordering column (optional, for List ordering)
    // ----------------------------------------------------------
    @MappedCollection(idColumn = "order_id", keyColumn = "item_position")
    private List<OrderItem> items = new ArrayList<>();

    // ----------------------------------------------------------
    // EMBEDDED VALUE OBJECT
    // Columns of ShippingAddress are stored in the SAME row as Order.
    // Use @Embedded.Nullable when all columns may be null.
    // Use @Embedded.Empty to get an empty instance instead of null.
    // ----------------------------------------------------------
    @Embedded.Nullable
    private ShippingAddress shippingAddress;

    // --- Constructors / factory methods ---

    public static Order create(String orderNumber, Long userId) {
        var order = new Order();
        order.orderNumber = orderNumber;
        order.userId      = userId;
        order.status      = OrderStatus.PENDING;
        order.total       = BigDecimal.ZERO;
        order.createdAt   = Instant.now();
        return order;
    }

    // --- Behaviour ---

    public void addItem(AggregateReference<Product, Long> product, int quantity, BigDecimal unitPrice) {
        items.add(new OrderItem(product, quantity, unitPrice));
        recalculateTotal();
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be confirmed");
        }
        this.status    = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    private void recalculateTotal() {
        this.total = items.stream()
            .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Getters (or use Lombok @Getter / record-style) ---

    public Long getId()                          { return id; }
    public String getOrderNumber()               { return orderNumber; }
    public Long getUserId()                      { return userId; }
    public OrderStatus getStatus()               { return status; }
    public BigDecimal getTotal()                 { return total; }
    public List<OrderItem> getItems()            { return List.copyOf(items); }
    public ShippingAddress getShippingAddress()  { return shippingAddress; }
    public Instant getCreatedAt()                { return createdAt; }
}


// ============================================================
// OWNED CHILD ENTITY (no repository — owned by Order)
// Spring Data JDBC derives table name as "order_item".
// ============================================================

class OrderItem {

    @Id
    private Long id;

    // Cross-aggregate reference: stores the product FK only
    private AggregateReference<Product, Long> product;

    private int quantity;

    @Column("unit_price")
    private BigDecimal unitPrice;

    // Required by Spring Data JDBC for reconstruction
    OrderItem() {}

    OrderItem(AggregateReference<Product, Long> product, int quantity, BigDecimal unitPrice) {
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    // Accessors
    public Long getId()                                      { return id; }
    public AggregateReference<Product, Long> getProduct()   { return product; }
    public Long getProductId()                               { return product.getId(); }
    public int quantity()                                    { return quantity; }
    public BigDecimal unitPrice()                            { return unitPrice; }
}


// ============================================================
// EMBEDDED VALUE OBJECT
// No @Table, no @Id — lives inside the parent row.
// ============================================================

public record ShippingAddress(
    @Column("street")  String street,
    @Column("city")    String city,
    @Column("state")   String state,
    @Column("zip")     String zip,
    @Column("country") String country
) {}


// ============================================================
// AGGREGATE ROOT with strongly-typed ID
// ============================================================

@Table("products")
public class Product {

    @Id
    private Long id;        // Long is fine; use a value-type ID for strict DDD

    @Column("code")
    private String code;

    private String name;
    private String description;
    private BigDecimal price;

    @Column("category_id")
    private Long categoryId;    // FK to another aggregate (Category) — plain Long

    private ProductStatus status;

    @Column("created_at")
    private Instant createdAt;

    // Unordered collection (Set — no keyColumn)
    @MappedCollection(idColumn = "product_id")
    private Set<ProductTag> tags;

    // Factory
    public static Product create(String code, String name, BigDecimal price, Long categoryId) {
        var p = new Product();
        p.code       = code;
        p.name       = name;
        p.price      = price;
        p.categoryId = categoryId;
        p.status     = ProductStatus.ACTIVE;
        p.createdAt  = Instant.now();
        return p;
    }

    public Long getId()          { return id; }
    public String getCode()      { return code; }
    public String getName()      { return name; }
    public BigDecimal getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
}


// ============================================================
// OWNED CHILD — simple tag (no FK back to product needed as field,
// Spring Data JDBC manages it via idColumn in @MappedCollection)
// ============================================================

class ProductTag {
    @Id
    private Long id;
    private String value;

    ProductTag() {}
    ProductTag(String value) { this.value = value; }

    public String getValue() { return value; }
}


// ============================================================
// CROSS-AGGREGATE REFERENCE USAGE EXAMPLES
// ============================================================

/*
 * To resolve an AggregateReference, inject the target repository:
 *
 *   @Service
 *   public class OrderService {
 *
 *       private final ProductRepository productRepository;
 *
 *       public void addItemToOrder(Order order, String productCode, int qty) {
 *           Product product = productRepository.findByCode(productCode)
 *               .orElseThrow(() -> new EntityNotFoundException("Product: " + productCode));
 *
 *           order.addItem(
 *               AggregateReference.to(product.getId()),
 *               qty,
 *               product.getPrice()
 *           );
 *       }
 *   }
 */
