### Task 2: Editor — `fundirBitmapNaCamadaAtiva`

**Files:**
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/EditorDeRecorteETracoDaImagemDaBiblioteca.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt` (wrapper público)

**Interfaces:**
- Consumes: `EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino`
- Produces: `Editor…fundirBitmapNaCamadaAtiva(bitmap: Bitmap): ResultadoDoEnvio…`
- Produces: `Painel…enviarBitmapDoComponenteParaCamadaAtiva(bitmap: Bitmap): ResultadoDoEnvio…` (só se `editandoImagem`)

- [ ] **Step 1: API no editor**

Adicionar método público que:
1. Lê `idDaCamadaAtiva` e a peça.
2. Chama `decidirDestino(editorAtivo = true, …)`.
3. Se `PROMOVER_FOLHA_E_FUNDIR`: `promoverFolhaParaImagem` (já existente, privado) e atualiza `idDaCamadaAtiva`.
4. Se `FUNDIR_NA_IMAGEM` (ou após promover): cria `Peca.Imagem` temporária com o bitmap, posicionada **centrada** no retângulo da destino, com largura/altura em coords da composição preservando aspect (caber dentro da destino se for maior; senão tamanho natural em px → coords via escala da destino).
5. Insere a peça na lista, chama `fundirPecaTransformadaNaOrigem(idTemp, idDestino)` (já empilha undo).
6. Retorna o enum da regra; em falha de promote → `RECUSAR_SEM_DESTINO`.

Esqueleto:

```kotlin
fun fundirBitmapNaCamadaAtiva(origem: Bitmap): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    var destino = objetos.find { it.id == idDaCamadaAtiva }
    val decisao = EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
        editorAtivo = true,
        idCamadaAtiva = idDaCamadaAtiva,
        destinoEhImagem = destino is Peca.Imagem,
        destinoEhFolha = destino is Peca.Folha,
    )
    when (decisao) {
        ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO -> return decisao
        ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR -> {
            val folha = destino as? Peca.Folha ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
            destino = promoverFolhaParaImagem(folha)
                ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
            idDaCamadaAtiva = destino.id
        }
        ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM -> Unit
    }
    val imagemDestino = destino as? Peca.Imagem
        ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
    val copia = origem.copy(Bitmap.Config.ARGB_8888, false) ?: origem
    val temp = Peca.Imagem(novoId(), copia)
    // dimensionar/centrar em cima de imagemDestino (aspect)
    objetos.add(temp)
    fundirPecaTransformadaNaOrigem(temp.id, imagemDestino.id)
    aoMudarSelecao?.invoke()
    invalidate()
    return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM
}
```

- [ ] **Step 2: Wrapper no Painel**

```kotlin
fun enviarBitmapDoComponenteParaCamadaAtiva(
    origem: Bitmap,
): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    if (!editandoImagem) {
        return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
    }
    return editor.fundirBitmapNaCamadaAtiva(origem)
}
```

Toast/mensagem curta no caller da Activity quando `RECUSAR_SEM_DESTINO` (string nova).

- [ ] **Step 3: Strings**

Em `strings_do_navegador_chromebrothers.xml`:

```xml
<string name="enviar_imagem_do_componente_para_a_camada_do_editor">Enviar à camada</string>
<string name="recusa_envio_de_imagem_do_componente_sem_camada_no_editor">Selecione uma camada de imagem no editor</string>
<string name="componentes_no_chrome_do_editor_de_imagem">Componentes</string>
<string name="falha_ao_ler_imagem_do_componente_para_o_editor">Não foi possível ler a imagem</string>
```

- [ ] **Step 4: Compile check**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

Expected: SUCCESS.

---

