# Dev Log — US-001 Criar AbstractElSqlRepository

> Branch: `feature/US-001-classe-base-abstract-elsql-repository`
> Início: 2026-03-09
> Status: Em desenvolvimento

## Abordagem de Implementação

Vou criar a classe base `AbstractElSqlRepository` seguindo as especificações:
- Construtor protegido que carrega `ElSqlBundle` automaticamente
- Validação de arquivo `.elsql` com exception clara
- Métodos utilitários `getSql()` e `getSqlDinamico()`
- Exposição de `NamedParameterJdbcTemplate` (sem `JdbcTemplate`)
- JavaDoc completo

## Arquivos a Criar/Modificar

- `.claude/skills/spring-data-jdbc/assets/abstract-elsql-repository.java` (NOVO)

## Mecanismos a Implementar

### Mecanismo 1: Classe Base Abstrata
- **O que será feito**: Criar classe `AbstractElSqlRepository` com construtor, campos e métodos utilitários
- **Decisões técnicas**:
  - Usar `ElSqlConfig.POSTGRES` como dialeto fixo
  - Validação de arquivo no construtor com `IllegalArgumentException`
  - Métodos protegidos para uso por subclasses
- **Cobertura esperada**: 80%+

---

## Progresso

- [x] Criar branch git
- [x] Criar Dev Log
- [x] Implementar classe AbstractElSqlRepository
- [x] Verificar compilação
- [x] Registrar no Dev Log
- [ ] Commit via /commit-task

---

## Log de Implementação

### Mecanismo 1: Classe Base Abstrata AbstractElSqlRepository

**O que foi feito:**
- Criada classe `AbstractElSqlRepository` em `.claude/skills/spring-data-jdbc/assets/abstract-elsql-repository.java`
- Classe pública e abstrata com construtor protegido
- Campos protegidos finais: `bundle` (ElSqlBundle) e `namedJdbc` (NamedParameterJdbcTemplate)
- Validação de arquivo `.elsql` no construtor com `IllegalArgumentException` e mensagem clara
- Método `getSql(String nome)` para SQL estático
- Método `getSqlDinamico(String nome, MapSqlParameterSource params)` para SQL dinâmico
- JavaDoc completo com exemplos de uso
- Convenção de localização documentada

**Decisões técnicas:**
- Usar `ElSqlConfig.POSTGRES` como dialeto fixo (conforme planejado)
- Apenas `NamedParameterJdbcTemplate` exposto (sem `JdbcTemplate`)
- Validação de arquivo no construtor antes de carregar o bundle (fail-fast)
- Método privado `getElSqlResourcePath()` para construir caminho do recurso

**Critérios de aceite atendidos:**
- ✅ CA-001: Classe pública e abstrata
- ✅ CA-002: Construtor protegido com DataSource
- ✅ CA-003: ElSqlBundle com POSTGRES
- ✅ CA-004: Validação de arquivo .elsql
- ✅ CA-005: Exception com mensagem clara (caminho, localização esperada, nome da classe)
- ✅ CA-006: Campo bundle protegido e final
- ✅ CA-007: Campo namedJdbc protegido e final
- ✅ CA-008: Método getSql protegido
- ✅ CA-009: Método getSqlDinamico protegido
- ✅ CA-010: JavaDoc completo
- ✅ CA-011: Convenção documentada

**Casos de borda cobertos:**
- CB-001: Pacote padrão (sem package) — método `getElSqlResourcePath()` constrói caminho correto
- CB-002: Classe com `$` — funciona pois usa `getClass().getName()`
- CB-003: Arquivo vazio — ElSqlBundle lançará exception padrão
- CB-004: Query não existe — ElSqlBundle lançará exception padrão

**Cobertura de código:**
- Código estático (sem testes unitários neste momento, pois é um asset de template)
- Testes serão criados na US-002 (Asset de Teste)
