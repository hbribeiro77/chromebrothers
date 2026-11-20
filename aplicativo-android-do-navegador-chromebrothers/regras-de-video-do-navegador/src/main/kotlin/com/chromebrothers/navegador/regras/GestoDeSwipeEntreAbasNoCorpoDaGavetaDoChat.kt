package com.chromebrothers.navegador.regras

import kotlin.math.abs

/**
 * Swipe horizontal no corpo da gaveta do chat troca de aba, mas a página tem prioridade:
 * só troca se ninguém sob o dedo quer o gesto (rolagem lateral, slider, canvas, carrossel)
 * ou se o gesto começou na borda. Toque longo é seleção de texto / arraste de thumbnail.
 */
object GestoDeSwipeEntreAbasNoCorpoDaGavetaDoChat {
    const val BORDA_DP = 20f
    const val DISTANCIA_MINIMA_DP = 24f
    const val FRACAO_DA_LARGURA_PARA_TROCAR = 0.3f
    const val VELOCIDADE_PARA_TROCAR_DP_POR_S = 900f
    const val ATRIBUTO_SEM_SWIPE = "data-sem-swipe-de-aba"

    enum class Decisao { AGUARDAR, TROCAR_DE_ABA, DEIXAR_PARA_A_PAGINA }

    /**
     * [paginaQuerOGesto] null = a consulta à página ainda não respondeu (conta como "quer").
     * [conteudoRolaNaDirecao] = alguma vista nativa sob o dedo ainda rola para esse lado.
     */
    fun decidir(
        dxDp: Float,
        dyDp: Float,
        comecouNaBorda: Boolean,
        toqueLongo: Boolean,
        paginaQuerOGesto: Boolean?,
        conteudoRolaNaDirecao: Boolean,
        temAbaNaDirecao: Boolean,
    ): Decisao {
        if (toqueLongo) return Decisao.DEIXAR_PARA_A_PAGINA
        val adx = abs(dxDp)
        val ady = abs(dyDp)
        if (adx < DISTANCIA_MINIMA_DP && ady < DISTANCIA_MINIMA_DP) return Decisao.AGUARDAR
        if (adx < 2f * ady) return Decisao.DEIXAR_PARA_A_PAGINA
        if (!temAbaNaDirecao) return Decisao.DEIXAR_PARA_A_PAGINA
        if (comecouNaBorda) return Decisao.TROCAR_DE_ABA
        if (conteudoRolaNaDirecao) return Decisao.DEIXAR_PARA_A_PAGINA
        if (paginaQuerOGesto != false) return Decisao.DEIXAR_PARA_A_PAGINA
        return Decisao.TROCAR_DE_ABA
    }

    /** Dedo para a esquerda (dx < 0) leva à próxima aba; sem volta circular. */
    fun abaVizinha(ids: List<String>, idAtiva: String, paraAProxima: Boolean): String? {
        val i = ids.indexOf(idAtiva)
        if (i < 0) return null
        return ids.getOrNull(if (paraAProxima) i + 1 else i - 1)
    }

    fun deveConfirmarAoSoltar(
        deslocamentoPx: Float,
        larguraPx: Float,
        velocidadePxPorS: Float,
        densidade: Float,
    ): Boolean {
        if (deslocamentoPx == 0f || larguraPx <= 0f) return false
        val mesmoSentido = deslocamentoPx * velocidadePxPorS >= 0f
        if (abs(deslocamentoPx) >= larguraPx * FRACAO_DA_LARGURA_PARA_TROCAR) {
            return mesmoSentido || abs(velocidadePxPorS) < VELOCIDADE_PARA_TROCAR_DP_POR_S * densidade
        }
        return mesmoSentido && abs(velocidadePxPorS) >= VELOCIDADE_PARA_TROCAR_DP_POR_S * densidade
    }

    /** JS que responde true se o elemento em (x, y) CSS (ou um ancestral) quer gesto horizontal. */
    fun scriptDaConsulta(xCss: Float, yCss: Float): String = """
        (function(x,y){
          var e=document.elementFromPoint(x,y);
          while(e&&e.nodeType===1&&e!==document.documentElement){
            if(e.hasAttribute('$ATRIBUTO_SEM_SWIPE'))return true;
            var t=e.tagName;
            if(t==='CANVAS'||t==='VIDEO')return true;
            if(t==='INPUT'&&e.type==='range')return true;
            if((t==='INPUT'||t==='TEXTAREA')&&(e===document.activeElement||e.scrollWidth>e.clientWidth+1))return true;
            var s=getComputedStyle(e);
            if((s.overflowX==='auto'||s.overflowX==='scroll')&&e.scrollWidth>e.clientWidth+1)return true;
            var ta=s.touchAction||'';
            if(ta.indexOf('pan-y')>=0&&ta.indexOf('pan-x')<0)return true;
            if(ta==='none')return true;
            var c=(typeof e.className==='string'?e.className:'').toLowerCase();
            if(/swiper|carousel|slick|slider|glide|splide|flickity/.test(c))return true;
            e=e.parentElement;
          }
          return false;
        })(${xCss},${yCss})
    """.trimIndent()

    fun lerRespostaDaConsulta(bruto: String?): Boolean =
        bruto?.trim()?.trim('"') == "true"
}
