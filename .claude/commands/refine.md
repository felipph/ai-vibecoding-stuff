# /refine — Refinar Planejamento

Executa a Fase 2: resolve dúvidas, valida premissas e melhora o plano existente.

## Como usar
```
/refine <nome-da-sessao>
```

## O que este comando faz

### 1. Carregar Artefatos
Leia obrigatoriamente:
- `docs/<sessao>/key-points.md`
- `docs/<sessao>/questions.md`
- `docs/<sessao>/plan.md`

Se algum arquivo não existir, avise e instrua a rodar `/plan` primeiro.

### 2. Resolver Dúvidas de `questions.md`
Para cada item não resolvido `[ ]`:

**Se puder inferir com segurança:**
- Resolva e documente a inferência e sua justificativa

**Se precisar do usuário:**
- Agrupe **todas** as perguntas necessárias
- Faça em uma **única rodada** ao usuário
- Aguarde as respostas antes de continuar

### 3. Analisar o `plan.md` e Sugerir Melhorias
Avalie criticamente e identifique:
- Lacunas no escopo (o que foi esquecido?)
- Riscos técnicos não mapeados
- Oportunidades de simplificação
- Ordenação de implementação que pode ser otimizada
- Casos de borda não considerados

### 4. Atualizar os Arquivos Existentes
> ⚠️ Atualize os arquivos existentes. Não crie arquivos novos.

**`docs/<sessao>/key-points.md`**
- Adicione uma seção `## Adições do Refinamento` com novos pontos descobertos
- Insira no topo: `> Atualizado na Fase 2 — <data>`

**`docs/<sessao>/questions.md`**
- Marque dúvidas resolvidas: `- [x] <dúvida> → **Resolução:** <resposta>`
- Insira no topo: `> Atualizado na Fase 2 — <data>`

**`docs/<sessao>/plan.md`**
- Adicione uma seção `## Refinamentos (v2)` com as melhorias
- Insira no topo: `> Versão 2 — atualizado em: <data>`

### 5. Solicitar Aprovação
Apresente um diff das mudanças ao usuário e pergunte:
- O plano refinado está aprovado?
- Pode prosseguir para `/stories`?

## Saída esperada
- `docs/<sessao>/key-points.md` atualizado ✅
- `docs/<sessao>/questions.md` com resoluções ✅
- `docs/<sessao>/plan.md` refinado ✅
- Aprovação do usuário ✅
