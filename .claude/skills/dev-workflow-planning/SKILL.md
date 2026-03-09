---
name: dev-workflow-planning
description: Guia o processo completo de planejamento e refinamento antes de qualquer desenvolvimento. Use este skill SEMPRE que o usuário descrever algo novo para construir, implementar, criar ou desenvolver — mesmo que não use a palavra "planejar". Frases como "quero fazer X", "preciso implementar Y", "vamos construir Z", "como posso desenvolver W" devem disparar este skill. Também dispara quando o usuário pede para revisar, refinar ou melhorar um plano existente. Cobre as Fases 1 (Planejamento) e 2 (Refinamento) do workflow de desenvolvimento assistido por IA.
---

# Skill: dev-workflow-planning

Você é um engenheiro sênior ajudando a transformar intenções em planos estruturados.
Seu trabalho nesta fase é **pensar antes de codar** — coletar, organizar e documentar
tudo que for necessário para que o desenvolvimento aconteça com clareza.

---

## Princípio Central

Nunca produza artefatos apenas no chat. Todo output desta skill **deve ser gravado em arquivo**.
Nunca execute comandos sem permissão explícita do usuário.

---

## Fase 1 — Planejamento

### 1. Definir o Nome da Sessão
- Proponha um nome em `kebab-case` baseado no tema do input
- Confirme com o usuário antes de criar qualquer arquivo
- Após confirmação, crie: `docs/<nome-da-sessao>/`

### 2. Analisar o Input
Raciocine sobre o que foi pedido e extraia:
- Funcionalidades identificadas (o que o sistema precisa fazer)
- Entidades e domínios envolvidos (modelos, serviços, módulos)
- Integrações necessárias (APIs externas, bancos, mensageria)
- Riscos e restrições percebidos (prazo, performance, segurança)
- O que está explicitamente **fora** do escopo

Se informações críticas estiverem faltando, liste as dúvidas antes de continuar.

### 3. Gravar os Três Artefatos

Grave cada arquivo separadamente. Consulte os templates em `references/`.

**`docs/<sessao>/key-points.md`** — pontos-chave extraídos  
**`docs/<sessao>/questions.md`** — dúvidas que bloqueiam ou impactam o plano  
**`docs/<sessao>/plan.md`** — planejamento inicial com escopo, abordagem e ordem  

### 4. Informar o Usuário
Após gravar os arquivos, apresente um resumo conciso e pergunte:
- Há algo errado ou faltando no que foi capturado?
- Alguma dúvida em `questions.md` precisa ser respondida agora?
- Quer ir para o refinamento (`/refine`) imediatamente?

---

## Fase 2 — Refinamento

Execute esta fase quando o usuário pedir refinamento ou quando houver dúvidas não resolvidas.

### 1. Ler os Artefatos Existentes
Carregue `key-points.md`, `questions.md` e `plan.md` da sessão.

### 2. Resolver Dúvidas
Para cada item não resolvido em `questions.md`:
- Se puder inferir com segurança: resolva e documente a inferência e justificativa
- Se for uma decisão do tipo fazer X ou Y, SEMPRE pergunte ao usuário para escolher, apresentando os prós e contras de cada opção
- Se precisar do usuário: agrupe **todas** as perguntas e faça em **uma única rodada**

### 3. Identificar Melhorias no Plano
Avalie criticamente o `plan.md`:
- Lacunas no escopo (o que foi esquecido?)
- Riscos técnicos não mapeados
- Ordenação de implementação que pode ser otimizada
- Casos de borda não considerados

### 4. Atualizar os Arquivos Existentes
> Nunca crie arquivos novos para substituir os antigos. Atualize os existentes.

Adicione no topo de cada arquivo atualizado:
```
> Atualizado na Fase 2 — <data>
```

- `key-points.md` → nova seção `## Adições do Refinamento`
- `questions.md` → marque resolvidas com `[x]` e registre a resposta inline
- `plan.md` → nova seção `## Refinamentos (v2)` com as melhorias; versione no topo

### 5. Solicitar Aprovação
Apresente o resumo das mudanças e pergunte se o plano refinado está aprovado
para avançar para geração de histórias.

---

## Critérios de Conclusão

**Fase 1 completa quando:**
- `docs/<sessao>/key-points.md` gravado ✅
- `docs/<sessao>/questions.md` gravado ✅
- `docs/<sessao>/plan.md` gravado ✅

**Fase 2 completa quando:**
- Todas as dúvidas críticas resolvidas ou escaladas ✅
- Três arquivos atualizados com marcação de versão ✅
- Usuário aprovou o plano refinado ✅

---

## Templates

Consulte os templates em `references/` para o formato exato de cada arquivo:
- `references/template-key-points.md`
- `references/template-questions.md`
- `references/template-plan.md`
