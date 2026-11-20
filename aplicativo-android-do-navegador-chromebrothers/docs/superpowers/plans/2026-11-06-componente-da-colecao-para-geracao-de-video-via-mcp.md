# Geração de vídeo MCP/API — Implementation Plan

> **For agentic workers:** TDD nas regras JVM; componente seed separado.

**Goal:** Componente `geracao-mcp-video` na coleção com Sjinn Wan 3 default.

**Architecture:** HTML seed + pedido/catálogo JVM + `ClienteApiDoSjinn.gerarVideo` + ponte `gerarVideosMcp` / `listarModelosGeracaoVideo` + seed na Activity.

**Tech Stack:** Kotlin, OkHttp, WebView bridge (mesmo padrão da imagem).

---

### Task 1: Pedido + catálogo + testes
### Task 2: Cliente gerarVideo + gravarVideoComMetadados
### Task 3: HTML + serviço/ponte + seed
### Task 4: Test + assemble + install
