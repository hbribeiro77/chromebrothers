# Gravador visual de roteiros — Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans ou implementar task-by-task. Checkboxes abaixo.

**Goal:** Faixa de gravação sobre a página: pick de elementos → candidatas → passos do schema atual → Play/Salvar no store de roteiros.

**Architecture:** Regras puras (candidatas + montagem de passo) + script JS de pick na WebView + `PainelDoGravador…` na Activity; Play/Salvar reusam executor e `GravacaoDosRoteiros…`.

**Tech Stack:** Kotlin JVM (regras) + Android Views/WebView Gecko + schema `RoteiroDeAutomacaoDaPagina` existente.

## Global Constraints

- Sem ações novas no schema além das já existentes (`abrir`, `aguardar`, `clicar`, `preencher`, `esperar_ms`, `rolar`, `clicar_se`).
- UI = faixa compacta, não gaveta/coleção.
- Uma gravação por vez; Play cancela executor anterior.

---

### Task 1: Ranqueamento de candidatas (TDD)

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/.../CandidatasDoElementoParaORoteiroDeAutomacao.kt`
- Create: `regras-de-video-do-navegador/src/test/kotlin/.../TesteDasCandidatasDoElementoParaORoteiroDeAutomacao.kt`

**Produces:** `data class MarcaDoElementoParaORoteiro`, `fun ranquear(marca): List<CandidataDoElementoParaORoteiro>` com `rotulo`, `seletor`, `texto`.

- [ ] Testes: id, name, aria, texto, nth
- [ ] Implementar ranqueamento
- [ ] `gradle :regras-de-video-do-navegador:test --tests …Candidatas…`

### Task 2: Montagem de passo a partir da escolha

**Files:**
- Create/extend: mesmo pacote regras — `MontagemDoPassoDoRoteiroAPartirDaCandidata.kt`
- Test: `TesteDaMontagemDoPassoDoRoteiroAPartirDaCandidata.kt`

**Produces:** funções que, dado tipo de passo + candidata + extras (valor, timeout, indice, contem, se_nao), devolvem `PassoDoRoteiroDeAutomacao` ou erro.

- [ ] Testes por tipo de passo
- [ ] Implementar
- [ ] Rodar testes

### Task 3: Script de pick na página

**Files:**
- Create: `ScriptsDoPickDeElementoParaOGravadorDeRoteiro.kt` (instalador + cancelar + resultado JSON)

**Produces:** JS que destaca elemento sob toque e devolve marca (id, name, aria, texto, tag, indiceNoSeletorTag) via bridge ou `evaluateJavascript` callback — preferir bridge `@JavascriptInterface` no painel/Activity: `chromebrothersGravador.elementoEscolhido(json)`.

- [ ] Script + parsing da marca com `TextoJsonDoChat`
- [ ] Teste unit do parser da marca

### Task 4: Painel faixa + wire

**Files:**
- Create: layout + `PainelDoGravadorDeRoteiroDeAutomacaoDoNavegador.kt`
- Modify: `layout_atividade_principal…` (container FrameLayout overlay se preciso)
- Modify: `PainelDoChat…` (botão gravar)
- Modify: `AtividadePrincipal…` (ligar pick, play, salvar)
- strings

**Produces:** abrir faixa, URL/nome, chips, lista passos, pick→candidatas dialog, Play, Salvar, ⋯ copiar/enviar chat.

- [ ] UI mínima funcional
- [ ] assembleDebug + install

### Task 5: Verificação

- [ ] Testes regras
- [ ] Smoke manual: gravar 2 passos, play, salvar, ▶ do chat
