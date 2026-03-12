# /dev — Iniciar Desenvolvimento de uma História

Executa a Fase 4: implementa uma história de usuário de forma incremental.
O usuário pode habilitar o modo autônomo para que o assistente tome decisões sem pedir permissão. O comando chave é "go god mode".

Caso você esteja com problemas para resolver um problema e demore mais do que 3 interações tentando resolver, chame o usuário para ajudar.


## Como usar
```
/dev <nome-da-sessao> [US-NNN]
```
Se `US-NNN` não for informado, sugira a melhor história para iniciar.

## O que este comando faz

### 1. Selecionar a História
Se nenhuma história foi especificada, leia `docs/<sessao>/stories/INDEX.md` e sugira
a mais adequada com base em:
- Menor número de dependências bloqueantes
- Maior valor de fundação para as demais histórias
- Menor complexidade (para ganhar tração)

**Apresente a sugestão e aguarde confirmação do usuário.**

### 2. Criar Branch de Trabalho
> 🔐 Solicite permissão antes de executar a menos que o usuário explicitamente diga que não precisa!

```bash
git checkout -b feature/US-<NNN>-<slug>
```

### 3. Criar Dev Log
Grave `docs/<sessao>/stories/US-<NNN>-dev-log.md`:
```markdown
# Dev Log — US-<NNN> <Título>

> Branch: `feature/US-<NNN>-<slug>`
> Início: <data>
> Status: Em desenvolvimento

## Abordagem de Implementação
<estratégia técnica escolhida>

## Arquivos a Criar/Modificar (estimativa)
- `src/...`
- `test/...`
```

### 4. Ciclo de Desenvolvimento por Mecanismo

Para cada mecanismo autocontido (classe, módulo, serviço, endpoint):

#### 4a. Implementar o Código de Produção
- Escreva o código seguindo os padrões do projeto
- Mantenha o escopo restrito ao mecanismo atual

#### 4b. Verificar Compilação

```bash
<build-command>
```
- Se falhar: corrija antes de continuar

#### 4c. Escrever Testes Unitários
- Implemente testes para o mecanismo
- Cubra casos de borda listados na história
- Meta: ≥ 80% de cobertura no módulo

#### 4d. Rodar Testes do Módulo

```bash
<test-command> <caminho-do-modulo> --coverage
```
- Se falhar: corrija antes de continuar

#### 4e. Registrar no Dev Log
Adicione ao `US-<NNN>-dev-log.md`:
```markdown
### Mecanismo N — <Nome>
- **O que foi feito**: ...
- **Decisões técnicas**: ...
- **Cobertura**: XX%
```

#### 4f. Executar `/commit-task` para este mecanismo

Repita o ciclo para o próximo mecanismo.

### 5. Ao Concluir Todos os Mecanismos
- Verifique os critérios de aceite da história
- Execute `/validate` para a validação final
- Se tudo passar, execute `/push-story`

### 6. Atualize o INDEX

Sempre atualize o INDEX para manter as tarefas atualizadas!

## Saída esperada por ciclo
- Código implementado ✅
- Build passando ✅
- Testes passando com ≥ 80% ✅
- Dev log atualizado ✅
- Commit realizado via `/commit-task` ✅
- INDEX atualizado
