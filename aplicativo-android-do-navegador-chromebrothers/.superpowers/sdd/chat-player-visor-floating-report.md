# Chat: player, visor e botão flutuante

Status: feito. Sem commit.

## O que entrou
- Leitura: `ler_video_detectado_na_aba`, `listar_videos_da_biblioteca` (recentes: chave, título, abreNoPlayerDeArquivo), `listar_imagens_da_biblioteca` (histórico: id, nome).
- Ações com confirmação (site liberado segue direto): `mostrar_botao_flutuante_do_video` (mesmo `aplicarPosicaoDoBotao`), `abrir_player_do_video_detectado` (`abrirPlayerNativo`), `abrir_video_da_biblioteca` (chave → `buscar` + `abrirItemDaBiblioteca`), `abrir_imagem_da_biblioteca` (id → `PainelDaGavetaDaBibliotecaDeVideos.abrirImagemDaBiblioteca`, que chama `abrirVisor`).
- Instrução separa botão flutuante, player da aba e biblioteca da coleção. Continuação: “abre o vídeo” pede o player; “botão flutuante” pede o botão; biblioteca de vídeo/imagem não vira abrir endereço.

## Verificação
- `:regras-de-video-do-navegador:test` e `:app:assembleDebug`: BUILD SUCCESSFUL.
- `adb install -r app\build\outputs\apk\debug\app-debug.apk`: Success (`adb` em `%LOCALAPPDATA%\Android\Sdk\platform-tools`, fora do PATH).
- Não houve exercício manual no aparelho das sete ferramentas pelo chat.
