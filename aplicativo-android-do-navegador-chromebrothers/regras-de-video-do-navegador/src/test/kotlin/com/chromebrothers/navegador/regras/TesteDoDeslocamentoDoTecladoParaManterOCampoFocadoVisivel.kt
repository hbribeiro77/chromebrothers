package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoDeslocamentoDoTecladoParaManterOCampoFocadoVisivel {
    @Test
    fun naoSobeSeOCampoJaFicaAcimaDoTeclado() {
        assertEquals(
            0f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoParaManterFocoVisivel(
                baseDoFoco = 400f,
                topoDoTeclado = 800f,
                margem = 16f,
            ),
        )
    }

    @Test
    fun sobeSoACoberturaDoCampoPeloTeclado() {
        assertEquals(
            -116f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoParaManterFocoVisivel(
                baseDoFoco = 900f,
                topoDoTeclado = 800f,
                margem = 16f,
            ),
        )
    }

    @Test
    fun limitaASubidaAFolgaDoTeclado() {
        assertEquals(
            -200f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoLimitadoPelaFolgaDoTeclado(
                deslocamento = -500f,
                folgaDoTeclado = 200f,
            ),
        )
        assertEquals(
            0f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoLimitadoPelaFolgaDoTeclado(
                deslocamento = 40f,
                folgaDoTeclado = 200f,
            ),
        )
    }

    @Test
    fun calculaNoEspacoCssEConvertePelaEscalaDaArea() {
        // Viewport CSS 800, área Android 800 (escala 1): campo a 700, teclado 200 → sobe 116.
        assertEquals(
            -116f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalComFocoNoViewport(
                baseDoFocoCss = 700f,
                alturaDoViewportCss = 800f,
                folgaDoTecladoPx = 200f,
                alturaDaAreaPx = 800f,
                margemCss = 16f,
            ),
        )
        // Campo no meio: já visível.
        assertEquals(
            0f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalComFocoNoViewport(
                baseDoFocoCss = 400f,
                alturaDoViewportCss = 800f,
                folgaDoTecladoPx = 200f,
                alturaDaAreaPx = 800f,
                margemCss = 16f,
            ),
        )
        // Densidade 2.5: CSS 800 ↔ Android 2000; campo a 700 CSS, teclado 500 px → folga CSS 200.
        assertEquals(
            -290f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalComFocoNoViewport(
                baseDoFocoCss = 700f,
                alturaDoViewportCss = 800f,
                folgaDoTecladoPx = 500f,
                alturaDaAreaPx = 2000f,
                margemCss = 16f,
            ),
        )
        assertEquals(
            0f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalComFocoNoViewport(
                baseDoFocoCss = null,
                alturaDoViewportCss = 800f,
                folgaDoTecladoPx = 200f,
                alturaDaAreaPx = 800f,
                margemCss = 16f,
            ),
        )
    }

    @Test
    fun leMedidaDoFocoNoJsonDoJavascript() {
        val medida = PosicaoDoCampoFocadoParaOTecladoDoNavegador.lerMedida(
            """{"bottom":912.5,"top":880,"viewport":1000}""",
        )
        assertEquals(912.5f, medida?.bottomCss)
        assertEquals(880f, medida?.topCss)
        assertEquals(1000f, medida?.viewportCss)
        assertNull(PosicaoDoCampoFocadoParaOTecladoDoNavegador.lerMedida("null"))
        assertNull(PosicaoDoCampoFocadoParaOTecladoDoNavegador.lerMedida(""))
        assertNull(PosicaoDoCampoFocadoParaOTecladoDoNavegador.lerMedida("""{"bottom":10}"""))
    }

    @Test
    fun baseDoPanPrefereCaretENaoUsaFundoDeCaixaAltaNoTopo() {
        // Busca/composer alto no topo: bottom distante não deve puxar a página toda.
        assertEquals(
            120f,
            PosicaoDoCampoFocadoParaOTecladoDoNavegador.baseCssParaPan(
                bottomCss = 700f,
                topCss = 80f,
                caretBottomCss = null,
                viewportCss = 900f,
            ),
        )
        // Caret manda quando existe.
        assertEquals(
            640f,
            PosicaoDoCampoFocadoParaOTecladoDoNavegador.baseCssParaPan(
                bottomCss = 800f,
                topCss = 500f,
                caretBottomCss = 640f,
                viewportCss = 900f,
            ),
        )
        // Campo baixo normal: usa o bottom.
        assertEquals(
            820f,
            PosicaoDoCampoFocadoParaOTecladoDoNavegador.baseCssParaPan(
                bottomCss = 820f,
                topCss = 780f,
                caretBottomCss = null,
                viewportCss = 900f,
            ),
        )
    }

    @Test
    fun deslocamentoComMedidaUsaBaseEfetivaDoCampoNoTopo() {
        val medida = PosicaoDoCampoFocadoParaOTecladoDoNavegador.lerMedida(
            """{"bottom":720,"top":64,"viewport":900}""",
        )!!
        assertEquals(
            0f,
            EspacoDoTecladoSobreAPaginaDoNavegador.deslocamentoVerticalComFocoNoViewport(
                baseDoFocoCss = medida.baseParaPan(),
                alturaDoViewportCss = medida.viewportCss,
                folgaDoTecladoPx = 400f,
                alturaDaAreaPx = 900f,
                margemCss = 16f,
            ),
        )
    }

    @Test
    fun scriptDaMedidaTemFallbackParaComposerTipoProseMirror() {
        val script = PosicaoDoCampoFocadoParaOTecladoDoNavegador.script()
        assertTrue(script.contains("ProseMirror"), script)
        assertTrue(script.contains("__cbGravadorAlvo") || script.contains("melhorEditor"), script)
        assertTrue(
            script.contains("Pergunte") || script.contains("Ask ChatGPT") || script.contains("pergunte"),
            script,
        )
        assertTrue(script.contains("role") && script.contains("textbox"), script)
    }
}
