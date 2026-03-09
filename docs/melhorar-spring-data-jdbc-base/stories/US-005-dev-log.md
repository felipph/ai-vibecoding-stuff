# Dev Log — US-005 Criar Exemplo .elsql com Convenção de Nomenclatura

> Branch: `feature/US-005-criar-exemplo-elsql`
> Início: 2026-03-09
> Status: Em desenvolvimento

## Abordagem de Implementação

Esta história é simples e autocontida: criar um único arquivo `.elsql` de exemplo demonstrando:
- Convenção de localização no classpath
- Queries estáticas e dinâmicas
- Tags ElSql comuns (`:name`, `:IF`, `:WHERE`, `:AND`, `:OFFSET`, `:FETCH`)
- Comentários educativos

**Estratégia**: Criar arquivo diretamente em `.claude/skills/spring-data-jdbc/assets/example-repository.elsql`

## Arquivos a Criar/Modificar

- **Novo**: `.claude/skills/spring-data-jdbc/assets/example-repository.elsql`

## Mecanismos

### Mecanismo 1 — Arquivo .elsql de Exemplo

**O que foi feito**:
- [x] Criar arquivo com exemplo completo de queries ElSql
- [x] Documentar convenção de localização no topo
- [x] Incluir query estática simples (`@@findAllActive`)
- [x] Incluir query dinâmica com `:IF` (`@@findByCategory`)
- [x] Incluir query dinâmica com `:WHERE` e `:AND` (`@@searchProducts`)
- [x] Incluir paginação com `:OFFSET` e `:FETCH` (`@@findPaginated`)
- [x] Adicionar comentários explicativos para cada tag

**Cobertura**: N/A (arquivo de documentação/exemplo)

**Decisões técnicas**:
- Usar domínio genérico (products, orders) para ser aplicável a qualquer contexto
- Incluir casos de borda: múltiplas condições `:IF`, `LIKE` com parâmetros, `JOIN`
- Adicionar seção de boas práticas no final do arquivo
- Usar formatação clara com separadores visuais

**Critérios de Aceite Verificados**:
- [x] CA-001: Arquivo criado em `assets/example-repository.elsql`
- [x] CA-002: Query estática simples demonstrada (`@@findAllActive`)
- [x] CA-003: Query dinâmica com `:IF` (`@@findByCategory`)
- [x] CA-004: Query dinâmica com `:WHERE` e `:AND` (`@@searchProducts`)
- [x] CA-005: Paginação com `:OFFSET` e `:FETCH` (`@@findPaginated`)
- [x] CA-006: Comentários explicativos para cada tag
- [x] CA-007: Convenção de localização documentada no topo

**Casos de Borda Cobertos**:
- [x] CB-001: Query com múltiplas condições `:IF` (`@@findComplex`)
- [x] CB-002: Query com `LIKE` e parâmetros (`@@searchByText`)
- [x] CB-003: Query com `JOIN` entre tabelas (`@@findWithCategory`)

## Progresso

- [x] Branch criado
- [x] Mecanismo 1: Arquivo .elsql ✅
- [ ] Validação final
- [ ] Commit via `/commit-task`
