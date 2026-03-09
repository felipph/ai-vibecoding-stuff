package {{PACKAGE}}.{{MODULE}}.domain;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.SqlFragments;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

// ============================================================
// 1. CUSTOM INTERFACE
//    Declare additional methods not provided by ListCrudRepository.
// ============================================================

public interface {{NAME}}RepositoryCustom {

    /**
     * Dynamic search with optional filters via ElSql :where/:and blocks.
     */
    List<{{NAME}}Entity> search({{NAME}}SearchCriteria criteria);

    /**
     * Bulk status update — bypasses entity lifecycle, no load required.
     */
    int bulkUpdateStatus({{NAME}}Status from, {{NAME}}Status to);

    /**
     * Targeted price update for a list of IDs.
     */
    int updatePriceById(Long id, BigDecimal newPrice);

    /**
     * High-performance batch insert.
     */
    void bulkInsert(List<{{NAME}}Entity> entities);

    /**
     * Delete by status, returns deleted row count.
     */
    int deleteByStatus({{NAME}}Status status);
}


// ============================================================
// 2. MAIN REPOSITORY
//    Extends both ListCrudRepository and the custom interface.
// ============================================================

public interface {{NAME}}Repository
        extends ListCrudRepository<{{NAME}}Entity, Long>,
                {{NAME}}RepositoryCustom {

    // Derived query methods for simple lookups
    Optional<{{NAME}}Entity> findByCode(String code);
    List<{{NAME}}Entity> findByStatus({{NAME}}Status status);
    boolean existsByCode(String code);
}


// ============================================================
// 3. IMPLEMENTATION
//    Named <Repository>Impl — Spring auto-detects by convention.
//    Inject the ElSqlBundle via @Qualifier to get the right bundle.
// ============================================================

@Repository
class {{NAME}}RepositoryImpl implements {{NAME}}RepositoryCustom {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    {{NAME}}RepositoryImpl(
            @Qualifier("{{name}}ElSqlBundle") ElSqlBundle bundle,
            NamedParameterJdbcTemplate jdbc) {
        this.bundle = bundle;
        this.jdbc   = jdbc;
    }

    // ---- Dynamic Search ----

    @Override
    public List<{{NAME}}Entity> search({{NAME}}SearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("name",       criteria.name())
            .addValue("status",     criteria.status() != null ? criteria.status().name() : null)
            .addValue("minPrice",   criteria.minPrice())
            .addValue("maxPrice",   criteria.maxPrice())
            .addValue("categoryId", criteria.categoryId());

        SqlFragments sql = bundle.getSql("SearchProducts", params);
        return jdbc.query(sql.getSqlString(), sql.getParameters(), entityRowMapper());
    }

    // ---- Bulk Update ----

    @Override
    @Transactional
    public int bulkUpdateStatus({{NAME}}Status from, {{NAME}}Status to) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("fromStatus", from.name())
            .addValue("toStatus",   to.name());

        SqlFragments sql = bundle.getSql("BulkUpdateStatus", params);
        return jdbc.update(sql.getSqlString(), sql.getParameters());
    }

    // ---- Targeted Update ----

    @Override
    @Transactional
    public int updatePriceById(Long id, BigDecimal newPrice) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id",    id)
            .addValue("price", newPrice);

        // Inline for a one-off targeted update; move to .elsql if reused
        return jdbc.update(
            "UPDATE {{TABLE_NAME}} SET price = :price, updated_at = NOW() WHERE id = :id",
            params
        );
    }

    // ---- Batch Insert ----

    @Override
    @Transactional
    public void bulkInsert(List<{{NAME}}Entity> entities) {
        SqlParameterSource[] batchParams = entities.stream()
            .map(e -> new MapSqlParameterSource()
                .addValue("code",      e.code())
                .addValue("name",      e.name())
                .addValue("price",     e.price())
                .addValue("status",    e.status().name())
                .addValue("createdAt", Instant.now()))
            .toArray(SqlParameterSource[]::new);

        jdbc.batchUpdate(
            """
            INSERT INTO {{TABLE_NAME}} (code, name, price, status, created_at)
            VALUES (:code, :name, :price, :status, :createdAt)
            """,
            batchParams
        );
    }

    // ---- Delete ----

    @Override
    @Transactional
    public int deleteByStatus({{NAME}}Status status) {
        MapSqlParameterSource params = new MapSqlParameterSource("status", status.name());
        SqlFragments sql = bundle.getSql("DeleteByStatus", params);
        return jdbc.update(sql.getSqlString(), sql.getParameters());
    }

    // ---- Row Mapper ----

    private static RowMapper<{{NAME}}Entity> entityRowMapper() {
        return (rs, rowNum) -> new {{NAME}}Entity(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBigDecimal("price"),
            {{NAME}}Status.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toInstant()
        );
    }
}


// ============================================================
// ENTITY (for reference — defined separately)
// ============================================================

/*
@Table("{{TABLE_NAME}}")
public class {{NAME}}Entity {

    @Id private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private {{NAME}}Status status;
    @Column("created_at") private Instant createdAt;

    // ... factory, accessors, etc.
}
*/


// ============================================================
// USAGE EXAMPLES IN SERVICE LAYER
// ============================================================

/*
@Service
@Transactional(readOnly = true)
public class {{NAME}}Service {

    private final {{NAME}}Repository repository;

    public {{NAME}}Service({{NAME}}Repository repository) {
        this.repository = repository;
    }

    // READ — inherited readOnly=true
    public List<{{NAME}}Entity> search({{NAME}}SearchCriteria criteria) {
        return repository.search(criteria);
    }

    // WRITE — explicit @Transactional override
    @Transactional
    public void deactivateAll() {
        int count = repository.bulkUpdateStatus({{NAME}}Status.ACTIVE, {{NAME}}Status.INACTIVE);
        log.info("Deactivated {} records", count);
    }

    @Transactional
    public void importAll(List<{{NAME}}Entity> items) {
        repository.bulkInsert(items);
    }
}
*/
