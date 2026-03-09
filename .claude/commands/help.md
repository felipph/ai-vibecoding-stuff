# /help — Referência de Comandos

Lista todos os comandos disponíveis do workflow de desenvolvimento assistido por IA.

## Como usar
```
/help
```

## O que este comando faz
Apresente a referência completa abaixo ao usuário.

---

## Comandos do Workflow

### Fase 1 — Planejamento
```
/plan <descrição>
```
Inicia uma nova sessão de trabalho. Cria a pasta `docs/<sessao>/` e grava
`key-points.md`, `questions.md` e `plan.md`.

---

### Fase 2 — Refinamento
```
/refine <nome-da-sessao>
```
Resolve dúvidas, valida premissas e atualiza os artefatos do planejamento.
Requer aprovação do usuário antes de prosseguir.

---

### Fase 3 — Histórias de Usuário
```
/stories <nome-da-sessao>
```
Converte o plano refinado em histórias de usuário. Grava um arquivo por história
em `docs/<sessao>/stories/` e cria o `INDEX.md`.

---

### Fase 4 — Desenvolvimento
```
/dev <nome-da-sessao> [US-NNN]
```
Inicia o desenvolvimento de uma história. Se `US-NNN` não for informado,
sugere a melhor história para começar. Cria branch e dev log.

---

### Fase 5 — Validação Pós-Dev
```
/validate <nome-da-sessao> <US-NNN>
```
Roda build completo, suite de testes e valida critérios de aceite antes do push.

---

### Fase 6 — Commit por Mecanismo
```
/commit-task <US-NNN> "<descrição>"
```
Commita um mecanismo autocontido após verificar build e testes do módulo.
Deve ser executado ao final de cada mecanismo, não ao final da história.

---

### Fase 7 — Push da História
```
/push-story <nome-da-sessao> <US-NNN>
```
Faz push da branch após validação completa e sugere template de Pull Request.

---

### Utilitários
```
/status <nome-da-sessao>
```
Exibe o estado atual da sessão: planejamento, histórias e próxima ação sugerida.

```
/help
```
Exibe este guia de referência.

---

## Fluxo Típico

```
/plan "implementar autenticação com JWT"
  ↓
/refine feature-auth-jwt
  ↓
/stories feature-auth-jwt
  ↓
/dev feature-auth-jwt US-001
  ↓ (ciclo por mecanismo)
/commit-task US-001 "middleware de validação JWT"
/commit-task US-001 "endpoint de refresh token"
  ↓
/validate feature-auth-jwt US-001
  ↓
/push-story feature-auth-jwt US-001
  ↓
/dev feature-auth-jwt US-002
```

---

## Regras Sempre Ativas
- 🔐 Nenhum comando é executado sem permissão explícita do usuário
- 📝 Todo artefato é gravado em arquivo, nunca apenas no chat
- 🚫 Push nunca ocorre com testes falhando ou build quebrado
