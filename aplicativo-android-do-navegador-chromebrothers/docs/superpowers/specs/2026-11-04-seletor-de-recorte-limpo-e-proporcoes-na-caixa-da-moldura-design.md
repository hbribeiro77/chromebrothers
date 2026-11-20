# Seletor de recorte limpo e proporções na caixa da moldura

Data: 2026-11-04  
Status: implementado (2026-11-04)

## Problema

O menu do ícone de recorte mistura proporção da moldura com modo de seleção, e ainda mostra Recortar, Destacar e Imagem (seleção inteligente), redundantes ou ultrapassados pela barra flutuante da seleção.

## Objetivo

- Proporções da moldura no botão de resolução embaixo (caixa nossa).
- Seletor de recorte só com modos de seleção úteis: Retângulo, Laço.

## Fora de escopo

- Remover o motor interno da seleção inteligente.
- Redesign da faixa lateral.
- Mudanças na barra flutuante Copiar/Mover/Recortar.

## Caixa da moldura

Toque no botão `L×A` abre a caixa (layout do tamanho livre), com:

- Chips: 1:1, 4:3, 16:9, 9:16, Original, Livre
- Campos largura/altura
- Cancelar / Confirmar (Confirmar = tamanho digitado / Livre)

Chip de proporção (exceto Livre): aplica na hora, atualiza rótulo, fecha a caixa.  
Original sem imagem: Toast atual, caixa permanece.  
Livre: destaca chip e foca nos campos.

## Seletor de recorte

- Sem fileira de proporções.
- Sem botões Recortar, Destacar, Imagem.
- Permanecem: Retângulo, Laço.
- Sem toggle “Nova camada” nem “com o dedo” no menu (dedo virá como preferência global depois).
- Toque fora da seleção ativa só cancela; nova seleção começa no toque seguinte.

## Técnico

- Estender layout da caixa de tamanho livre.
- `BarraContextualDoEditorDeImagem`: RECORTE não mapeia mais para PROPORCAO.
- Painel: wire chips; ocultar/remover UI morta do menu.
