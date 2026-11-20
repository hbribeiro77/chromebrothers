# Relatório Task OCR-3 — Orquestrador

**Status:** DONE (sem commit; menus/Activity não ligados)

## Arquivo criado
`app/src/main/kotlin/com/chromebrothers/navegador/OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt`

## Comportamento
- Construtor: `(activity: AppCompatActivity, cofre: CofreDaChaveDaOpenRouter, lerModelo: () -> String, aoAbrirChatComTexto: (String) -> Unit)`.
- `extrairDeBytes(bytes)`: decodifica com `BitmapFactory`, chama `extrairDeBitmap`, e recicla o bitmap decodificado (cópia própria) em `finally`.
- `extrairDeBitmap(bitmap)`: não recicla o bitmap do chamador. Usa `paraParte(bitmap)` (que já recicla suas cópias internas).
- `emAndamento` ignora segunda extração; zerado em `aoTerminar`.
- Cliente `ClienteDeChatDaOpenRouter` próprio (não cancela o do chat). `comFerramentas = false`.
- Acumula `aoTrecho`; no fim: `fim.erro != null` → `mostrarErro(mensagemDeFalha(erro))`, senão `mostrarResultado(textoParaExibir(acumulado))`.
- Copiar: `ClipboardManager` + toast `R.string.texto_extraido_copiado`. Enviar: `modal.fechar()` + `aoAbrirChatComTexto(texto)`.
- Falhas imediatas (bytes inválidos, `paraParte` nulo, chave vazia) → `modal.mostrarErro(FALHA_GENERICA)` sem chamar a API.

## Verificação
`:app:assembleDebug` → BUILD SUCCESSFUL. (1ª tentativa falhou por inferência recursiva de tipo em `modal`; corrigido com tipo explícito.)

## Preocupações
- `paraParte` roda na thread principal (decode/compress JPEG); pode causar leve jank em imagens grandes. Mover para thread de fundo se necessário.
- Não há teste unitário (classe depende de Activity/rede); a lógica pura já é testada em `ExtracaoDeTextoDeImagemViaIaDoNavegador`.
- `aoTerminar` assume execução na main thread (o cliente faz `principal.post`).
- Chave vazia mostra mensagem genérica; Tasks 5–6 devem esconder a ação sem chave (`mostrarAcao`).

## Correções de revisão (Critical + Important)
- **CRITICAL:** `aoTrecho` entrega o texto acumulado a cada chamada; trocado `StringBuilder.append` por `var acumulado = ""` + `acumulado = parcial` (substitui).
- **IMPORTANT (modal dispensado):** token `geracao` no orquestrador. `aoTerminar`/`aoTrecho` de uma geração antiga são ignorados, então o modal não reabre após dispensar.
- **IMPORTANT (emAndamento preso):** `ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador` ganhou `aoDispensar: (() -> Unit)? = null`, chamado no `setOnDismissListener`. O orquestrador usa `cancelarExtracaoEmAndamento()`: `geracao++`, `emAndamento=false`, `cliente.cancelar()`. Os retornos antecipados nunca setam `emAndamento`; `aoTerminar` o zera.
- **Verificação:** `gradle :app:assembleDebug` (gradle-8.11.1 em %LOCALAPPDATA%; o projeto não tem gradlew) → BUILD SUCCESSFUL. Sem commit.
