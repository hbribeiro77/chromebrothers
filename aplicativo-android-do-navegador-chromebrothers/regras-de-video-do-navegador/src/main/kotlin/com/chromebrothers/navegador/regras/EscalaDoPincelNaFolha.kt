package com.chromebrothers.navegador.regras

import kotlin.math.max
import kotlin.math.min

/**
 * O tamanho do pincel é preso à folha (como no Procreate/Photoshop), não à tela: o mesmo gesto deixa
 * o mesmo traço na imagem em qualquer zoom. O controle de tamanho vale "dp na tela" numa escala de
 * referência fixa: a folha encaixada na tela inteira do aparelho, sem depender do zoom, da rotação
 * nem da largura da vista (gaveta do chat aberta ou fechada).
 */
object EscalaDoPincelNaFolha {
    /**
     * Pixels de tela por unidade da folha na referência: lado longo com lado longo, curto com curto
     * (a rotação do tablet não muda o resultado).
     */
    fun escalaDeReferencia(larguraDaTela: Int, alturaDaTela: Int, larguraDaFolha: Int, alturaDaFolha: Int): Float {
        if (larguraDaTela <= 0 || alturaDaTela <= 0 || larguraDaFolha <= 0 || alturaDaFolha <= 0) return 1f
        val telaLonga = max(larguraDaTela, alturaDaTela).toFloat()
        val telaCurta = min(larguraDaTela, alturaDaTela).toFloat()
        val folhaLonga = max(larguraDaFolha, alturaDaFolha).toFloat()
        val folhaCurta = min(larguraDaFolha, alturaDaFolha).toFloat()
        return min(telaLonga / folhaLonga, telaCurta / folhaCurta).coerceAtLeast(0.0001f)
    }

    /** Diâmetro na folha a partir do diâmetro nominal (px de tela na referência). */
    fun diametroNaFolha(diametroNominalPx: Float, escalaDeReferencia: Float): Float =
        diametroNominalPx / escalaDeReferencia.coerceAtLeast(0.0001f)

    /** Diâmetro que o pincel ocupa na tela com a escala atual da vista (prévia do tamanho). */
    fun diametroNaTela(diametroNominalPx: Float, escalaDeReferencia: Float, escalaDaVista: Float): Float =
        diametroNaFolha(diametroNominalPx, escalaDeReferencia) * escalaDaVista.coerceAtLeast(0f)
}
