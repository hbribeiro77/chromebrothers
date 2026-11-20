### Task 7: Fatia 2 — arrastar `<img>` até o quadro

**Files:**
- Modify: script JS no `PainelDaColecaoFlutuante…`
- Modify: `EditorDeRecorteETraco…` ou overlay no Painel (hit-test “soltou no quadro”)
- Reuse: `resultadoDoGestoDeSoltar` + `enviarBitmapDoComponenteParaCamadaAtiva`

- [ ] **Step 1:** JS `dragstart` / touch-move longa em `<img>` notifica nativo com URI + coords de tela (bridge `iniciarArrasteDeImagemParaOEditor` / `atualizarArraste` / `soltarArraste`).

- [ ] **Step 2:** Nativo: shadow/feedback leve; no UP, converter coords → se intersecta bounds do `editor` na tela → `FUNDIR`, senão `CANCELAR`.

- [ ] **Step 3:** Mesmo decode + `enviarBitmap…` da fatia 1.

- [ ] **Step 4:** Testes de regra do gesto já cobertos na Task 1; assemble + install; smoke drag no quadro / fora.

---

## Self-review vs spec

| Spec | Task |
|------|------|
| Abrir componentes no editor (mesma coleção) | 4 |
| Entrada no chrome | 4 |
| Máx. 3 / mesmo gestor | 4 (reuso) |
| Fechar janelas ao fechar editor | 5 |
| Toque longo `<img>` → fundir | 1–3 |
| Folha promove | 1–2 |
| Undo | 2 (`fundirPecaTransformadaNaOrigem`) |
| Feedback destino/decode inválido | 2–3 |
| Arrastar → fundir / fora cancela | 7 (+ regras Task 1) |
| Fora de escopo (captura janela, substituir, camada nova) | não planejado |
