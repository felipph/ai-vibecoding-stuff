# Pontos-Chave — melhorar-spring-data-jdbc-base
> Atualizado na Fase 2 — 2026-03-09
> Criado em: 2026-03-09

## Funcionalidades Identificadas

- **Classe abstrata base para repositories ElSql**: Eliminar código repetido em custom repositories
- **Carregamento automático de bundles ElSql**: Baseado na estrutura de pacotes do repository
- **Métodos utilitários simplificados**:
  - `getSql(String nome)` → retorna SQL estático sem parâmetros
  - `getSqlDinamico(String nome, MapSqlParameterSource params)` → retorna SQL com tags ElSql avaliadas
- **NamedParameterJdbcTemplate**: Criado automaticamente pela classe base (apenas este, sem JdbcTemplate)
- **Convenção de localização de arquivos**: `.elsql` no resources seguindo a estrutura de pacotes do Java
- **Validação de arquivo .elsql**: Lançar `IllegalArgumentException` com mensagem clara se arquivo não encontrado

## Entidades / Domínios

- **AbstractElSqlRepository**: Classe base abstrata (nome definido na Fase 2)
- **Custom Repository Implementations**: Classes que estendem a classe base (ex: `ProdutoRepositoryImpl`, `OrderRepositoryImpl`)
- **ElSqlBundle**: Bundle externo do ElSql que será carregado
- **NamedParameterJdbcTemplate**: Template JDBC do Spring (apenas este, sem JdbcTemplate)
- **Arquivos .elsql**: Recursos no classpath seguindo convenção de pacotes

## Integrações Necessárias

- **ElSql library**: Já existente na skill (`com.opengamma.elsql`)
- **Spring Data JDBC**: Já existente na skill
- **Spring Core**: Para carregamento de recursos do classpath (`ClassPathResource`)
- **SKILL.md**: Documentação da skill `spring-data-jdbc` a ser atualizada
- **assets/**: Criar novo asset com a classe base abstrata
- **references/**: Possivelmente atualizar `custom-repositories.md` com exemplos usando a classe base

## Riscos e Restrições

- **Convenção vs Configuração**: Se o arquivo .elsql não seguir a convenção de nomes, exceção será lançada com mensagem clara (mitigado na Fase 2)
- **Múltiplos bundles por repository**: A classe base assume um bundle por classe (um arquivo .elsql por repository)
- **Sem suporte a dialetos customizáveis**: A classe base usa sempre POSTGRES (decisão da Fase 2: manter simples)
- **Testabilidade**: A classe base deve ser facilmente testável/mockável (será abordado com asset de teste)
- **Substituição de exemplos**: A documentação será atualizada para usar a classe base como padrão (decisão da Fase 2)

## Fora do Escopo

- ❌ Modificação da funcionalidade existente da skill (apenas adição)
- ❌ Suporte a dialetos customizáveis via sobrescrita de método
- ❌ Exposição de JdbcTemplate (apenas NamedParameterJdbcTemplate)
- ❌ Mudança na sintaxe ElSql
- ❌ Alteração na configuração de beans ElSql existente
- ❌ Implementação de CQRS (isso já está coberto em outros assets)

## Adições do Refinamento (Fase 2)

**Decisões tomadas:**
- Nome da classe: `AbstractElSqlRepository` (segue padrão Spring Framework)
- Apenas `NamedParameterJdbcTemplate` exposto (removido `JdbcTemplate`)
- Verificação explícita de arquivo .elsql no construtor com `IllegalArgumentException`
- Substituir completamente exemplos em `custom-repositories.md`
- Adicionar asset de teste para a classe base
- Sem suporte a dialetos customizáveis (POSTGRES fixo)
- Sem fallback silencioso (fail-fast com exception clara)
