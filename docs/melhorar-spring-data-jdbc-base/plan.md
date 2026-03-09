# Plano — melhorar-spring-data-jdbc-base
> Atualizado na Fase 2 — 2026-03-09
> Versão 1 — criado em: 2026-03-09

## Refinamentos (v2)

Baseado nas decisões tomadas na Fase 2, as seguintes melhorias foram aplicadas ao plano:

1. **Simplificação dos Templates JDBC**: Removido `JdbcTemplate` — apenas `NamedParameterJdbcTemplate` será exposto, pois é o adequado para ElSql.

2. **Nomenclatura Definida**: Classe base será chamada `AbstractElSqlRepository`, seguindo padrão de nomenclatura do Spring Framework.

3. **Estratégia de Erro**: Implementar verificação explícita no construtor para lançar `IllegalArgumentException` com mensagem clara se o arquivo `.elsql` não for encontrado.

4. **Documentação**: Substituir completamente os exemplos em `references/custom-repositories.md` para usar a classe base como padrão recomendado.

5. **Testes**: Adicionar asset de teste simplificado mostrando como testar repositories que estendem `AbstractElSqlRepository`.

6. **Sem Customização de Dialecto**: Removida a opção de sobrescrita de dialeto. Manter POSTGRES como padrão. Quem precisar de outro dialeto deve usar a abordagem manual.

---

## Escopo da Entrega

Criar e documentar uma **classe abstrata base** para simplificar custom repositories Spring Data JDBC que utilizam ElSql. A classe seguirá convenções de configuração over configuration para eliminar código repetido.

**Artefatos a criar:**
1. `AbstractElSqlRepository` — classe base abstrata
2. Asset template: `assets/abstract-elsql-repository.java`
3. Atualização de `SKILL.md` — adicionar seção sobre a classe base
4. Atualização de `references/custom-repositories.md` — mostrar novo padrão simplificado
5. Exemplo de uso migrado para a classe base

## Abordagem Técnica

### 1. Classe Base Abstrata

```java
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
 * <p>Automatically loads an ElSql bundle from the classpath following the convention:
 * <pre>
 * Repository: com.exemplo.repo.ProdutoRepositoryImpl
 * ElSql file: src/main/resources/com/exemplo/repo/ProdutoRepositoryImpl.elsql
 * </pre>
 *
 * <p>Provides {@link NamedParameterJdbcTemplate} for query execution and utility methods
 * for retrieving SQL fragments from the bundle.
 *
 * @see ElSqlBundle
 * @see NamedParameterJdbcTemplate
 */
public abstract class AbstractElSqlRepository {

    protected final ElSqlBundle bundle;
    protected final NamedParameterJdbcTemplate namedJdbc;

    /**
     * Creates a new AbstractElSqlRepository.
     *
     * <p>Loads the ElSql bundle from the classpath based on the concrete class's package.
     * For example, {@code com.exemplo.repo.ProdutoRepositoryImpl} will load
     * {@code com/exemplo/repo/ProdutoRepositoryImpl.elsql}.
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
     * <p>Use this for static SQL queries that don't use {@code :IF} or other dynamic tags.
     *
     * @param name the fragment name in the .elsql file
     * @return the SQL string
     */
    protected String getSql(String name) {
        return bundle.getSql(name, new MapSqlParameterSource())
                    .getSqlString();
    }

    /**
     * Returns the SQL for the named fragment, evaluating all conditional tags.
     *
     * <p>Use this for dynamic SQL queries with {@code :IF}, {@code :WHERE}, etc.
     *
     * @param name the fragment name in the .elsql file
     * @param params the parameters for conditional tag evaluation
     * @return the SQL fragments with evaluated conditionals
     */
    protected SqlFragments getSqlDinamico(String name, MapSqlParameterSource params) {
        return bundle.getSql(name, params);
    }

    private String getElSqlResourcePath() {
        String className = getClass().getName();
        return className.replace('.', '/') + ".elsql";
    }
}
```

**Convenção de localização:**
- Repository: `com.exemplo.repo.ProdutoRepositoryImpl`
- ElSql: `src/main/resources/com/exemplo/repo/ProdutoRepositoryImpl.elsql`

### 2. Exemplo de Uso (Antes vs Depois)

**Antes (manual - sem classe base):**
```java
@Repository
class ProdutoRepositoryImpl implements ProdutoRepositoryCustom {
    private final ElSqlBundle bundle;
    private final NamedParameterJdbcTemplate jdbc;

    public ProdutoRepositoryImpl(DataSource ds) {
        this.bundle = ElSqlBundle.of(ElSqlConfig.POSTGRES, ProdutoQueries.class);
        this.jdbc = new NamedParameterJdbcTemplate(ds);
    }

    public List<Produto> buscarAtivos() {
        var sql = bundle.getSql("FindActive", new MapSqlParameterSource());
        return jdbc.query(sql.getSqlString(), sql.getParameters(), produtoRowMapper());
    }
}
```

**Depois (com AbstractElSqlRepository):**
```java
@Repository
class ProdutoRepositoryImpl extends AbstractElSqlRepository
                            implements ProdutoRepositoryCustom {

    public ProdutoRepositoryImpl(DataSource ds) {
        super(ds);  // Bundle e template criados automaticamente
    }

    public List<Produto> buscarAtivos() {
        var sql = getSqlDinamico("FindActive", new MapSqlParameterSource());
        return namedJdbc.query(sql.getSqlString(), sql.getParameters(), produtoRowMapper());
    }
}
```

**Arquivo ElSql:** `src/main/resources/com/exemplo/repo/ProdutoRepositoryImpl.elsql`
```sql
-- :name FindActive
-- SELECT id, nome, preco, status
-- FROM produtos
-- WHERE status = 'ACTIVE'
-- ORDER BY nome
```

### 3. Tratamento de Erros

- **Arquivo .elsql não encontrado**: Lançar `IllegalArgumentException` no construtor com mensagem clara indicando o caminho esperado e a classe do repository.
- **Query nomeada não existe**: Deixar o `ElSqlBundle` lançar sua própria exception (comportamento padrão da biblioteca).

**Exemplo de mensagem de erro:**
```
IllegalArgumentException: ElSql file not found on classpath: com/exemplo/repo/ProdutoRepositoryImpl.elsql
Expected location: src/main/resources/com/exemplo/repo/ProdutoRepositoryImpl.elsql
Repository class: com.exemplo.repo.ProdutoRepositoryImpl
```

### 4. Dialeto SQL

- **Padrão**: `ElSqlConfig.POSTGRES` (funiona para a maioria dos bancos modernos com LIMIT/OFFSET)
- **Mudança de dialeto**: Não suportado via sobrescrita. Quem precisar de outro dialeto deve usar a abordagem manual sem a classe base.
- **Justificativa**: Manter a classe base simples. A grande maioria dos projetos usa Postgres ou compatível.

## Ordem de Implementação Sugerida

1. ✅ **Planejamento e Refinamento** (fase atual)
2. Criar arquivo asset `assets/abstract-elsql-repository.java` com a classe base completa
3. Criar asset `assets/abstract-elsql-repository-test.java` com exemplo de teste
4. Atualizar `SKILL.md`:
   - Adicionar seção "Abstract Base Class" após "Custom Repository"
   - Adicionar referência aos novos assets
5. **Substituir completamente** `references/custom-repositories.md`:
   - Reescrever exemplos para usar `AbstractElSqlRepository` como padrão recomendado
   - Remover exemplos da abordagem manual
6. Criar exemplo `.elsql` demonstrando a convenção de nomenclatura

## Dependências Externas

- ✅ ElSql library (já existente na skill)
- ✅ Spring Data JDBC (já existente na skill)
- ✅ Spring JDBC Core (já existente na skill)

## Fora do Escopo

- ❌ Suporte a dialetos customizáveis via sobrescrita de método
- ❌ Modificação da funcionalidade existente da skill (apenas adição)
- ❌ Novos assets para CQRS (já existe em `references/cqrs-query-service.md`)
- ❌ Integração com outras tecnologias (R2DBC, jOOQ, etc.)
- ❌ Suporte a múltiplos bundles por repository (um arquivo .elsql por classe)

## Critérios de Sucesso

- [ ] Classe base `AbstractElSqlRepository` criada e documentada com JavaDoc completo
- [ ] Apenas `NamedParameterJdbcTemplate` exposto (sem `JdbcTemplate`)
- [ ] Verificação explícita de arquivo .elsql com mensagem de erro clara
- [ ] Asset de teste demonstrando como testar repositories que estendem a classe base
- [ ] `SKILL.md` atualizada com seção sobre a classe base
- [ ] `references/custom-repositories.md` reescrito com `AbstractElSqlRepository` como padrão
- [ ] Convenção de localização de arquivos clara e documentada
- [ ] Código funcional e pronto para uso em projetos reais
