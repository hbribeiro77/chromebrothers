# Gerar Markdown e ZIP pelo chat (com liberação)

Data: 2026-11-01  
Status: implementado (2026-11-01)

## Objetivo

O chat pode gerar **documento Markdown** (`.md` + pasta `assets/` com imagens) e **pacote ZIP** (imagens, vídeos, md, etc.), com confirmação — ou **sem perguntar** se o site da aba estiver liberado. A mesma regra de liberação passa a valer para a **coleção**.

## Entrega padrão (opção C)

- Documento: `titulo.md` + `assets/` em Downloads/ChromeBrothers.
- Pacote: um `.zip` quando a IA usar a ferramenta de ZIP (compartilhar / vários tipos).

## Ferramentas

### `gerar_markdown`

- Args: `titulo`, `conteudo`, `imagens[]` opcional (`endereco`, `nome`).
- Endereços: `http`, `https`, `file://` local do app.
- App baixa imagens, grava assets, reescreve refs para `assets/nome`, publica o `.md`.

### `gerar_zip`

- Args: `nome`, `itens[]` (`endereco`, `caminho_no_zip`), `conteudo_markdown` opcional (vira um `.md` dentro do zip).
- App baixa itens, monta o zip, publica em Downloads/ChromeBrothers.

## Liberação

Site liberado na aba atual → segue sem perguntar para:

- montar / organizar / tirar coleção
- gerar_markdown / gerar_zip
- (já existia) abrir, criar aba, clicar, vincular

Revogar: configurações ou pedir à IA para tirar a liberação.

## Fora de escopo (fase 2)

Mais ferramentas de WebView (scroll, seletor, esperar, JS livre).
