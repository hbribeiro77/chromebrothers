# Warm-start Visualizador + MCP — Implementation Plan

> **For agentic workers:** Executar task a task. Sem commit automático.

**Goal:** Pré-carregar invisível Visualizador + MCP imagem/vídeo; abrir revela; fechar esconde sem bolha; toggle global nos ⋯ + tag/id no componente.

**Architecture:** Regra JVM decide elegíveis; preferência `preCarregarPaineisDaColecao`; Gestor cria janelas OCULTA após 1º paint; limite total 6 / visíveis 3.

**Tech Stack:** Kotlin, SharedPreferences, coleção flutuante WebView.

## Tasks

1. Regra `WarmStartDeComponentesDaColecaoDoNavegador` + testes (tag `warm-start`, IDs MCP, nome Visualizador).
2. Preferência + string do menu ⋯ checkable.
3. Painel: carregar sem mostrar; recolher sem limpar; X em warm-start = recolher.
4. Gestor: preCarregar; abrir reusa OCULTA; limites visível/total.
5. Activity: postDelayed warm-start; toggle menu.
6. Build/install.
