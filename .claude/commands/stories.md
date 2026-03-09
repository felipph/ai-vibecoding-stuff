# /stories — Gerar Histórias de Usuário

Executa a Fase 3: converte o plano refinado em histórias de usuário detalhadas.

## Como usar
```
/stories <nome-da-sessao>
```

## O que este comando faz

### 1. Carregar Artefatos Refinados
Leia obrigatoriamente:
- `docs/<sessao>/key-points.md`
- `docs/<sessao>/questions.md`
- `docs/<sessao>/plan.md`

### 2. Decompor em Histórias
Quebre o plano seguindo o princípio **INVEST**:
- **I**ndependente: pode ser desenvolvida sem outra estar completa
- **N**egociável: escopo pode ser ajustado
- **V**aliosa: entrega valor por si só
- **E**stimável: complexidade pode ser estimada
- **S**mall: desenvolvível em 1-2 dias
- **T**estável: critérios de aceite verificáveis

Regras de decomposição:
- Uma história = uma funcionalidade coesa
- Histórias não devem ter dependências circulares
- Se uma história parecer grande, quebre-a

### 3. Criar Pasta de Histórias
```
docs/<sessao>/stories/
```

### 4. Gravar Cada História em Arquivo Separado
**Nomenclatura:** `docs/<sessao>/stories/US-<NNN>-<slug>.md`

Sequência numérica: `001`, `002`, `003`...

Conteúdo de cada arquivo:
```markdown
# US-<NNN> — <Título>

> Status: Backlog
> Branch: `feature/US-<NNN>-<slug>`
> Criada em: <data>

## Narrativa
Como <tipo de usuário>,
quero <ação ou funcionalidade>,
para <benefício ou objetivo>.

## Contexto Técnico
<módulos impactados, padrões a seguir, integrações>

## Critérios de Aceite
- [ ] AC1: ...
- [ ] AC2: ...

## Requisitos de Teste
- Cobertura mínima: **80%**
- Casos de borda a cobrir:
  - ...

## Dependências
| História | Tipo |
|----------|------|
| US-XXX   | Deve ser concluída antes |

## Estimativa
| Métrica | Valor |
|---------|-------|
| Complexidade | Baixa / Média / Alta |
| Estimativa   | X horas |
```

### 5. Gravar `docs/<sessao>/stories/INDEX.md`
```markdown
# Índice de Histórias — <Nome da Sessão>

## Tabela
| ID | Título | Complexidade | Dependências | Status |
|----|--------|--------------|--------------|--------|
| US-001 | ... | Baixa | — | Backlog |

## Ordem de Implementação Sugerida
1. US-001 — <motivo>
2. US-002 — <motivo>

## Diagrama de Dependências
US-001 → US-002 → US-003
         ↓
         US-004
```

### 6. Solicitar Aprovação
- Apresente o índice ao usuário
- Aguarde aprovação das histórias antes de prosseguir para `/dev`

## Saída esperada
- `docs/<sessao>/stories/US-NNN-*.md` (um por história) ✅
- `docs/<sessao>/stories/INDEX.md` ✅
- Aprovação do usuário ✅
