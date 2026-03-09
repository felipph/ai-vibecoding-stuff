# Dev Log — US-004 Substituir custom-repositories.md com Novo Padrão

> Branch: `feature/US-004-substituir-custom-repositories-md`
> Início: 2026-03-09
> Status: Em desenvolvimento

## Abordagem de Implementação

Vou reescrever completamente o arquivo `references/custom-repositories.md` para apresentar
`AbstractElSqlRepository` como o padrão recomendado para custom repositories.

O novo conteúdo focará em:
- Uso da classe base AbstractElSqlRepository
- Exemplos simplificados de custom repository
- Convenção de localização de arquivos
- Bulk operations com a classe base
- Quando NÃO usar a classe base

## Arquivos a Criar/Modificar

- `.claude/skills/spring-data-jdbc/references/custom-repositories.md` (SUBSTITUIR COMPLETAMENTE)

## Mecanismos a Implementar

### Mecanismo 1: Reescrever custom-repositories.md
- **O que será feito**: Substituir todo o conteúdo para usar AbstractElSqlRepository como padrão
- **Decisões técnicas**:
  - Manter estrutura geral do documento (introdução, exemplos, etc.)
  - Focar nos benefícios: menos código, mais limpo
  - Adicionar seção sobre quando NÃO usar a classe base
  - Incluir nota de migração da abordagem manual
- **Cobertura esperada**: N/A (documentação)

---

## Progresso

- [x] Criar branch git
- [x] Criar Dev Log
- [x] Reescrever custom-repositories.md com AbstractElSqlRepository
- [x] Verificar formatação e exemplos
- [x] Registrar no Dev Log
- [ ] Commit via /commit-task

---

## Log de Implementação

### Mecanismo 1: Reescrever custom-repositories.md

**O que foi feito:**
- Arquivo `references/custom-repositories.md` reescrito completamente
- Novo conteúdo foca em `AbstractElSqlRepository` como padrão recomendado
- Seções incluídas:
  - Quick Start com AbstractElSqlRepository
  - Convenção de localização de arquivos
  - Exemplo completo (interface, implementação, repository principal)
  - Arquivo .elsql correspondente
  - Queries dinâmicas com ElSql (`:IF`, `:WHERE`, `:AND`)
  - Batch update com `batchUpdate`
  - Upsert (INSERT OR UPDATE)
  - Delete com retorno de contagem
  - Seção "When NOT to Use AbstractElSqlRepository"
  - Melhores práticas
  - Testes de custom repository
  - Nota de migração da abordagem manual

**Decisões técnicas:**
- Estrutura geral do documento mantida (introdução, exemplos, etc.)
- Foco nos benefícios: menos código, mais limpo, menos propenso a erros
- Seção sobre quando NÃO usar a classe base (dialetos diferentes, múltiplos bundles)
- Exemplos de migração mostrando antes/depois
- Notas sobre testes simplificados (sem @Qualifier necessário)

**Critérios de aceite atendidos:**
- ✅ CA-001: Arquivo reescrito completamente
- ✅ CA-002: AbstractElSqlRepository como padrão
- ✅ CA-003: Exemplo completo com interface e implementação
- ✅ CA-004: Exemplo de arquivo .elsql correspondente
- ✅ CA-005: Convenção de localização documentada
- ✅ CA-006: Seção "When NOT to Use" incluída
- ✅ CA-007: Exemplos manuais removidos (apenas em nota de migração)
- ✅ CA-008: Exemplos de bulk operations incluídos (batchUpdate, upsert, delete)

**Casos de borda cobertos:**
- CB-001: Repository com múltiplos métodos (search, bulkUpdateStatus, bulkInsert)
- CB-002: Queries dinâmicas com :IF e :WHERE (seção específica)
- CB-003: Bulk operations (batchUpdate, upsert, delete múltiplos)
