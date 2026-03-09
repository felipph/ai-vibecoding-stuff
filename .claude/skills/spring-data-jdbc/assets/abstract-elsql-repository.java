package {{PACKAGE}}.repository;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.ElSqlConfig;
import com.opengamma.elsql.SqlFragments;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Base class for custom repositories using ElSql.
 *
 * <p>This class automatically loads an ElSql bundle from the classpath following a convention:
 * <pre>
 * Repository: com.exemplo.repo.ProdutoRepositoryImpl
 * ElSql file: src/main/resources/com/exemplo/repo/ProdutoRepositoryImpl.elsql
 * </pre>
 *
 * <p>Provides {@link NamedParameterJdbcTemplate} for query execution and utility methods
 * for retrieving SQL fragments from the bundle.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * @Repository
 * class ProdutoRepositoryImpl extends AbstractElSqlRepository
 *                             implements ProdutoRepositoryCustom {
 *
 *     public ProdutoRepositoryImpl(DataSource ds) {
 *         super(ds);
 *     }
 *
 *     public List<Produto> buscarAtivos() {
 *         var sql = getSqlDinamico("FindActive", new MapSqlParameterSource());
 *         return namedJdbc.query(sql.getSqlString(), sql.getParameters(), produtoRowMapper());
 *     }
 * }
 * }</pre>
 *
 * <p><b>File location convention:</b>
 * <ul>
 *   <li>Repository class: {@code com.exemplo.repo.ProdutoRepositoryImpl}</li>
 *   <li>ElSql file: {@code src/main/resources/com/exemplo/repo/ProdutoRepositoryImpl.elsql}</li>
 * </ul>
 *
 * <p><b>Important notes:</b>
 * <ul>
 *   <li>Uses {@code ElSqlConfig.POSTGRES} dialect (LIMIT/OFFSET pagination)</li>
 *   <li>Only {@code NamedParameterJdbcTemplate} is exposed (not {@code JdbcTemplate})</li>
 *   <li>Throws {@code IllegalArgumentException} if .elsql file is not found on classpath</li>
 * </ul>
 *
 * @see ElSqlBundle
 * @see NamedParameterJdbcTemplate
 * @see ElSqlConfig
 */
public abstract class AbstractElSqlRepository {

    /**
     * The ElSql bundle loaded from the classpath.
     * <p>Bundle is loaded based on the concrete class's package and name following the convention:
     * {@code {package}/{ClassName}.elsql}</p>
     */
    protected final ElSqlBundle bundle;

    /**
     * The NamedParameterJdbcTemplate for executing queries.
     * <p>Use this template for all database operations with named parameters.</p>
     */
    protected final NamedParameterJdbcTemplate namedJdbc;

    /**
     * Creates a new AbstractElSqlRepository.
     *
     * <p>Loads the ElSql bundle from the classpath based on the concrete class's package.
     * For example, {@code com.exemplo.repo.ProdutoRepositoryImpl} will load
     * {@code com/exemplo/repo/ProdutoRepositoryImpl.elsql}.
     *
     * <p>The constructor validates that the .elsql file exists on the classpath.
     * If the file is not found, an {@code IllegalArgumentException} is thrown with a clear message
     * indicating the expected location.
     *
     * @param dataSource the data source for JDBC operations
     * @throws IllegalArgumentException if the .elsql file is not found on the classpath
     */
    protected AbstractElSqlRepository(DataSource dataSource) {
        String elSqlPath = getElSqlResourcePath();
        var resource = getClass().getClassLoader().getResource(elSqlPath);

        if (resource == null) {
            throw new IllegalArgumentException(
                "ElSql file not found on classpath: " + elSqlPath +
                "\nExpected location: src/main/resources/" + elSqlPath +
                "\nRepository class: " + getClass().getName()
            );
        }

        this.bundle = ElSqlBundle.of(ElSqlConfig.POSTGRES, getClass());
        this.namedJdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Returns the SQL for the named fragment without evaluating conditional tags.
     *
     * <p>Use this method for static SQL queries that don't use {@code :IF}, {@code :WHERE},
     * or other dynamic ElSql tags. The SQL is returned as a plain string.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * // .elsql file:
     * // -- :name FindAllActive
     * // SELECT * FROM products WHERE status = 'ACTIVE'
     *
     * String sql = getSql("FindAllActive");
     * // Returns: "SELECT * FROM products WHERE status = 'ACTIVE'"
     * }</pre>
     *
     * @param name the fragment name in the .elsql file
     * @return the SQL string without conditional tag evaluation
     * @throws com.opengamma.elsql.ElSqlException if the named fragment is not found
     */
    protected String getSql(String name) {
        return bundle.getSql(name, new MapSqlParameterSource())
                    .getSqlString();
    }

    /**
     * Returns the SQL for the named fragment, evaluating all conditional tags.
     *
     * <p>Use this method for dynamic SQL queries with {@code :IF}, {@code :WHERE},
     * {@code :AND}, and other conditional ElSql tags. The returned {@link SqlFragments}
     * contains both the SQL string and the evaluated parameters.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * // .elsql file:
     * // -- :name SearchProducts
     * // SELECT * FROM products
     * // WHERE 1=1
     * // :IF name
     * // AND name LIKE :name
     * // :ENDIF
     *
     * var params = new MapSqlParameterSource("name", "Widget");
     * SqlFragments sql = getSqlDinamico("SearchProducts", params);
     * namedJdbc.query(sql.getSqlString(), sql.getParameters(), rowMapper);
     * }</pre>
     *
     * @param name the fragment name in the .elsql file
     * @param params the parameters for conditional tag evaluation and query execution
     * @return the SQL fragments with evaluated conditionals
     * @throws com.opengamma.elsql.ElSqlException if the named fragment is not found
     */
    protected SqlFragments getSqlDinamico(String name, MapSqlParameterSource params) {
        return bundle.getSql(name, params);
    }

    /**
     * Returns the expected classpath resource path for the .elsql file.
     *
     * <p>The path follows the convention: {@code {package}/{ClassName}.elsql}
     * <p>For example:
     * <ul>
     *   <li>{@code com.exemplo.repo.ProdutoRepositoryImpl} → {@code com/exemplo/repo/ProdutoRepositoryImpl.elsql}</li>
     *   <li>{@code com.myapp.repositories.UserRepositoryImpl} → {@code com/myapp/repositories/UserRepositoryImpl.elsql}</li>
     * </ul>
     *
     * @return the classpath resource path for the .elsql file
     */
    private String getElSqlResourcePath() {
        String className = getClass().getName();
        return className.replace('.', '/') + ".elsql";
    }
}
