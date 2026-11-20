# Task OCR-6 — Extrair texto no menu de toque longo do visor da biblioteca

## Status
Implementado. Build OK. Instalação via adb NÃO realizada (nenhum dispositivo/emulador conectado). Sem commit.

## Alterações
- `layout_caixa_de_opcoes_do_toque_longo_na_imagem_ampliada_do_visor_da_biblioteca.xml`: novo botão `botao_extrair_texto_da_imagem_ampliada_pelo_toque_longo` (`@string/extrair_texto_da_imagem`, `visibility=gone` por padrão).
- `PainelDaGavetaDaBibliotecaDeVideos.kt`:
  - `fun ligarExtracaoDeTexto(temChave: () -> Boolean, extrair: (ByteArray) -> Unit)`.
  - Em `mostrarOpcoesDoToqueLongoNaImagemAmpliada`: visibilidade via `ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(temChave())`; clique fecha o diálogo e chama `extrairTextoDaImagemAberta()`.
  - `extrairTextoDaImagemAberta()`: item aberto → `gravacaoDeImagens.ler(nomeDoArquivo)`; sem id → PNG do `quadroAmpliado` (mesmo padrão de `copiarImagemAberta`); sem bytes → toast `quadro_indisponivel`.
  - Sem `ligarExtracaoDeTexto` (ex.: `AtividadeDoPlayerNativo...`) o botão fica oculto.
- `AtividadePrincipalDoNavegadorChromebrothers.kt`: após criar o painel, `ligarExtracaoDeTexto(temChave = cofre.temChave(), extrair = orquestrador.extrairDeBytes)`.

## Verificação
- `gradle :app:assembleDebug` (JDK 21, gradle-8.11.1) → BUILD SUCCESSFUL (1ª tentativa falhou por import faltando de `regras.ExtracaoDeTextoDeImagemViaIaDoNavegador`; corrigido).
- `adb devices` (inclusive após kill-server/start-server) → lista vazia; `adb install -r` não executado.
- APK pronto: `app\build\outputs\apk\debug\app-debug.apk`.
- Sem testes novos (wiring de UI).

## Pendências / concerns
- Conectar dispositivo/emulador e rodar: `& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk`.
- Smoke manual (passos 1–5 do brief) não executado.
- Para itens da biblioteca `naGaleria` cujo arquivo local tenha sido apagado, `ler()` retorna null → toast de indisponível (mesmo comportamento de fallback do `copiarItem`).
