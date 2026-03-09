# US-005: Criar Exemplo .elsql com Convenção de Nomenclatura

> Criada em: 2026-03-09
> Complexidade: Baixa
> Dependências: Nenhuma

## Narrativa

**Como** desenvolvedor usando `AbstractElSqlRepository`,
**Quero** um arquivo de exemplo `.elsql` completo demonstrando a convenção de nomenclatura e localização,
**Para** entender rapidamente como organizar meus queries ElSql.

## Contexto Técnico

A classe base `AbstractElSqlRepository` segue uma convenção:
- Repository: `com.exemplo.repo.ProdutoRepositoryImpl`
- ElSql: `src/main/resources/com/exemplo/repo/ProdutoRepositoryImpl.elsql`

Esta história cria um arquivo de exemplo demonstrando:
- Localização correta no classpath
- Tags ElSql comuns (`:name`, `:IF`, `:WHERE`, `:AND`)
- Queries estáticas e dinâmicas
- Boas práticas de organização

## Critérios de Aceite

- [ ] **CA-001**: Arquivo `.elsql` criado em `assets/` (ex: `assets/example-repository.elsql`)
- [ ] **CA-002**: Arquivo demonstra query estática simples
- [ ] **CA-003**: Arquivo demonstra query dinâmica com `:IF`
- [ ] **CA-004**: Arquivo demonstra query dinâmica com `:WHERE` e `:AND`
- [ ] **CA-005**: Arquivo demonstra paginação com `:OFFSET` e `:FETCH`
- [ ] **CA-006**: Comentários explicando cada tag e seu propósito
- [ ] **CA-007**: Comentário no topo explicando a convenção de localização

## Casos de Borda

- [ ] **CB-001**: Query com múltiplas condições `:IF`
- [ ] **CB-002**: Query com `LIKE` e parâmetros
- [ ] **CB-003**: Query com JOIN entre tabelas

## Critérios de Teste

- Verificar que sintaxe ElSql está válida
- Verificar que todos os exemplos funcionariam com `AbstractElSqlRepository`
- Verificar que comentários são claros e educativos

## Notas de Implementação

- Localização: `.claude/skills/spring-data-jdbc/assets/`
- Nome do arquivo: `example-repository.elsql` (ou similar)
- Incluir comentários educativos em cada seção
- Usar nomes de tabelas e colunas genéricos (ex: `products`, `orders`)
- Demonstrar tags mais comuns do ElSql

## Definição de Pronto

- [ ] Todos os critérios de aceite atendidos
- [ ] Arquivo `.elsql` criado em `assets/`
- [ ] Exemplos cobrem tags principais do ElSql
- [ ] Comentários claros e educativos
- [ ] Convenção de localização documentada no topo do arquivo
