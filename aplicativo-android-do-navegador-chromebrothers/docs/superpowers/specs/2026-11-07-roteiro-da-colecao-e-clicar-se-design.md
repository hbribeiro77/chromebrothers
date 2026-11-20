# Roteiro a partir da coleção + `clicar_se`

**Status:** aprovado (conversa 2026-11-07 — “pode fazer”)

## Objetivo

1. Componente HTML com botão Play chama `chromebrothers.rodarRoteiro(idOuNome)` e dispara o roteiro na aba (sem IA).
2. Novo passo `clicar_se`: pega o N‑ésimo elemento de um seletor e só clica se o texto contiver `contem`; senão `se_nao` = `parar` (padrão) ou `seguir`.

## Contrato da ponte

- `rodarRoteiro(id)` → JSON imediato `{ok:true}` se iniciou, ou erro.
- Ao terminar: toast + `window.chromebrothersAoTerminarRoteiro?.(json)` se existir.

## Passo

```json
{"acao":"clicar_se","seletor":"#rso a","indice":0,"contem":"Wikipedia","se_nao":"parar"}
```
