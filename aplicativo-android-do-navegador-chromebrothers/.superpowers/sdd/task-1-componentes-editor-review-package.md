# Review package Task 1
Base: (no git — new files)
Head: working tree

## Files
- CREATE D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers\regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\EnvioDeImagemDoComponenteParaCamadaDoEditor.kt
- CREATE D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers\regras-de-video-do-navegador\src\test\kotlin\com\chromebrothers\navegador\regras\TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor.kt

## Diff / full contents

### EnvioDeImagemDoComponenteParaCamadaDoEditor.kt
```kotlin
package com.chromebrothers.navegador.regras

enum class ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    FUNDIR_NA_IMAGEM,
    PROMOVER_FOLHA_E_FUNDIR,
    RECUSAR_SEM_DESTINO,
}

enum class ResultadoDoGestoDeArrasteDaImagemDoComponente {
    FUNDIR,
    CANCELAR,
}

object EnvioDeImagemDoComponenteParaCamadaDoEditor {
    fun decidirDestino(
        editorAtivo: Boolean,
        idCamadaAtiva: Int,
        destinoEhImagem: Boolean,
        destinoEhFolha: Boolean,
    ): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
        if (!editorAtivo || idCamadaAtiva == -1) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
        }
        if (destinoEhImagem) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM
        }
        if (destinoEhFolha) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR
        }
        return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
    }

    fun resultadoDoGestoDeSoltar(
        soltouSobreOQuadroDoEditor: Boolean,
    ): ResultadoDoGestoDeArrasteDaImagemDoComponente =
        if (soltouSobreOQuadroDoEditor) {
            ResultadoDoGestoDeArrasteDaImagemDoComponente.FUNDIR
        } else {
            ResultadoDoGestoDeArrasteDaImagemDoComponente.CANCELAR
        }
}

```

### TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor.kt
```kotlin
package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    @Test
    fun imagemAtivaAceitaFundir() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = 3,
                destinoEhImagem = true,
                destinoEhFolha = false,
            ),
        )
    }

    @Test
    fun folhaVaziaPromoveEFunde() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = 1,
                destinoEhImagem = false,
                destinoEhFolha = true,
            ),
        )
    }

    @Test
    fun semEditorOuSemCamadaRecusa() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = false,
                idCamadaAtiva = 1,
                destinoEhImagem = true,
                destinoEhFolha = false,
            ),
        )
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = -1,
                destinoEhImagem = false,
                destinoEhFolha = false,
            ),
        )
    }

    @Test
    fun soltarNoQuadroFundeEForaCancela() {
        assertEquals(
            ResultadoDoGestoDeArrasteDaImagemDoComponente.FUNDIR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(
                soltouSobreOQuadroDoEditor = true,
            ),
        )
        assertEquals(
            ResultadoDoGestoDeArrasteDaImagemDoComponente.CANCELAR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(
                soltouSobreOQuadroDoEditor = false,
            ),
        )
    }
}

```

