package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.PlistBinarioDaApple.Uid
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoPincelDoProcreate {

    /** Gravador mínimo de bplist00 (só para o teste): dict, array, texto, inteiro, real, bool, uid, null. */
    private object Gravador {
        fun gravar(raiz: Any?): ByteArray {
            val objetos = mutableListOf<Any?>()
            val refs = mutableListOf<IntArray>()
            fun achatar(v: Any?): Int {
                val i = objetos.size
                objetos += v
                refs += IntArray(0)
                when (v) {
                    is Map<*, *> -> {
                        val ks = v.keys.map { achatar(it.toString()) }
                        val vs = v.values.map { achatar(it) }
                        refs[i] = (ks + vs).toIntArray()
                    }
                    is List<*> -> refs[i] = v.map { achatar(it) }.toIntArray()
                }
                return i
            }
            achatar(raiz)
            val saida = ByteArrayOutputStream()
            saida.write("bplist00".toByteArray())
            val offsets = IntArray(objetos.size)
            fun cabecalho(tipo: Int, n: Int) {
                if (n < 15) saida.write((tipo shl 4) or n) else {
                    saida.write((tipo shl 4) or 0xF); saida.write(0x12)
                    for (s in intArrayOf(24, 16, 8, 0)) saida.write((n shr s) and 0xFF)
                }
            }
            objetos.forEachIndexed { i, v ->
                offsets[i] = saida.size()
                when (v) {
                    null -> saida.write(0x00)
                    false -> saida.write(0x08)
                    true -> saida.write(0x09)
                    is Int, is Long -> {
                        val n = (v as Number).toLong()
                        saida.write(0x13)
                        for (s in 56 downTo 0 step 8) saida.write(((n shr s) and 0xFF).toInt())
                    }
                    is Double -> {
                        val bits = java.lang.Double.doubleToLongBits(v)
                        saida.write(0x23)
                        for (s in 56 downTo 0 step 8) saida.write(((bits shr s) and 0xFF).toInt())
                    }
                    is String -> {
                        val ascii = v.all { it.code < 128 }
                        if (ascii) { cabecalho(0x5, v.length); saida.write(v.toByteArray(Charsets.US_ASCII)) } else {
                            cabecalho(0x6, v.length); saida.write(v.toByteArray(Charsets.UTF_16BE))
                        }
                    }
                    is Uid -> { saida.write(0x80); saida.write(v.indice) }
                    is Map<*, *> -> { cabecalho(0xD, v.size); refs[i].forEach { r -> saida.write(r shr 8); saida.write(r and 0xFF) } }
                    is List<*> -> { cabecalho(0xA, v.size); refs[i].forEach { r -> saida.write(r shr 8); saida.write(r and 0xFF) } }
                    else -> error("tipo")
                }
            }
            val tabela = saida.size()
            offsets.forEach { o -> for (s in intArrayOf(24, 16, 8, 0)) saida.write((o shr s) and 0xFF) }
            repeat(6) { saida.write(0) }
            saida.write(4); saida.write(2)
            fun l8(v: Long) { for (s in 56 downTo 0 step 8) saida.write(((v shr s) and 0xFF).toInt()) }
            l8(objetos.size.toLong()); l8(0); l8(tabela.toLong())
            return saida.toByteArray()
        }
    }

    /** Arquivo chaveado do jeito do Procreate: $objects[1] = SilicaBrush com UIDs para textos. */
    private fun arquivoDoProcreate(campos: Map<String, Any?>): ByteArray {
        val objetos = mutableListOf<Any?>("\$null")
        val silica = LinkedHashMap<String, Any?>()
        objetos += silica
        campos.forEach { (k, v) ->
            if (v is String) {
                silica[k] = Uid(objetos.size)
                objetos += v
            } else {
                silica[k] = v
            }
        }
        silica["bundledShapePath"] = Uid(0)
        silica["\$class"] = Uid(objetos.size)
        objetos += mapOf("\$classname" to "SilicaBrush", "\$classes" to listOf("SilicaBrush", "NSObject"))
        return Gravador.gravar(
            mapOf(
                "\$version" to 100000L,
                "\$archiver" to "NSKeyedArchiver",
                "\$top" to mapOf("root" to Uid(1)),
                "\$objects" to objetos,
            ),
        )
    }

    private fun lerArquivo(bytes: ByteArray) = PlistBinarioDaApple.raizDoArquivoChaveado(PlistBinarioDaApple.ler(bytes))!!

    @Test
    fun leTiposBasicosDoPlistBinario() {
        val bytes = Gravador.gravar(mapOf("a" to 3L, "b" to 0.5, "c" to true, "d" to "Lápis", "e" to listOf(1L, null)))
        val lido = PlistBinarioDaApple.ler(bytes) as Map<*, *>
        assertEquals(3L, lido["a"])
        assertEquals(0.5, lido["b"])
        assertEquals(true, lido["c"])
        assertEquals("Lápis", lido["d"])
        assertEquals(listOf(1L, null), lido["e"])
    }

    @Test
    fun arquivoQuebradoNaoLanca() {
        assertNull(PlistBinarioDaApple.ler(ByteArray(10)))
        val bom = Gravador.gravar(mapOf("a" to 1L))
        assertNull(PlistBinarioDaApple.ler(bom.copyOf(bom.size - 5)))
        val corrompido = bom.copyOf().also { it[it.size - 9] = 0x7F }
        assertNull(PlistBinarioDaApple.ler(corrompido))
    }

    @Test
    fun desfazOArquivoChaveado() {
        val silica = lerArquivo(arquivoDoProcreate(mapOf("name" to "Nanquim seco", "plotSpacing" to 0.0025)))
        assertEquals("Nanquim seco", silica["name"])
        assertEquals(0.0025, silica["plotSpacing"])
        assertNull(silica["bundledShapePath"])
        assertTrue("\$class" !in silica)
    }

    @Test
    fun mapeiaOsAjustesPrincipais() {
        val silica = lerArquivo(
            arquivoDoProcreate(
                mapOf(
                    "name" to "Meu lápis",
                    "plotSpacing" to 0.04,
                    "plotJitter" to 0.2,
                    "plotSmoothing" to 0.35,
                    "shapeScatter" to 1.0,
                    "shapeRotation" to 1.0,
                    "shapeInverted" to true,
                    "dynamicsPressureSize" to 0.7,
                    "dynamicsPressureOpacity" to 0.4,
                    "maxSize" to 0.5,
                    "minSize" to 0.01,
                    "textureDepth" to 0.8,
                    "dynamicsMix" to 0.5,
                ),
            ),
        )
        val leitura = PincelDoProcreate.ler(silica, "reserva", temFormaPropria = true, temGranuladoProprio = true)
        val p = leitura.pincel
        assertEquals("Meu lápis", p.nome)
        assertEquals(FormaDoCarimbo.IMAGEM, p.forma)
        assertEquals(0.3f, p.espacamento, 0.001f) // √0,04 × 1,5
        assertEquals(0.2f, p.dispersao, 0.001f)
        assertEquals(0.35f, p.streamline, 0.001f)
        assertEquals(1f, p.tremorDeRotacao, 0.001f)
        assertTrue(p.seguirTraco)
        assertTrue(leitura.formaInvertida)
        assertEquals(0.7f, p.pressaoNoTamanho, 0.001f)
        assertEquals(0.5f, p.tamanhoMaximo, 0.001f)
        assertEquals(FonteDoGranulado.IMAGEM, p.granulado)
        assertEquals(0.8f, p.intensidadeDoGranulado, 0.001f)
        assertEquals(listOf("mistura molhada"), leitura.semEquivalente)
        assertTrue(leitura.relatorio.any { it.startsWith("dynamicsMix=") && it.endsWith("(não usada)") })
    }

    @Test
    fun semFormaPropriaViraRedondoESemGranuladoNenhum() {
        val silica = lerArquivo(arquivoDoProcreate(mapOf("plotSpacing" to 0.01)))
        val leitura = PincelDoProcreate.ler(silica, "Pincel 3", temFormaPropria = false, temGranuladoProprio = false)
        assertEquals("Pincel 3", leitura.pincel.nome)
        assertEquals(FormaDoCarimbo.REDONDA, leitura.pincel.forma)
        assertEquals(FonteDoGranulado.NENHUM, leitura.pincel.granulado)
    }

    @Test
    fun valoresForaDaFaixaSaoContidos() {
        val silica = lerArquivo(arquivoDoProcreate(mapOf("plotSpacing" to 9.0, "plotSmoothing" to -2.0, "maxOpacity" to 0.2, "minOpacity" to 0.6)))
        val p = PincelDoProcreate.ler(silica, "x", temFormaPropria = false, temGranuladoProprio = false).pincel
        assertEquals(1.5f, p.espacamento, 0.001f)
        assertEquals(0f, p.streamline)
        assertTrue(p.opacidadeMaxima >= p.opacidadeMinima)
    }

    @Test
    fun achaAsPastasDosPinceisNaOrdemDoConjunto() {
        val entradas = listOf(
            "B-2/Brush.archive", "B-2/Shape.png",
            "A-1/Brush.archive", "A-1/Grain.png",
            "A-1/Sub01/Brush.archive",
            "Reset/Brush.archive", "QuickLook/Thumbnail.png",
            "C-3/Brush.archive",
            "brushset.plist",
        )
        assertEquals(listOf("C-3/", "B-2/", "A-1/"), PincelDoProcreate.pastasDosPinceis(entradas, listOf("C-3", "B-2", "A-1")))
        assertEquals(listOf("A-1/", "B-2/", "C-3/"), PincelDoProcreate.pastasDosPinceis(entradas))
        assertEquals(listOf(""), PincelDoProcreate.pastasDosPinceis(listOf("Brush.archive", "Shape.png")))
    }

    @Test
    fun leAOrdemDoBrushset() {
        val plist = PlistBinarioDaApple.ler(Gravador.gravar(mapOf("name" to "Kit", "brushes" to listOf("X", "Y"))))
        assertEquals(listOf("X", "Y"), PincelDoProcreate.ordemDoBrushset(plist))
        assertEquals("Kit", PincelDoProcreate.nomeDoConjunto(plist))
    }
}
