### Task 1: Regras — destino e resultado do envio

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/EnvioDeImagemDoComponenteParaCamadaDoEditor.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor.kt`

**Interfaces:**
- Produces: `ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor`, `EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(...)`, `EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(...)`

- [ ] **Step 1: Write the failing test**

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

- [ ] **Step 2: Run test to verify it fails**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor"
```

Expected: FAIL (classe/símbolos não existem).

- [ ] **Step 3: Write minimal implementation**

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

- [ ] **Step 4: Run tests — expect PASS** (mesmo comando do Step 2)

---

