# Componentes ancorados como abas — plano

> **For agentic workers:** Implementar task-by-task. Spec: `docs/superpowers/specs/2026-11-08-componentes-ancorados-como-abas-na-gaveta-do-chat-design.md`

**Goal:** Ancorar janelas da coleção na gaveta do chat como abas; desvincular volta ao flutuante.

**Architecture:** Estado puro em `regras` (lista + limite 3 + aba ativa). UI: faixa de abas + host na gaveta. `PainelDaColecao` move a WebView para o host / devolve. Gestor + PainelDoChat orquestram.

**Tech Stack:** Kotlin Android, WebView reparent, HorizontalScrollView de abas.

## Tasks

- [x] T1: Regras + testes (`AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador`)
- [x] T2: Layout gaveta (faixa + host) + strings
- [x] T3: API no PainelDaColecao (ancorar/desancorar movendo WebView)
- [x] T4: Botão ancorar na janela + longo na bolha
- [x] T5: Faixa de abas no PainelDoChat + desvincular no longo
- [x] T6: Wire Gestor/Atividade + gravador exclusa + assemble/install
