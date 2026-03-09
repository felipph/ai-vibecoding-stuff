# US-001: Criar AbstractElSqlRepository

> Criada em: 2026-03-09
> Complexidade: Média
> Dependências: Nenhuma

## Narrativa

**Como** desenvolvedor trabalhando com Spring Data JDBC e ElSql,
**Quero** uma classe base abstrata que carregue automaticamente bundles ElSql e forneça métodos simplificados para executar queries,
**Para** eliminar código repetido em custom repositories e seguir uma convenção clara de localização de arquivos.

## Contexto Técnico

Custom repositories Spring Data JDBC que utilizam ElSql atualmente precisam:
1. Criar manualmente o `ElSqlBundle`
2. Criar manualmente o `NamedParameterJdbcTemplate`
3. Repetir esse código em cada repository

Esta história cria uma classe base que:
- Carrega automaticamente o bundle ElSql baseado na estrutura de pacotes
- Valida a existência do arquivo `.elsql` no classpath
- Fornece métodos utilitários `getSql()` e `getSqlDinamico()`
- Expõe `NamedParameterJdbcTemplate` para execução de queries

## Critérios de Aceite

- [ ] **CA-001**: Classe `AbstractElSqlRepository` criada como pública e abstrata
- [ ] **CA-002**: Construtor protegido recebe `DataSource` como parâmetro
- [ ] **CA-003**: Construtor carrega `ElSqlBundle` usando `ElSqlConfig.POSTGRES`
- [ ] **CA-004**: Construtor valida existência do arquivo `.elsql` no classpath
- [ ] **CA-005**: Se arquivo não existe, lança `IllegalArgumentException` com mensagem clara contendo:
  - Caminho do arquivo não encontrado
  - Localização esperada (`src/main/resources/...`)
  - Nome da classe repository
- [ ] **CA-006**: Campo protegido `bundle` do tipo `ElSqlBundle` (final)
- [ ] **CA-007**: Campo protegido `namedJdbc` do tipo `NamedParameterJdbcTemplate` (final)
- [ ] **CA-008**: Método protegido `getSql(String nome)` retorna SQL estático sem avaliar tags condicionais
- [ ] **CA-009**: Método protegido `getSqlDinamico(String nome, MapSqlParameterSource params)` retorna `SqlFragments` com tags avaliadas
- [ ] **CA-010**: JavaDoc completo na classe e em todos os métodos públicos/protegidos
- [ ] **CA-011**: Convenção documentada: arquivo `.elsql` segue estrutura de pacotes da classe

## Casos de Borda

- [ ] **CB-001**: Repository em pacote padrão (sem package) — deve construir caminho correto
- [ ] **CB-002**: Repository com nome contendo `$` (classe aninhada) — deve funcionar
- [ ] **CB-003**: Arquivo `.elsql` vazio — deve deixar `ElSqlBundle` lançar exception padrão
- [ ] **CB-004**: Query nomeada não existe — deve deixar `ElSqlBundle` lançar exception padrão

## Critérios de Teste

- **Cobertura mínima**: 80%
- **Testes unitários**: Validar carregamento de bundle, caminho do arquivo, exception quando arquivo não existe
- **Testes de integração**: Criar repository de exemplo que estende a classe base e executar query real

## Notas de Implementação

- Localização: `.claude/skills/spring-data-jdbc/assets/abstract-elsql-repository.java`
- Não expor `JdbcTemplate` (apenas `NamedParameterJdbcTemplate`)
- Usar `POSTGRES` como dialeto fixo (sem customização)
- Nome da classe: `AbstractElSqlRepository` (segue padrão Spring Framework)
- Arquivo ElSql: `{package.replace('.', '/')}/{ClassName}.elsql`

## Definição de Pronto

- [ ] Todos os critérios de aceite atendidos
- [ ] Código compilando sem erros
- [ ] JavaDoc completo e claro
- [ ] Asset criado em `.claude/skills/spring-data-jdbc/assets/`
- [ ] Sem `// TODO` ou código de debug
