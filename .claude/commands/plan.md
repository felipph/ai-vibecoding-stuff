# /plan — Iniciar Planejamento

Inicia a Fase 1 do workflow para o input fornecido.

## Como usar
```
/plan <descrição do que precisa ser feito>
```

## O que este comando faz

### 1. Definir a Sessão
- Sugira um nome de sessão em `kebab-case` baseado no input
- Confirme com o usuário antes de criar
- Crie o diretório: `docs/<nome-da-sessao>/`

### 2. Analisar o Input
Raciocine sobre o input e extraia:
- Funcionalidades identificadas
- Entidades e domínios envolvidos
- Integrações necessárias
- Riscos e restrições percebidos
- O que está claramente **fora** do escopo

### 3. Gravar `docs/<sessao>/key-points.md`
```markdown
# Pontos-Chave — <Nome da Sessão>
> Criado em: <data>

## Funcionalidades Identificadas
- ...

## Entidades / Domínios
- ...

## Integrações Necessárias
- ...

## Riscos e Restrições
- ...

## Fora do Escopo
- ...
```

### 4. Gravar `docs/<sessao>/questions.md`
Liste todas as dúvidas que bloqueiam ou impactam o planejamento:
```markdown
# Dúvidas — <Nome da Sessão>
> Criado em: <data>

## Dúvidas de Negócio
- [ ] ...

## Dúvidas Técnicas
- [ ] ...

## Ambiguidades
- [ ] ...
```
Se não houver dúvidas, grave o arquivo com `_Nenhuma dúvida identificada._`

### 5. Gravar `docs/<sessao>/plan.md`
```markdown
# Plano — <Nome da Sessão>
> Versão 1 — criado em: <data>

## Escopo da Entrega
...

## Abordagem Técnica
...

## Ordem de Implementação Sugerida
1. ...
2. ...

## Dependências Externas
- ...

## Fora do Escopo
- ...
```

### 6. Informar o Usuário
Apresente um resumo do que foi gravado e pergunte:
- Há dúvidas a resolver antes de prosseguir?
- O usuário quer ir para `/refine` agora?

## Saída esperada
- `docs/<sessao>/key-points.md` ✅
- `docs/<sessao>/questions.md` ✅
- `docs/<sessao>/plan.md` ✅
