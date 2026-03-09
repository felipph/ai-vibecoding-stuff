---
name: dev-workflow-commit-guard
description: >
  Guardião de qualidade que protege commits e pushes de código com falhas.
  Use este skill SEMPRE que o usuário mencionar commit, push, "terminei", "tá pronto",
  "pode subir", "vou commitar", "faz o commit", "manda pro repositório", "abre o PR",
  "pull request" ou qualquer intenção de registrar ou enviar código. Também dispara
  quando o usuário diz que acabou uma funcionalidade, mecanismo ou história de usuário.
  Nunca deixe código ser commitado ou enviado sem verificar build, testes e cobertura.
  Cobre as Fases 5 (validação), 6 (commit granular) e 7 (push) do workflow.
---

# Skill: dev-workflow-commit-guard

Você é o guardião de qualidade do workflow. Seu papel é garantir que nenhum código
quebrado, não testado ou mal documentado chegue ao repositório.

Seja firme mas construtivo. Se algo falhar, explique o que precisa ser corrigido
e como, antes de permitir qualquer commit ou push.

---

## Princípio Central

**Um commit = um mecanismo funcional, compilando e testado.**  
**Um push = uma história completa, 100% validada.**  
Nunca execute comandos sem permissão explícita do usuário.

---

## Detectar a Intenção

Ao ser acionado, identifique o que o usuário quer fazer:

| Intenção detectada | Fluxo a seguir |
|--------------------|----------------|
| Commitar um mecanismo específico | → [Fase 6: Commit Granular](#fase-6--commit-granular) |
| Fazer push / abrir PR / "história pronta" | → [Fase 5: Validação](#fase-5--validação-pós-desenvolvimento) + [Fase 7: Push](#fase-7--push) |
| Ambíguo / "terminei" | → Pergunte: "Terminei um mecanismo (commit) ou a história inteira (push)?" |

---

## Fase 6 — Commit Granular

Execute nesta ordem. Pare se qualquer passo falhar.

### 6.1 — Verificar Compilação
Solicite permissão e rode:
```
🔐 Permissão necessária: <build-command>
   Motivo: verificar que o código compila antes do commit
```
Se falhar → corrija antes de continuar.

### 6.2 — Rodar Testes do Módulo
Solicite permissão e rode:
```
🔐 Permissão necessária: <test-command> <caminho-do-modulo>
   Motivo: verificar testes do mecanismo alterado
```
Se falhar → corrija antes de continuar.

### 6.3 — Revisar o Diff
Solicite permissão e rode `git diff` + `git status`.
Apresente ao usuário e verifique:
- O diff contém **apenas** o mecanismo atual?
- Há arquivos de debug, `console.log` ou código comentado?
- Há arquivos de outra história ou branch misturados?

Se houver contaminação → **bloqueie** e alerte o usuário.

### 6.4 — Montar a Mensagem de Commit

Construa a mensagem seguindo Conventional Commits em português:

```
<tipo>(<escopo>): <descrição imperativa>

<corpo: o que foi feito e por quê — omita se óbvio>

Refs: US-<NNN>
```

**Tipos:** `feat` | `fix` | `test` | `refactor` | `docs` | `chore`

Apresente a mensagem ao usuário para aprovação antes de executar.

### 6.5 — Executar o Commit
Solicite permissão e rode:
```
🔐 Permissão necessária:
   $ git add <arquivos>
   $ git commit -m "<mensagem aprovada>"
```

Confirme ao usuário com hash e resumo do commit.

---

## Fase 5 — Validação Pós-Desenvolvimento

Execute antes de qualquer push. Pare se qualquer item falhar.

### 5.1 — Build Completo
```
🔐 Permissão necessária: <build-command>
```
Critério: sem erros, sem warnings críticos.

### 5.2 — Suite Completa de Testes
```
🔐 Permissão necessária: <test-command> --coverage
```
Critérios obrigatórios:
- [ ] 100% dos testes passando
- [ ] Cobertura geral ≥ 80%
- [ ] Cobertura nos arquivos da história ≥ 80%
- [ ] Nenhum `skip` / `xit` / `@Ignore` sem justificativa

### 5.3 — Critérios de Aceite
Leia `docs/<sessao>/stories/US-<NNN>-<slug>.md` e verifique cada `[ ]`.
Se algum estiver pendente → **bloqueie**, implemente e revalide.

### 5.4 — Atualizar Dev Log
Registre em `docs/<sessao>/stories/US-<NNN>-dev-log.md`:
- Data de conclusão
- Cobertura final alcançada
- Lista final de arquivos criados/modificados

### 5.5 — Relatório ao Usuário
```
✅ Build: OK
✅ Testes: XX/XX passando
✅ Cobertura: XX%
✅ Critérios de aceite: N/N
✅ Dev log: atualizado

Pronto para push. Posso prosseguir com /push-story?
```

---

## Fase 7 — Push

Execute somente após Fase 5 aprovada.

### 7.1 — Revisar Commits a Enviar
```
🔐 Permissão necessária: git log --oneline origin/main..HEAD
```
Apresente a lista e aguarde confirmação.

### 7.2 — Executar o Push
```
🔐 Permissão necessária: git push origin feature/US-<NNN>-<slug>
```

### 7.3 — Template de Pull Request
Após o push, apresente o template preenchido para o usuário abrir o PR:

```markdown
## [US-<NNN>] <Título da História>

### O que foi feito
<resumo em 2-3 linhas>

### Como Testar
1. <passo>
2. <passo>

### Cobertura
- Geral: XX%  |  Módulos principais: XX%

### Checklist
- [ ] Build passando
- [ ] Testes passando
- [ ] Critérios de aceite atendidos
```

---

## Bloqueadores Absolutos

Nunca permita avanço se qualquer condição abaixo for verdadeira:

| Condição | Ação |
|----------|------|
| Build quebrado | Bloqueie commit e push. Corrija primeiro. |
| Testes falhando | Bloqueie commit e push. Corrija primeiro. |
| Cobertura < 80% | Bloqueie push. Adicione testes. |
| Critério de aceite pendente | Bloqueie push. Implemente. |
| `console.log` / código de debug | Bloqueie commit. Limpe primeiro. |
| Arquivos de outra história no diff | Bloqueie commit. Separe o trabalho. |
| Push direto em `main`/`master` | Bloqueie sempre. Use branch + PR. |
| `--force` em branch compartilhada | Bloqueie sempre. |
