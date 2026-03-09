# /validate — Validação Pós-Desenvolvimento

Executa a Fase 5: garante que a história está completa e estável antes do push.

## Como usar
```
/validate <nome-da-sessao> <US-NNN>
```

## O que este comando faz

Execute cada passo **na ordem**. Pare se qualquer verificação falhar.

---

### 5.1 — Build Completo
> 🔐 Solicite permissão antes de executar

```bash
<build-command>
```

**Critério:** Build limpo, sem erros e sem warnings críticos.

Se falhar → corrija, faça `/commit-task` da correção e reinicie `/validate`.

---

### 5.2 — Suite Completa de Testes
> 🔐 Solicite permissão antes de executar

```bash
<test-command> --coverage
```

**Critérios:**
- [ ] 100% dos testes passando
- [ ] Cobertura geral ≥ 80%
- [ ] Cobertura nos arquivos da história ≥ 80%
- [ ] Nenhum teste com `skip`/`xit`/`@Ignore` sem justificativa documentada

Se falhar → corrija, faça `/commit-task` e reinicie `/validate`.

---

### 5.3 — Revisar Critérios de Aceite
Abra `docs/<sessao>/stories/US-<NNN>-<slug>.md` e verifique item a item:

```
- [x] AC1: ...   ← marque como concluído
- [x] AC2: ...
- [ ] AC3: ...   ← se pendente, implemente antes de continuar
```

Atualize o arquivo da história com os checkboxes marcados.

---

### 5.4 — Finalizar o Dev Log
Atualize `docs/<sessao>/stories/US-<NNN>-dev-log.md`:

```markdown
## Conclusão
- Data de conclusão: <data>
- Cobertura final: XX%
- Todos os critérios de aceite: ✅

## Arquivos Criados/Modificados (lista final)
- `src/...`
- `test/...`

## Observações para Code Review
<pontos de atenção, trade-offs, débito técnico>
```

---

### 5.5 — Atualizar INDEX.md
Em `docs/<sessao>/stories/INDEX.md`:
- Marque a história: `[x] Concluída`
- Registre a branch: `feature/US-<NNN>-<slug>`

---

### 5.6 — Relatório Final ao Usuário
Apresente:
```
✅ Build: OK
✅ Testes: XX/XX passando
✅ Cobertura: XX%
✅ Critérios de aceite: N/N atendidos
✅ Dev log: finalizado

História US-<NNN> pronta para push.
Executar `/push-story <sessao> US-<NNN>`?
```

## Saída esperada
- Build limpo ✅
- Todos os testes passando ✅
- Cobertura ≥ 80% ✅
- Critérios de aceite 100% ✅
- Dev log finalizado ✅
- INDEX.md atualizado ✅
