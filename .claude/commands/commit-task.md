# /commit-task — Commit de um Mecanismo

Executa a Fase 6: registra um mecanismo funcional com commit granular e rastreável.

## Como usar
```
/commit-task <US-NNN> "<descrição do mecanismo>"
```

**Execute após cada mecanismo autocontido estar implementado e testado.**

## O que este comando faz

### 6.1 — Verificar Compilação
> 🔐 Solicite permissão antes de executar

```bash
<build-command>
```

Se falhar → corrija antes de continuar. Não commite código que não compila.

---

### 6.2 — Rodar Testes do Módulo
> 🔐 Solicite permissão antes de executar

```bash
<test-command> <caminho-do-modulo-alterado>
```

Se falhar → corrija antes de continuar. Não commite com testes falhando.

---

### 6.3 — Revisar o Diff
> 🔐 Solicite permissão antes de executar

```bash
git diff
git status
```

Verifique e apresente ao usuário:
- Lista de arquivos alterados
- O diff contém **apenas** o mecanismo atual?
- Há arquivos indesejados (debug, logs, configs locais)?

Se houver arquivos de outra história → não commite. Alerte o usuário.

---

### 6.4 — Montar a Mensagem de Commit
Construa a mensagem seguindo Conventional Commits:

```
<tipo>(<escopo>): <descrição imperativa em português>

<corpo: o que foi feito e por quê — omita se óbvio>

Refs: US-<NNN>
```

**Tipos:**
| Tipo | Quando |
|------|--------|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `test` | Adição/ajuste de testes |
| `refactor` | Refatoração sem mudança de comportamento |
| `docs` | Somente documentação |
| `chore` | Build, configs, dependências |

**Exemplo:**
```
feat(pagamento): implementa cálculo de juros compostos

Adiciona serviço PagamentoService com suporte a juros simples e compostos.
Cobre os casos de prazo zero e taxa negativa como erros de validação.

Refs: US-003
```

Apresente a mensagem ao usuário para aprovação antes de commitar.

---

### 6.5 — Executar o Commit
> 🔐 Solicite permissão antes de executar

```bash
git add <arquivos-do-mecanismo>
git commit -m "<mensagem aprovada>"
```

---

### 6.6 — Confirmar ao Usuário
```
✅ Commit realizado: <hash curto>
   <tipo>(<escopo>): <descrição>
   Arquivos: N arquivo(s)
```

## Saída esperada
- Build passou ✅
- Testes do módulo passando ✅
- Apenas arquivos do mecanismo no commit ✅
- Mensagem descritiva e aprovada ✅
- Commit realizado ✅

## ⚠️ Bloqueadores
- Nunca commite com build quebrado
- Nunca commite com testes falhando
- Nunca commite arquivos de outra história ou branch
- Nunca commite `console.log`, código comentado ou debug temporário
