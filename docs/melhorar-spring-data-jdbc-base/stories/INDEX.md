# Índice de Histórias — melhorar-spring-data-jdbc-base
> Criado em: 2026-03-09

## Tabela de Histórias

| ID | Título | Complexidade | Dependências | Status |
|----|--------|--------------|--------------|--------|
| US-001 | Criar AbstractElSqlRepository | Média | - | ✅ Completo |
| US-002 | Criar Asset de Teste | Baixa | US-001 | ✅ Completo |
| US-003 | Atualizar SKILL.md | Baixa | US-001 | ✅ Completo |
| US-004 | Substituir custom-repositories.md | Média | US-001 | ✅ Completo |
| US-005 | Criar Exemplo .elsql | Baixa | - | ⏳ Pending |

## Ordem de Implementação Sugerida

### 1️⃣ US-001: Criar AbstractElSqlRepository (Fundação)

**Por que primeiro?**
- É a base para todas as outras histórias
- US-002, US-003 e US-004 dependem desta
- Maior valor técnico e fundação

**Riscos:** Nenhum bloqueio identificado

---

### 2️⃣ US-003: Atualizar SKILL.md (Documentação Principal)

**Por que segundo?**
- Depende apenas da US-001
- Documenta a funcionalidade principal
- Baixa complexidade, ganho rápido

**Riscos:** Nenhum

---

### 3️⃣ US-004: Substituir custom-repositories.md (Padrão Completo)

**Por que terceiro?**
- Depende da US-001
- Reescreve a documentação principal de custom repositories
- Maior esforço de documentação

**Riscos:** Nenhum

---

### 4️⃣ US-002: Criar Asset de Teste (Exemplo de Uso)

**Por que quarto?**
- Depende da US-001
- Demonstra testabilidade da classe base
- Completa o conjunto de assets

**Riscos:** Nenhum

---

### 5️⃣ US-005: Criar Exemplo .elsql (Independente)

**Por que último?**
- Independente, pode ser feito em qualquer momento
- Baixa prioridade funcional (é um exemplo auxiliar)
- Completa a documentação com exemplo prático

**Riscos:** Nenhum

**Alternativa:** Pode ser feita em paralelo com US-002 ou US-003, pois é independente.

---

## Diagrama de Dependências

```
US-001 (AbstractElSqlRepository)
  ├─→ US-002 (Asset de Teste)
  ├─→ US-003 (SKILL.md)
  └─→ US-004 (custom-repositories.md)

US-005 (Exemplo .elsql)
  (Independente)
```

## Resumo de Esforço

| Complexidade | Quantidade | Esforço Estimado |
|--------------|------------|-------------------|
| Alta | 0 | 0 dias |
| Média | 2 | 2-3 dias |
| Baixa | 3 | 1-1.5 dias |
| **Total** | **5** | **3.5-4.5 dias** |

## Próximos Passos

1. ✅ Histórias criadas
2. ✅ US-001, US-002, US-003, US-004 completadas (4/5 = 80%)
3. ⏳ US-005 pendente
4. ⏳ Executar commits git manualmente para todas as histórias

**Histórias Restantes:**
- US-005: Criar Exemplo .elsql (última história)

**Para continuar:**
- `/dev melhorar-spring-data-jdbc-base US-005` — Iniciar última história
- `/status melhorar-spring-data-jdbc-base` — Ver status detalhado da sessão

---

**Progresso: 80% completo (4 de 5 histórias)**
