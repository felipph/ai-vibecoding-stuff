# US-004: Substituir custom-repositories.md com Novo Padrão

> Criada em: 2026-03-09
> Complexidade: Média
> Dependências: US-001

## Narrativa

**Como** usuário da skill `spring-data-jdbc`,
**Quero** que a documentação de Custom Repositories apresente `AbstractElSqlRepository` como o padrão recomendado,
**Para** seguir as melhores práticas atuais e escrever menos código repetido.

## Contexto Técnico

O arquivo `references/custom-repositories.md` documenta o padrão de Custom Repository.
Atualmente ele mostra a abordagem manual:
- Criar `ElSqlBundle` manualmente
- Criar `NamedParameterJdbcTemplate` manualmente
- Implementar interface custom

Esta história substitui completamente o conteúdo para usar `AbstractElSqlRepository` como padrão, mostrando:
- Herança da classe base
- Código significativamente mais simples
- Convenção de localização de arquivos

## Critérios de Aceite

- [ ] **CA-001**: Arquivo `references/custom-repositories.md` reescrito completamente
- [ ] **CA-002**: Exemplos usam `AbstractElSqlRepository` como padrão
- [ ] **CA-003**: Exemplo completo de custom repository com interface e implementação
- [ ] **CA-004**: Exemplo de arquivo `.elsql` correspondente
- [ ] **CA-005**: Seção explicando a convenção de localização de arquivos
- [ ] **CA-006**: Seção explicando quando NÃO usar a classe base (dialeto diferente, etc.)
- [ ] **CA-007**: Exemplos da abordagem manual REMOVIDOS ou movidos para nota histórica
- [ ] **CA-008**: Exemplos de bulk operations com `AbstractElSqlRepository`

## Casos de Borda

- [ ] **CB-001**: Repository com múltiplos métodos (GET, POST, DELETE)
- [ ] **CB-002**: Repository com queries dinâmicas usando `:IF` e `:WHERE`
- [ ] **CB-003**: Repository com bulk operations (batch update, delete multiple)

## Critérios de Teste

- Verificar que todos os exemplos de código estão corretos
- Verificar que sintaxe Java está válida
- Verificar que sintaxe ElSql está válida
- Verificar que caminhos de arquivo estão corretos

## Notas de Implementação

- Localização: `.claude/skills/spring-data-jdbc/references/custom-repositories.md`
- Substituir completamente (não apenas adicionar)
- Manter estrutura geral do documento (introdução, exemplos, etc.)
- Adicionar nota sobre "Migration from Manual Approach" se apropriado
- Focar em benefícios: menos código, mais limpo, menos propenso a erros

## Definição de Pronto

- [ ] Todos os critérios de aceite atendidos
- [ ] Arquivo reescrito com `AbstractElSqlRepository` como padrão
- [ ] Exemplos claros e funcionais
- [ ] Convenção de arquivos bem documentada
- [ ] Nota sobre quando não usar a classe base
