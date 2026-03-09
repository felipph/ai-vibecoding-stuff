package {{PACKAGE}}.{{MODULE}}.domain;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.SqlFragments;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * CQRS Query Service — read-only side of the {{NAME}} aggregate.
 *
 * Responsibilities:
 *   - Return View Models (never entities) to callers
 *   - Execute SQL via ElSqlBundle + NamedParameterJdbcTemplate
 *   - No writes — any write attempt should call the domain service or command handler
 *
 * Design rules:
 *   - All SQL lives in {{NAME}}Queries.elsql — never inline strings here
 *   - Class is @Transactional(readOnly = true) — no per-method annotation needed for reads
 *   - Use MapSqlParameterSource.addValue(..., null) for optional params (ElSql :and skips them)
 *
 * Paired with:
 *   - {{NAME}}Repository (package-private) — write side
 *   - {{NAME}}Queries.elsql               — all SQL fragments
 *   - ElSqlConfig                         — bean wiring
 */
@Service
@Transactional(readOnly = true)
public class {{NAME}}QueryService {

    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    public {{NAME}}QueryService(
            @Qualifier("{{name}}ElSqlBundle") ElSqlBundle bundle,
            NamedParameterJdbcTemplate jdbc) {
        this.bundle = bundle;
        this.jdbc   = jdbc;
    }

    // ==================== SIMPLE QUERIES ====================

    /**
     * Returns all active items as lightweight View Models.
     */
    public List<{{NAME}}VM> findAllActive() {
        SqlFragments sql = bundle.getSql("FindAllActive", new MapSqlParameterSource());
        return jdbc.query(sql.getSqlString(), sql.getParameters(), {{NAME}}VM.rowMapper());
    }

    /**
     * Returns a single item by ID, or empty if not found.
     */
    public Optional<{{NAME}}DetailsVM> findDetailsById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        SqlFragments sql = bundle.getSql("GetDetailById", params);
        List<{{NAME}}DetailsVM> results = jdbc.query(
            sql.getSqlString(), sql.getParameters(), {{NAME}}DetailsVM.rowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Returns a single item by business code, or empty if not found.
     */
    public Optional<{{NAME}}VM> findByCode(String code) {
        MapSqlParameterSource params = new MapSqlParameterSource("code", code);
        SqlFragments sql = bundle.getSql("GetByCode", params);
        List<{{NAME}}VM> results = jdbc.query(
            sql.getSqlString(), sql.getParameters(), {{NAME}}VM.rowMapper());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // ==================== DYNAMIC SEARCH ====================

    /**
     * Dynamic multi-filter search with pagination.
     * All criteria fields are optional — null values are ignored.
     */
    public Page<{{NAME}}VM> search({{NAME}}SearchCriteria criteria, int page, int size) {
        MapSqlParameterSource params = buildSearchParams(criteria)
            .addValue("offset", (long) page * size)
            .addValue("fetch",  size);

        Long total = jdbc.queryForObject(
            bundle.getSql("CountSearch", params).getSqlString(),
            params,
            Long.class
        );

        List<{{NAME}}VM> content = jdbc.query(
            bundle.getSql("Search", params).getSqlString(),
            params,
            {{NAME}}VM.rowMapper()
        );

        return new PageImpl<>(content, PageRequest.of(page, size), total != null ? total : 0L);
    }

    private MapSqlParameterSource buildSearchParams({{NAME}}SearchCriteria criteria) {
        return new MapSqlParameterSource()
            .addValue("code",       criteria.code())
            .addValue("name",       criteria.name())       // e.g. "%widget%" pre-wrapped
            .addValue("status",     criteria.status() != null ? criteria.status().name() : null)
            .addValue("minPrice",   criteria.minPrice())
            .addValue("maxPrice",   criteria.maxPrice())
            .addValue("categoryId", criteria.categoryId())
            .addValue("fromDate",   criteria.fromDate())
            .addValue("toDate",     criteria.toDate());
        // null values tell ElSql :and blocks to exclude that condition
    }

    // ==================== PAGINATION ====================

    /**
     * Returns a fixed-criteria page of active items.
     */
    public Page<{{NAME}}VM> findActivePage(int page, int size) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("offset", (long) page * size)
            .addValue("fetch",  size);

        Long total = jdbc.queryForObject(
            bundle.getSql("CountActive", new MapSqlParameterSource()).getSqlString(),
            new MapSqlParameterSource(),
            Long.class
        );

        List<{{NAME}}VM> content = jdbc.query(
            bundle.getSql("FindActivePaged", params).getSqlString(),
            params,
            {{NAME}}VM.rowMapper()
        );

        return new PageImpl<>(content, PageRequest.of(page, size), total != null ? total : 0L);
    }

    // ==================== AGGREGATIONS ====================

    /**
     * Returns stats grouped by category.
     */
    public List<{{NAME}}StatVM> statsByCategory() {
        SqlFragments sql = bundle.getSql("StatsPerCategory", new MapSqlParameterSource());
        return jdbc.query(sql.getSqlString(), sql.getParameters(), {{NAME}}StatVM.rowMapper());
    }

    // ==================== IN CLAUSE ====================

    /**
     * Loads items for a given set of IDs.
     */
    public List<{{NAME}}VM> findByIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);
        SqlFragments sql = bundle.getSql("FindByIds", params);
        return jdbc.query(sql.getSqlString(), sql.getParameters(), {{NAME}}VM.rowMapper());
    }

    // ==================== EXISTS ====================

    public boolean existsById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        SqlFragments sql = bundle.getSql("ExistsById", params);
        Long count = jdbc.queryForObject(sql.getSqlString(), sql.getParameters(), Long.class);
        return count != null && count > 0;
    }
}


// ============================================================
// VIEW MODELS — immutable records with inline RowMapper
// ============================================================

/**
 * Lightweight VM for list/summary display.
 */
record {{NAME}}VM(
    Long id,
    String code,
    String name,
    BigDecimal price,
    String status,
    Instant createdAt
) {
    static RowMapper<{{NAME}}VM> rowMapper() {
        return (rs, rowNum) -> new {{NAME}}VM(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBigDecimal("price"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toInstant()
        );
    }
}

/**
 * Detailed VM with nested category info.
 */
record {{NAME}}DetailsVM(
    Long id,
    String code,
    String name,
    String description,
    BigDecimal price,
    String status,
    Instant createdAt,
    Instant updatedAt,
    CategoryVM category
) {
    record CategoryVM(Long id, String name) {}

    static RowMapper<{{NAME}}DetailsVM> rowMapper() {
        return (rs, rowNum) -> new {{NAME}}DetailsVM(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getString("status"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toInstant() : null,
            new CategoryVM(
                rs.getLong("category_id"),
                rs.getString("category_name")
            )
        );
    }
}

/**
 * Stats VM for aggregation queries.
 */
record {{NAME}}StatVM(
    String categoryName,
    long itemCount,
    BigDecimal avgPrice,
    BigDecimal minPrice,
    BigDecimal maxPrice
) {
    static RowMapper<{{NAME}}StatVM> rowMapper() {
        return (rs, rowNum) -> new {{NAME}}StatVM(
            rs.getString("category_name"),
            rs.getLong("item_count"),
            rs.getBigDecimal("avg_price"),
            rs.getBigDecimal("min_price"),
            rs.getBigDecimal("max_price")
        );
    }
}

/**
 * Search criteria — all fields optional (null = not applied).
 */
record {{NAME}}SearchCriteria(
    String code,
    String name,
    {{NAME}}Status status,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Long categoryId,
    Instant fromDate,
    Instant toDate
) {
    /** Builder-style factory for convenience. */
    static Builder builder() { return new Builder(); }

    static final class Builder {
        private String code, name;
        private {{NAME}}Status status;
        private BigDecimal minPrice, maxPrice;
        private Long categoryId;
        private Instant fromDate, toDate;

        Builder code(String v)            { this.code = v; return this; }
        Builder name(String v)            { this.name = v; return this; }
        Builder status({{NAME}}Status v)  { this.status = v; return this; }
        Builder minPrice(BigDecimal v)    { this.minPrice = v; return this; }
        Builder maxPrice(BigDecimal v)    { this.maxPrice = v; return this; }
        Builder categoryId(Long v)        { this.categoryId = v; return this; }
        Builder fromDate(Instant v)       { this.fromDate = v; return this; }
        Builder toDate(Instant v)         { this.toDate = v; return this; }

        {{NAME}}SearchCriteria build() {
            return new {{NAME}}SearchCriteria(
                code, name, status, minPrice, maxPrice, categoryId, fromDate, toDate);
        }
    }
}
