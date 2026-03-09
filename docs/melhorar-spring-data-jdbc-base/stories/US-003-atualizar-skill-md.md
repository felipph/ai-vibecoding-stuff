# US-003: Atualizar SKILL.md com Seção Abstract Base Class

> Criada em: 2026-03-09
> Complexidade: Baixa
> Dependências: US-001

## Narrativa

**Como** usuário da skill `spring-data-jdbc`,
**Quero** encontrar documentação clara sobre `AbstractElSqlRepository` no SKILL.md,
**Para** entender rapidamente quando e como usar a classe base em meus projetos.

## Contexto Técnico

O arquivo `SKILL.md` é a documentação principal da skill.
Atualmente ele documenta:
- Simple Repository
- @Query Repository
- ElSql Repository
- Custom Repository
- CQRS Query Service

Precisamos adicionar uma seção sobre `AbstractElSqlRepository` como uma alternativa simplificada para Custom Repository.

## Critérios de Aceite

- [ ] **CA-001**: Nova seção "Abstract Base Class" adicionada após "Custom Repository"
- [ ] **CA-002**: Seção explica quando usar a classe base (para custom repositories com ElSql)
- [ ] **CA-003**: Exemplo de código mostrando herança de `AbstractElSqlRepository`
- [ ] **CA-004**: Exemplo de código mostrando comparação antes/depois (manual vs classe base)
- [ ] **CA-005**: Link para asset `assets/abstract-elsql-repository.java`
- [ ] **CA-006**: Link para asset de teste `assets/abstract-elsql-repository-test.java`
- [ ] **CA-007**: Convenção de localização de arquivos documentada
- [ ] **CA-008**: Seção "Available Assets" atualizada com novos assets

## Casos de Borda

- [ ] **CB-001**: Documentar que JdbcTemplate não está disponível (apenas NamedParameterJdbcTemplate)
- [ ] **CB-002**: Documentar que dialeto é POSTGRES fixo
- [ ] **CB-003**: Documentar exception quando arquivo não encontrado

## Critérios de Teste

- Verificar que todos os links funcionam
- Verificar que exemplos de código estão formatados corretamente
- Verificar que seção está no local correto (após Custom Repository)

## Notas de Implementação

- Localização: `.claude/skills/spring-data-jdbc/SKILL.md`
- Usar formatação Markdown consistente com o restante do arquivo
- Incluir exemplos de código antes/depois para mostrar benefício
- Manter tom e estilo da documentação existente

## Definição de Pronto

- [ ] Todos os critérios de aceite atendidos
- [ ] SKILL.md atualizado
- [ ] Exemplos de código claros e funcionais
- [ ] Links para assets corretos
- [ ] Seção integrada organicamente com o restante da documentação
