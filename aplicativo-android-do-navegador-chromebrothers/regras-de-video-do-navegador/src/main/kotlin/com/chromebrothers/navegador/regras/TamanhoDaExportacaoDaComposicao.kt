package com.chromebrothers.navegador.regras

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Tamanhos oferecidos ao salvar a composição: a moldura como está, 1080 e 2048 px no lado maior,
 * e o máximo que as camadas têm de detalhe (densidade da imagem mais nítida), sempre na proporção da
 * moldura e dentro do limite de memória (4096 px / 16 MP).
 */
object TamanhoDaExportacaoDaComposicao {
    enum class Tipo { MOLDURA, LADO_1080, LADO_2048, MAXIMO }

    data class Opcao(val tipo: Tipo, val largura: Int, val altura: Int)

    enum class Formato { PNG, JPEG }

    fun opcoes(molduraLargura: Int, molduraAltura: Int, densidadeMaxima: Float): List<Opcao> {
        if (molduraLargura <= 0 || molduraAltura <= 0) return emptyList()
        val lista = mutableListOf(Opcao(Tipo.MOLDURA, molduraLargura, molduraAltura))
        fun pelaMaiorLado(lado: Int, tipo: Tipo) {
            val escala = lado.toFloat() / max(molduraLargura, molduraAltura)
            lista += limitar(tipo, molduraLargura * escala, molduraAltura * escala)
        }
        pelaMaiorLado(1080, Tipo.LADO_1080)
        pelaMaiorLado(2048, Tipo.LADO_2048)
        val densidade = densidadeMaxima.coerceAtLeast(1f)
        lista += limitar(Tipo.MAXIMO, molduraLargura * densidade, molduraAltura * densidade)
        // Sem repetidos (ex.: moldura já de 1080), mantendo a primeira de cada tamanho.
        return lista.distinctBy { it.largura to it.altura }
    }

    private fun limitar(tipo: Tipo, largura: Float, altura: Float): Opcao {
        val escala = CrescimentoDaCamadaDeImagemParaOTraco.escala(largura, altura)
        return Opcao(tipo, max(1, (largura * escala).roundToInt()), max(1, (altura * escala).roundToInt()))
    }

    fun extensao(formato: Formato): String = if (formato == Formato.PNG) "png" else "jpg"

    fun mime(formato: Formato): String = if (formato == Formato.PNG) "image/png" else "image/jpeg"

    fun mimePeloNome(nomeDoArquivo: String): String =
        if (nomeDoArquivo.endsWith(".png", ignoreCase = true)) "image/png" else "image/jpeg"
}
