# Review package Task 2
Base: prior working tree | Head: after Task 2 | no git

## Stat
- EditorDeRecorteETracoDaImagemDaBiblioteca.kt — added fundirBitmapNaCamadaAtiva (~L316-362)
- PainelDaGavetaDaBibliotecaDeVideos.kt — added enviarBitmapDoComponenteParaCamadaAtiva (~L2208-2215)
- strings_do_navegador_chromebrothers.xml — 4 new strings (~L444-447)

## Diff excerpts

### fundirBitmapNaCamadaAtiva
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
                val folha = destino as? Peca.Folha
                    ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
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
        // Tamanho natural em coords da composição via escala da destino; caber se for maior.
        val escalaX = imagemDestino.largura / imagemDestino.bitmap.width.coerceAtLeast(1).toFloat()
        val escalaY = imagemDestino.altura / imagemDestino.bitmap.height.coerceAtLeast(1).toFloat()
        var largura = copia.width * escalaX
        var altura = copia.height * escalaY
        val fator = min(
            1f,
            min(
                imagemDestino.largura / largura.coerceAtLeast(1f),
                imagemDestino.altura / altura.coerceAtLeast(1f),
            ),
        )
        largura *= fator
        altura *= fator
        temp.esquerda = imagemDestino.esquerda + (imagemDestino.largura - largura) / 2f
        temp.topo = imagemDestino.topo + (imagemDestino.altura - altura) / 2f
        temp.largura = largura
        temp.altura = altura
        objetos.add(temp)
        fundirPecaTransformadaNaOrigem(temp.id, imagemDestino.id)
        aoMudarSelecao?.invoke()
        invalidate()
        return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM
    }


```

### enviarBitmapDoComponenteParaCamadaAtiva
```kotlin
    fun enviarBitmapDoComponenteParaCamadaAtiva(
        origem: Bitmap,
    ): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
        if (!editandoImagem) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
        }
        return editor.fundirBitmapNaCamadaAtiva(origem)
    }

    private fun mostrarEspessura() {

```

### strings
```xml
    <string name="enviar_imagem_do_componente_para_a_camada_do_editor">Enviar à camada</string>
    <string name="recusa_envio_de_imagem_do_componente_sem_camada_no_editor">Selecione uma camada de imagem no editor</string>
    <string name="componentes_no_chrome_do_editor_de_imagem">Componentes</string>
    <string name="falha_ao_ler_imagem_do_componente_para_o_editor">Não foi possível ler a imagem</string>
</resources>

```
