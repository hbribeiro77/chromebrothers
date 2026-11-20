package com.chromebrothers.navegador.regras

import kotlin.math.abs

/**
 * Abrir o chat deslizando da borda direita da página para a esquerda;
 * recolher deslizando para a direita a partir da junção entre a página e o chat.
 * Só dispara com deslize horizontal claro; toque simples segue para a página/chat.
 */
object GestoDeBordaParaAbrirEFecharAGavetaDoChatDoNavegador {
    const val LARGURA_DA_BORDA_DP: Float = 20f
    const val ZONA_DA_JUNCAO_DO_LADO_DA_PAGINA_DP: Float = 16f

    /** Do lado do chat só a margem interna da gaveta: a borda do corpo é do swipe entre abas. */
    const val ZONA_DA_JUNCAO_DO_LADO_DO_CHAT_DP: Float = 12f
    const val DISTANCIA_PARA_DISPARAR_DP: Float = 48f
    const val DESVIO_VERTICAL_PARA_DESISTIR_DP: Float = 24f
    const val RECUO_PARA_DESISTIR_DP: Float = 16f
    const val PROPORCAO_HORIZONTAL_MINIMA: Float = 1.5f

    enum class Inicio { NENHUM, ABRIR_PELA_BORDA, FECHAR_PELA_JUNCAO }

    enum class Decisao { AGUARDAR, DISPARAR, DESISTIR }

    fun inicio(
        habilitado: Boolean,
        gavetaAberta: Boolean,
        xDp: Float,
        dentroDaAlturaDaArea: Boolean,
        direitaDaAreaDasPaginasDp: Float,
        esquerdaDaGavetaDp: Float,
    ): Inicio {
        if (!habilitado || !dentroDaAlturaDaArea) return Inicio.NENHUM
        if (!gavetaAberta) {
            val naBorda = xDp <= direitaDaAreaDasPaginasDp && xDp >= direitaDaAreaDasPaginasDp - LARGURA_DA_BORDA_DP
            return if (naBorda) Inicio.ABRIR_PELA_BORDA else Inicio.NENHUM
        }
        val naJuncao = xDp >= esquerdaDaGavetaDp - ZONA_DA_JUNCAO_DO_LADO_DA_PAGINA_DP &&
            xDp <= esquerdaDaGavetaDp + ZONA_DA_JUNCAO_DO_LADO_DO_CHAT_DP
        return if (naJuncao) Inicio.FECHAR_PELA_JUNCAO else Inicio.NENHUM
    }

    fun decidir(inicio: Inicio, dxDp: Float, dyDp: Float): Decisao {
        val sentido = when (inicio) {
            Inicio.NENHUM -> return Decisao.DESISTIR
            Inicio.ABRIR_PELA_BORDA -> -1f
            Inicio.FECHAR_PELA_JUNCAO -> 1f
        }
        val avanco = dxDp * sentido
        val vertical = abs(dyDp)
        if (avanco <= -RECUO_PARA_DESISTIR_DP) return Decisao.DESISTIR
        if (vertical >= DESVIO_VERTICAL_PARA_DESISTIR_DP && vertical > abs(dxDp)) return Decisao.DESISTIR
        if (avanco >= DISTANCIA_PARA_DISPARAR_DP && avanco >= vertical * PROPORCAO_HORIZONTAL_MINIMA) {
            return Decisao.DISPARAR
        }
        return Decisao.AGUARDAR
    }
}
