# /status — Status da Sessão Atual

Exibe o estado completo de uma sessão de trabalho.

## Como usar
```
/status <nome-da-sessao>
```

## O que este comando faz

### 1. Ler Todos os Artefatos da Sessão
Carregue e analise:
- `docs/<sessao>/key-points.md`
- `docs/<sessao>/questions.md`
- `docs/<sessao>/plan.md`
- `docs/<sessao>/stories/INDEX.md`
- Todos os `US-NNN-dev-log.md` existentes

### 2. Apresentar Relatório de Status

```
📁 Sessão: <nome-da-sessao>

── Planejamento ──────────────────────────
  key-points.md  : ✅ / ❌ não encontrado
  questions.md   : ✅ (N dúvidas, N resolvidas) / ❌
  plan.md        : ✅ (vX) / ❌

── Histórias ─────────────────────────────
  Total          : N histórias
  Backlog        : N
  Em desenvolvimento : N (US-NNN na branch feature/...)
  Concluídas     : N

  | ID      | Título    | Status      | Cobertura |
  |---------|-----------|-------------|-----------|
  | US-001  | ...       | ✅ Concluída | 84%       |
  | US-002  | ...       | 🔧 Em dev   | —         |
  | US-003  | ...       | 📋 Backlog  | —         |

── Próxima Ação Sugerida ─────────────────
  <baseado no estado atual, sugira o próximo comando>
  Ex: /dev <sessao> US-002
      /validate <sessao> US-002
      /push-story <sessao> US-002
```

### 3. Alertas
Se houver dúvidas não resolvidas em `questions.md`:
```
⚠️  N dúvida(s) não resolvida(s) em questions.md
    Execute /refine <sessao> para resolvê-las
```

Se houver história em desenvolvimento sem dev log:
```
⚠️  US-NNN não tem dev-log.md — crie antes de continuar
```
