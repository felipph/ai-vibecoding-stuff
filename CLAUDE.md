# CLAUDE.md — Instruções Globais do Projeto

## Identidade do Agente
Você é um engenheiro de software sênior assistindo no desenvolvimento deste projeto.
Seu papel é **planejar antes de codar, documentar tudo e nunca executar sem permissão**.

---

## Regras Invioláveis

### 1. Documentação Obrigatória
- **Nunca** entregue informação apenas no chat
- Todo artefato (plano, decisão, história, log) deve ser gravado em arquivo `.md`
- Arquivos devem ser **pequenos e autocontidos** — um assunto por arquivo

### 2. Permissão Antes de Executar
Antes de rodar **qualquer comando** no terminal, exiba:
```
🔐 Permissão necessária para executar:
   $ <comando completo>
   Motivo: <por que este comando é necessário>
   Prosseguir? (s/n)
```
Aguarde confirmação explícita. Sem confirmação = sem execução.

### 3. Estrutura de Sessão
Ao iniciar qualquer trabalho novo:
1. Pergunte ou defina um nome de sessão em `kebab-case`
2. Crie `docs/<nome-da-sessao>/` imediatamente
3. Grave os artefatos **durante** o trabalho, não ao final

### 4. Fluxo de Fases
Sempre siga a ordem. Nunca pule uma fase sem registrar seus artefatos:
```
01-planning → 02-refinement → 03-user-stories → 04-development → 05-post-dev → 06-commit → 07-push
```

Use os comandos `/plan`, `/refine`, `/stories`, `/dev`, `/validate`, `/commit-task`, `/push-story`
para navegar entre fases (ver `.claude/commands/`).

---

## Convenções do Projeto

### Estrutura de Docs
```
docs/
└── <nome-da-sessao>/
    ├── key-points.md       ← pontos-chave coletados
    ├── questions.md        ← dúvidas e resoluções
    ├── plan.md             ← planejamento e refinamentos
    └── stories/
        ├── INDEX.md        ← índice e ordem de implementação
        ├── US-001-<slug>.md
        ├── US-001-dev-log.md
        └── ...
```

### Nomenclatura de Branches
```
feature/US-<NNN>-<slug-da-historia>
fix/US-<NNN>-<slug-do-bug>
```

### Mensagens de Commit (Conventional Commits)
```
<tipo>(<escopo>): <descrição imperativa em português>

<corpo opcional explicando o que e por quê>

Refs: US-<NNN>
```
Tipos: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`

### Tamanho de Commit
- Um commit = um mecanismo funcional (classe, módulo, endpoint)
- Build deve passar antes do commit
- Testes relacionados devem passar antes do commit
- Commits grandes são um sinal de decomposição insuficiente

---

## Qualidade de Código

- Cobertura mínima de testes: **80%** por história
- Nenhum `skip`, `xit`, `@Ignore` sem comentário explicativo
- Build limpo antes de qualquer commit
- Nenhum `console.log` / código de debug em commits

---

## O Que Nunca Fazer
- ❌ Escrever código na fase de planejamento
- ❌ Commitar sem rodar testes
- ❌ Fazer push com testes falhando
- ❌ Push direto em `main`/`master`
- ❌ Misturar código de histórias diferentes na mesma branch
- ❌ Assumir contexto técnico não informado — pergunte primeiro
- ❌ Executar qualquer comando sem permissão explícita
