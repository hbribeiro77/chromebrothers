# `aguardar_qualquer` + `ir_para` (ramificação A/B)

**Status:** aprovado (conversa 2026-11-07 — “beleza, vou querer”, opção 1)

## Objetivo

Esperar o primeiro de N estados na página, gravar o id do ramo em variável (padrão `_ramo`) e saltar para outro passo com `ir_para` (condicional ou incondicional). Serve para Topview: Generate (livre) vs cobrança de créditos.

## Passos

### `aguardar_qualquer`

```json
{
  "acao": "aguardar_qualquer",
  "variavel": "_ramo",
  "timeout_ms": 15000,
  "opcoes": [
    {"id": "livre", "texto": "Generate"},
    {"id": "creditos", "texto": "credits"}
  ]
}
```

- 2–4 opções; cada uma precisa de `id` + (`seletor` e/ou `texto`).
- Polling: na mesma checagem, a **primeira opção da lista** que estiver presente vence.
- Sucesso → `variaveis[variavel] = id` e segue o próximo passo.
- Timeout → falha clara listando os ids esperados.

### `marcar` (rótulo)

```json
{"acao":"marcar","id":"fluxo_livre"}
```

No-op na execução; ancora destino estável para `ir_para`.

### `ir_para`

```json
{"acao":"ir_para","passo":8,"se":"{{_ramo}}=livre"}
{"acao":"ir_para","destino":"fluxo_creditos","se":"{{_ramo}}=creditos"}
{"acao":"ir_para","destino":"fim"}
```

- `passo`: índice **1-based** da lista; ou `destino`: id de um `marcar`.
- `se` vazio = salto incondicional.
- `se` com `=` : após substituir `{{vars}}`, compara esquerda e direita (trim, case-insensitive). Se não bater, **segue para o próximo passo** (fallthrough).
- Proteção: no máximo 200 execuções de passo por corrida (anti-loop).

## Exemplo Topview

1. abrir  
2. aguardar_qualquer → `_ramo` = livre|creditos  
3. ir_para se livre → marcar fluxo_livre  
4. ir_para → marcar fluxo_creditos  
5. marcar fluxo_livre … passos A … ir_para fim  
6. marcar fluxo_creditos … passos B … ir_para fim  
7. marcar fim …

## Gravador

- Chip **A|B**: pick opção 1 (pede id) → pick opção 2 (pede id) → grava `aguardar_qualquer`.
- Chip **Marcar**: pede id.
- Chip **Ir para**: escolhe destino (marcar existente ou número do passo) + `se` opcional (`{{_ramo}}=id`).

## Fora de escopo

Sub-roteiros, `se/senao` aninhado, >4 opções.
