# US-002: Criar Asset de Teste para AbstractElSqlRepository

> Criada em: 2026-03-09
> Complexidade: Baixa
> Dependências: US-001

## Narrativa

**Como** desenvolvedor usando `AbstractElSqlRepository`,
**Quero** um exemplo de teste demonstrando como testar repositories que estendem essa classe base,
**Para** entender rapidamente como mockar o `DataSource` e carregar o bundle ElSql em ambiente de teste.

## Contexto Técnico

A classe base `AbstractElSqlRepository` requer um `DataSource` no construtor.
Em testes unitários, precisamos:
1. Mockar ou fornecer um `DataSource` de teste
2. Garantir que o arquivo `.elsql` está no classpath do teste
3. Validar que o bundle é carregado corretamente

Esta história cria um asset de teste reutilizável que demonstra:
- Como mockar `DataSource` com Mockito
- Como estruturar testes para repositories que estendem a classe base
- Como usar `@JdbcTest` ou `DataJdbcTest` com Testcontainers

## Critérios de Aceite

- [ ] **CA-001**: Asset de teste criado em `assets/abstract-elsql-repository-test.java`
- [ ] **CA-002**: Teste demonstra mock de `DataSource` com Mockito
- [ ] **CA-003**: Teste demonstra verificação de que `ElSqlBundle` foi carregado
- [ ] **CA-004**: Teste demonstra execução de query usando `namedJdbc`
- [ ] **CA-005**: Teste demonstra exception quando arquivo `.elsql` não existe
- [ ] **CA-006**: Comentários explicativos em cada parte do teste
- [ ] **CA-007**: Arquivo `.elsql` de exemplo incluído no teste

## Casos de Borda

- [ ] **CB-001**: Teste com `DataSource` real via H2 memória
- [ ] **CB-002**: Teste com `DataSource` mockado
- [ ] **CB-003**: Teste de exception quando arquivo não encontrado

## Critérios de Teste

- **Cobertura mínima**: 80% (do próprio teste de exemplo)
- Teste deve ser funcional e executável como exemplo

## Notas de Implementação

- Localização: `.claude/skills/spring-data-jdbc/assets/`
- Usar `@ExtendWith(MockitoExtension.class)` para mockar `DataSource`
- Incluir arquivo `.elsql` de exemplo em `src/test/resources/`
- Mostrar tanto mock quanto uso com Testcontainers/H2

## Definição de Pronto

- [ ] Todos os critérios de aceite atendidos
- [ ] Teste funcional e executável
- [ ] Comentários claros explicando cada passo
- [ ] Asset criado em `.claude/skills/spring-data-jdbc/assets/`
- [ ] Exemplo cobre cenários principais (mock, exception, query)
