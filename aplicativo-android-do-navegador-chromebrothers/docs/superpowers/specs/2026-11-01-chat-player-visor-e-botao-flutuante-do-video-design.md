# Chat: player, visor e botão flutuante do vídeo

Data: 2026-11-01  
Status: implementado (2026-11-01)

## Objetivo

O chat consegue (1) **listar/abrir** vídeos e imagens da biblioteca nativa (player / visor da gaveta) e (2) tratar o vídeo detectado na aba: **mostrar o floating button** ou **abrir o player direto**, conforme o pedido do usuário.

## Ferramentas

### Aba atual

| Tool | Tipo | Comportamento |
|------|------|----------------|
| `ler_video_detectado_na_aba` | leitura | Relata se existe `videoAtual` (endereço truncado/útil, se botão pode aparecer). Sem vídeo: mensagem clara. |
| `mostrar_botao_flutuante_do_video` | ação* | Se há vídeo detectado: reposiciona/mostra o botão flutuante **sem** abrir player. Senão: recusa amigável. |
| `abrir_player_do_video_detectado` | ação* | Mesmo fluxo do toque no botão (`DecisaoDeDestinoDoToqueNoBotaoDoVideo` → player de arquivo ou player da página). |

### Biblioteca

| Tool | Tipo | Comportamento |
|------|------|----------------|
| `listar_videos_da_biblioteca` | leitura | Chave/id, título, se abre em player de arquivo; recentes (lista útil, sem inventar). |
| `listar_imagens_da_biblioteca` | leitura | Id, nome/título. |
| `abrir_video_da_biblioteca` | ação* | Args: `chave` (ou id). Abre como a gaveta (`abrirItemDaBiblioteca`). |
| `abrir_imagem_da_biblioteca` | ação* | Args: `id`. Abre no visor ampliado da biblioteca. |

\*Confirmação do usuário, **exceto** site liberado (mesma família de abrir endereço / página da biblioteca).

## Instrução do sistema

- Pediu o botão / floating → `mostrar_botao_flutuante_do_video` (depois de ler se precisar).
- Pediu tocar/abrir direto o da página → `abrir_player_do_video_detectado`.
- Pediu algo da gaveta → listar + abrir vídeo/imagem da biblioteca.
- Não confundir com `publicar_pagina` / WebView da coleção.

## Arquitetura

- Regras: nomes/schema/parse de args; textos de listagem a partir das bibliotecas em memória.
- App: callbacks no `PainelDoChat` + wire na `AtividadePrincipal` reutilizando `abrirPlayerNativo` / `abrirItemDaBiblioteca` / abertura do visor (mesmo caminho da gaveta).
- `mostrar_botao…` reutiliza o posicionamento já usado quando `videoAtual` existe.

## Fora de escopo (v1)

- Registrar/favoritar vídeo pela tool (só abrir / mostrar botão).
- Controles do player (pause, seek) via chat.
- Download pela tool.
- Abrir downloads da gaveta (seção baixados) — só vídeos/imagens da biblioteca.

## Critério de sucesso

Usuário pode pedir “mostra o botão do vídeo” e ver o floating button; ou “abre no player” e ir direto. Também “abre aquele vídeo/imagem da biblioteca” e cair no player/visor nativos.
