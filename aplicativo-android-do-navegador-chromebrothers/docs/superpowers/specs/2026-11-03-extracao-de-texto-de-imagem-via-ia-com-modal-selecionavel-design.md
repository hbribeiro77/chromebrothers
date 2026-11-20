# Extração de texto de imagem via IA (OCR) com modal

## Escopo

1. Ação **Extrair texto** no menu de toque longo da imagem na página e no menu do visor/biblioteca (imagem ampliada).
2. Visível **somente** com chave OpenRouter guardada (`cofre.temChave()`).
3. Chamada **avulsa** à API (modelo atual do chat), multimodal, **sem** gravar no histórico e **sem** tools.
4. Modal no padrão das caixas atuais: loading → texto selecionável + **Copiar** + **Enviar ao chat** + **Fechar**.
5. **Enviar ao chat** fecha a modal, abre a gaveta do chat e **preenche o campo** com o texto (usuário revisa e envia).

## Fora de escopo

- OCR on-device (ML Kit) na v1
- Abrir gaveta / poluir histórico durante a extração
- Reanexar a imagem ao chat ao “Enviar ao chat”
- Tradução, resumo ou “descrever imagem” (só OCR literal)
- Extração a partir de vídeo/frames

## Fluxo

```
Menu imagem → Extrair texto
  → obter bytes (página: download existente; biblioteca: arquivo/bitmap)
  → compactar como multimodal do chat
  → POST OpenRouter (chave + modelo atuais, prompt OCR fixo, sem tools)
  → modal: loading | texto | erro
  → Copiar → clipboard + toast
  → Enviar ao chat → abrir gaveta + preencher campo
  → Fechar
```

## Peças

| Peça | Responsabilidade |
|------|------------------|
| Regra de prompt/OCR | Prompt fixo (“devolver só o texto literal; sem comentário”); parse da resposta; mensagem se vazio |
| Cliente avulso | Uma chamada chat/completions com imagem; reusa compactação/mime do chat; sem histórico |
| Modal | Dialog + layout próprio; estados loading/resultado/erro; seleção de texto; botões |
| Wire página | Botão no `layout_caixa_de_opcoes_da_imagem_tocada_na_pagina…`; destino/callback novo |
| Wire biblioteca | Botão no menu do toque longo / ações da imagem ampliada |
| Wire chat | API pública no painel: abrir gaveta + preencher campo (alinhado ao `preencherCampo` já usado por tools) |

## UI

- Visual igual às caixas (`Dialog`, fundo transparente, largura ~420dp).
- Copiar / Enviar ao chat **habilitados só** com texto não vazio.
- Segundo toque em Extrair enquanto uma extração roda: **ignorar**.
- Sem texto: “Não encontrei texto nesta imagem.”
- Modelo sem visão / falha API / rede: erro amigável na modal (ex.: trocar modelo no chat).

## Critérios de aceite

- Sem chave: ação não aparece nos menus.
- Com chave: extrai, mostra texto selecionável, copia e preenche o campo do chat sem gravar a chamada OCR no histórico.
- Página e biblioteca/visor cobertos.
- Falhas não crasham; usuário vê feedback e pode fechar.

## Testes (regras)

- Prompt / parse: resposta limpa; resposta vazia → mensagem de “sem texto”; strip de cercas markdown se o modelo enrolar.
- Gate de visibilidade: com/sem chave.
- (Opcional UI) estados da modal habilitam/desabilitam botões conforme texto.
