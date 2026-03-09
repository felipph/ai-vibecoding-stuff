package {{PACKAGE}}.repository;

import com.opengamma.elsql.ElSqlBundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test examples demonstrating how to test repositories that extend {@link AbstractElSqlRepository}.
 *
 * <p>This class shows three testing approaches:
 * <ol>
 *   <li>Mock DataSource with Mockito — Fast, unit-style testing</li>
 *   <li>Real DataSource with H2 — Integration testing with real SQL execution</li>
 *   <li>Exception testing — Verify error handling when .elsql file is missing</li>
 * </ol>
 *
 * <h2>File Location Convention</h2>
 * <p>The .elsql file must be on the test classpath following the same package structure:
 * <pre>
 * Test class: src/test/java/com/example/repository/AbstractElSqlRepositoryTest.java
 * ElSql file: src/test/resources/com/example/repository/AbstractElSqlRepositoryTest.elsql
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class AbstractElSqlRepositoryTest {

    @Mock
    private DataSource mockDataSource;

    /**
     * Example 1: Testing with Mocked DataSource (Unit Test Style)
     *
     * <p>This approach is fast and suitable for testing logic without actual database execution.
     * Use when you want to verify that queries are being constructed correctly.
     */
    @Test
    void shouldLoadElSqlBundleWithMockedDataSource() {
        // GIVEN: A mock DataSource that returns a real Connection
        // In real tests, you might use Mockito to return a mock Connection
        when(mockDataSource.toString()).thenReturn("MockDataSource");

        // WHEN: Creating a repository that extends AbstractElSqlRepository
        var repository = new TestRepository(mockDataSource);

        // THEN: Verify that the bundle and template were initialized
        assertThat(repository.bundle).isNotNull();
        assertThat(repository.namedJdbc).isNotNull();

        // Verify that the ElSqlBundle was loaded with the correct class
        // The bundle should be able to retrieve SQL fragments
        var sql = repository.getSql("FindAll");
        assertThat(sql).contains("SELECT");  // Basic validation

        // Additional verification: getSqlDinamico evaluates conditional tags
        var params = new MapSqlParameterSource("name", "Test");
        var dynamicSql = repository.getSqlDinamico("FindByName", params);
        assertThat(dynamicSql.getSqlString()).isNotEmpty();
    }

    /**
     * Example 2: Testing with Real DataSource (Integration Test Style)
     *
     * <p>This approach uses an embedded H2 database for real SQL execution.
     * Use when you want to verify that queries actually work with a database.
     */
    @Test
    void shouldExecuteQueryWithRealDataSource() {
        // GIVEN: A real embedded H2 database
        DataSource realDataSource = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:com/example/repository/schema.sql")  // Optional: schema
            .build();

        // WHEN: Creating a repository and executing a query
        var repository = new TestRepository(realDataSource);

        // Insert test data
        realDataSource.getConnection().prepareStatement(
            "CREATE TABLE test_table (id INT, name VARCHAR(255))"
        ).execute();
        realDataSource.getConnection().prepareStatement(
            "INSERT INTO test_table VALUES (1, 'Test')"
        ).execute();

        // Execute query using getSqlDinamico
        var params = new MapSqlParameterSource();
        var sql = repository.getSqlDinamico("FindAll", params);

        // THEN: Verify the query executes successfully
        var results = repository.namedJdbc.query(
            sql.getSqlString(),
            sql.getParameters(),
            (rs, rowNum) -> rs.getString("name")
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo("Test");
    }

    /**
     * Example 3: Testing Exception When .elsql File Not Found
     *
     * <p>This test verifies that AbstractElSqlRepository throws a clear exception
     * when the .elsql file is not on the classpath.
     */
    @Test
    void shouldThrowExceptionWhenElSqlFileNotFound() {
        // GIVEN: A repository implementation whose .elsql file doesn't exist
        // WHEN: Attempting to create the repository
        // THEN: IllegalArgumentException is thrown with a clear message
        assertThatThrownBy(() -> new MissingFileRepository(mockDataSource))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ElSql file not found on classpath")
            .hasMessageContaining("Expected location: src/main/resources/")
            .hasMessageContaining("Repository class:");
    }

    /**
     * Example 4: Testing SQL Fragment Retrieval
     *
     * <p>Demonstrates the difference between {@code getSql()} and {@code getSqlDinamico()}:
     * <ul>
     *   <li>{@code getSql()} — Returns static SQL without evaluating conditional tags</li>
     *   <li>{@code getSqlDinamico()} — Evaluates {@code :IF}, {@code :WHERE} tags based on parameters</li>
     * </ul>
     */
    @Test
    void shouldRetrieveSqlFragmentsCorrectly() {
        // GIVEN: A repository with a real DataSource
        DataSource realDataSource = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();

        var repository = new TestRepository(realDataSource);

        // WHEN: Getting static SQL (no conditional tags)
        String staticSql = repository.getSql("FindAll");
        assertThat(staticSql).contains("SELECT");
        assertThat(staticSql).doesNotContain("WHERE");  // Static query has no WHERE clause

        // WHEN: Getting dynamic SQL (evaluates conditional tags)
        var params = new MapSqlParameterSource("status", "ACTIVE");
        var dynamicSql = repository.getSqlDinamico("FindByStatus", params);
        assertThat(dynamicSql.getSqlString()).contains("WHERE");
        assertThat(dynamicSql.getSqlString()).contains("status");

        // WHEN: All parameters are null (conditional tags evaluate to empty)
        var nullParams = new MapSqlParameterSource("status", null);
        var nullCheckedSql = repository.getSqlDinamico("FindByStatus", nullParams);
        // The :IF tag should evaluate to false, so WHERE might not appear
        // depending on how the ElSql is structured
    }

    // -----------------------------------------------------------------------
    // Test Repository Implementations
    // -----------------------------------------------------------------------

    /**
     * Example repository implementation for testing.
     * <p>Corresponding .elsql file: {@code AbstractElSqlRepositoryTest.elsql}
     */
    static class TestRepository extends AbstractElSqlRepository {

        public TestRepository(DataSource dataSource) {
            super(dataSource);
        }

        // Expose protected members for testing
        public final ElSqlBundle bundle = this.bundle;
        public final NamedParameterJdbcTemplate namedJdbc = this.namedJdbc;
    }

    /**
     * Example repository with missing .elsql file (for exception testing).
     * <p>This class's .elsql file doesn't exist, triggering an exception.
     */
    static class MissingFileRepository extends AbstractElSqlRepository {

        public MissingFileRepository(DataSource dataSource) {
            super(dataSource);
        }
    }
}
