# /push-story — Push de História Completa

Executa a Fase 7: integra a história ao repositório remoto após validação completa.

## Como usar
```
/push-story <nome-da-sessao> <US-NNN>
```

**Pré-requisito:** `/validate` deve ter sido executado e aprovado.

## O que este comando faz

### 7.1 — Verificar Pré-requisitos
Confirme com o usuário que todos os itens abaixo estão ✅:

```
Pré-requisitos para push de US-<NNN>:
[ ] /validate executado e aprovado
[ ] Build completo passando
[ ] 100% dos testes passando
[ ] Cobertura ≥ 80%
[ ] Todos os critérios de aceite atendidos
[ ] Dev log finalizado
[ ] INDEX.md atualizado com história concluída
```

Se qualquer item estiver pendente → bloqueie o push e informe o usuário.

---

### 7.2 — Validação Final Antes do Push
> 🔐 Solicite permissão antes de executar

```bash
<build-command> && <test-command> --coverage
```

Se qualquer verificação falhar → bloqueie o push.

---

### 7.3 — Revisar Commits a Enviar
> 🔐 Solicite permissão antes de executar

```bash
git log --oneline origin/main..HEAD
```

Apresente ao usuário a lista de commits que serão enviados para aprovação.

---

### 7.4 — Executar o Push
> 🔐 Solicite permissão antes de executar

```bash
git push origin feature/US-<NNN>-<slug>
```

---

### 7.5 — Sugerir Pull Request
Apresente o template de PR para o usuário copiar:

```markdown
## Título
[US-<NNN>] <Título da História>

## O que foi feito
<resumo da implementação em 2-3 linhas>

## Histórias Atendidas
- US-<NNN>: <título>

## Como Testar
1. <passo 1>
2. <passo 2>

## Cobertura de Testes
- Cobertura geral: XX%
- Módulos principais: XX%

## Checklist
- [ ] Build passando
- [ ] Testes passando
- [ ] Critérios de aceite atendidos
- [ ] Documentação atualizada
```

---

### 7.6 — Registrar no Dev Log
Atualize `docs/<sessao>/stories/US-<NNN>-dev-log.md`:
```markdown
## Push
- Data: <data>
- Branch: `feature/US-<NNN>-<slug>`
- PR: <link ou "aberto manualmente">
```

---

### 7.7 — Confirmar ao Usuário
```
🚀 Push realizado com sucesso!
   Branch: feature/US-<NNN>-<slug>
   Commits enviados: N
   
Próxima história sugerida: US-<NNN+1> — <título>
Execute: /dev <sessao> US-<NNN+1>
```

## ⚠️ Bloqueadores Absolutos
- Nunca faça push com testes falhando
- Nunca faça push direto na `main`/`master`
- Nunca use `--force` em branches compartilhadas
- Uma história = um PR (não misture histórias no mesmo push)
