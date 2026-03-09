# Dúvidas — melhorar-spring-data-jdbc-base
> Criado em: 2026-03-09

## Dúvidas de Negócio
- [x] **Nenhum item** — Esta é uma melhoria técnica interna da skill

## Dúvidas Técnicas

### Carregamento do Bundle ElSql
- [x] Qual estratégia de fallback quando o arquivo .elsql não existe? Exception ou null?
  **Resolvido**: Exception com mensagem clara (`IllegalArgumentException`).
  **Justificativa**: Fail-fast é melhor para desenvolvimento. Se o arquivo .elsql não existe, é um erro de configuração que deve ser descoberto imediatamente, não silenciosamente em runtime.

- [x] Devemos suportar Sobrescrita do caminho do bundle via construtor? (para casos fora da convenção)
  **Resolvido**: Não.
  **Justificativa**: Manter simples. Convenção over configuration. Se alguém precisar de comportamento diferente, pode não usar a classe base e criar sua própria implementação manual (que já é suportada pela skill).

### JdbcTemplate vs NamedParameterJdbcTemplate
- [x] Devemos expor AMBOS templates ou apenas NamedParameterJdbcTemplate?
  **Resolvido**: Apenas `NamedParameterJdbcTemplate`.
  **Justificativa**: ElSql usa named parameters (`:nome`), então `JdbcTemplate` não faz sentido. `NamedParameterJdbcTemplate` pode fazer tudo que `JdbcTemplate` faz e mais.

- [x] JdbcTemplate é necessário para algum caso de uso comum?
  **Resolvido**: Não.
  **Justificativa**: NamedParameterJdbcTemplate é suficiente e mais adequado para ElSql. Se alguém precisar de JdbcTemplate puro, provavelmente não deveria usar ElSql.

### Nomenclatura da Classe Base
- [x] Qual o melhor nome? `AbstractElSqlRepository`, `BaseElSqlRepository`, `ElSqlRepositorySupport`?
  **Resolvido**: `AbstractElSqlRepository`.
  **Justificativa**: Segue padrão de nomenclatura do Spring Framework (ex: `AbstractTransactionalTestNGListener`, `AbstractJUnit4SpringContextTests`, `AbstractRoutingDataSource`). É claro e convencional.

### Integração com Documentação Existente
- [x] Devemos atualizar `references/custom-repositories.md` para usar a nova classe base como padrão?
  **Resolvido**: Sim.
  **Justificativa**: A classe base será a abordagem recomendada para novos projetos. Ela simplifica significativamente o código.

- [x] Mantemos os exemplos antigos ou substituímos completamente?
  **Resolvido**: Substituir completamente.
  **Justificativa**: Manter dois padrões paralelos gera confusão. A classe base é superior em todos os aspectos (menos código, mais limpo, menos propenso a erros). Quem já tem código antigo pode continuar usando, mas a documentação deve recomendar o novo padrão.

### Testes
- [x] Devemos incluir um asset de teste para a classe base?
  **Resolvido**: Sim, mas de forma simplificada.
  **Justificativa**: Um exemplo básico de como testar um repository que estende `AbstractElSqlRepository`. Mostra como mockar o `DataSource` e carregar o bundle ElSql em ambiente de teste.

## Ambiguidades
- [x] Nenhuma ambiguidade identificada no momento
