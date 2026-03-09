package {{PACKAGE}}.config;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.ElSqlConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * ElSql Bundle Configuration
 *
 * ElSqlBundle loads an .elsql file from the classpath, parses its named fragments,
 * and evaluates conditional tags (:if, :where, :and, :offsetfetch) at query time.
 *
 * Placement options for .elsql files:
 *   1. Same package as the marker class (recommended for module locality):
 *        ElSqlBundle.of(ElSqlConfig.POSTGRES, ProductQueries.class)
 *        → loads com/example/products/ProductQueries.elsql
 *
 *   2. Explicit classpath resource path:
 *        ElSqlBundle.of(ElSqlConfig.POSTGRES,
 *            ProductQueries.class.getResource("/sql/ProductQueries.elsql"))
 *
 * Dialects control pagination syntax:
 *   POSTGRES  → LIMIT :fetch OFFSET :offset
 *   MYSQL     → LIMIT :offset, :fetch
 *   ORACLE    → ROWNUM-based subquery
 *   MSSQL     → OFFSET :offset ROWS FETCH NEXT :fetch ROWS ONLY
 *   HSQL      → LIMIT :fetch OFFSET :offset  (tests)
 *   H2        → LIMIT :fetch OFFSET :offset  (tests)
 */
@Configuration
public class ElSqlConfig {

    // ------------------------------------------------------------------
    // One bundle per aggregate / service area.
    // Use @Qualifier to inject the correct bundle when multiple exist.
    // ------------------------------------------------------------------

    /**
     * Product aggregate SQL bundle.
     * Loads from: com/example/products/ProductQueries.elsql
     */
    @Bean
    @Qualifier("productElSqlBundle")
    public ElSqlBundle productElSqlBundle() {
        return ElSqlBundle.of(ElSqlConfig.POSTGRES, ProductQueries.class);
    }

    /**
     * Order aggregate SQL bundle.
     * Loads from: com/example/orders/OrderQueries.elsql
     */
    @Bean
    @Qualifier("orderElSqlBundle")
    public ElSqlBundle orderElSqlBundle() {
        return ElSqlBundle.of(ElSqlConfig.POSTGRES, OrderQueries.class);
    }

    /**
     * Reporting / cross-aggregate SQL bundle.
     * Loaded from an explicit resource path for visibility.
     */
    @Bean
    @Qualifier("reportingElSqlBundle")
    public ElSqlBundle reportingElSqlBundle() {
        return ElSqlBundle.of(
            ElSqlConfig.POSTGRES,
            getClass().getResource("/sql/ReportingQueries.elsql")
        );
    }
}


// ------------------------------------------------------------------
// Marker classes — empty, same package as the .elsql file.
// The class serves only to anchor the classpath resource lookup.
// ------------------------------------------------------------------

/** Marker for ProductQueries.elsql */
final class ProductQueries {}

/** Marker for OrderQueries.elsql */
final class OrderQueries {}


// ------------------------------------------------------------------
// TEST CONFIGURATION
// Switch to H2/HSQL dialect for unit/integration tests.
// Annotate with @TestConfiguration in the test class.
// ------------------------------------------------------------------

/*
@TestConfiguration
static class TestElSqlConfig {

    @Bean
    @Primary                             // overrides the production bean
    @Qualifier("productElSqlBundle")
    ElSqlBundle productElSqlBundleTest() {
        // H2 uses same pagination syntax as Postgres for simple LIMIT/OFFSET
        return ElSqlBundle.of(ElSqlConfig.H2, ProductQueries.class);
    }

    @Bean
    @Primary
    @Qualifier("orderElSqlBundle")
    ElSqlBundle orderElSqlBundleTest() {
        return ElSqlBundle.of(ElSqlConfig.H2, OrderQueries.class);
    }
}
*/


// ------------------------------------------------------------------
// PROFILE-BASED DIALECT SWITCHING
// Allows deploying the same code against multiple databases.
// ------------------------------------------------------------------

/*
@Configuration
public class DialectElSqlConfig {

    @Bean
    @Profile("postgres")
    @Qualifier("productElSqlBundle")
    ElSqlBundle productBundlePostgres() {
        return ElSqlBundle.of(ElSqlConfig.POSTGRES, ProductQueries.class);
    }

    @Bean
    @Profile("mysql")
    @Qualifier("productElSqlBundle")
    ElSqlBundle productBundleMysql() {
        return ElSqlBundle.of(ElSqlConfig.MYSQL, ProductQueries.class);
    }

    @Bean
    @Profile("test")
    @Qualifier("productElSqlBundle")
    ElSqlBundle productBundleTest() {
        return ElSqlBundle.of(ElSqlConfig.H2, ProductQueries.class);
    }
}
*/
