package com.chromebrothers.navegador.regras

/**
 * Aba "Visor": a imagem/vídeo tocado na grade abre numa aba própria, não por cima da página da aba atual
 * (antes parecia que o navegador não respondia). Uma só, reaproveitada; fechar o visor fecha a aba e volta
 * para a aba de onde veio. Não é salva entre sessões.
 */
object AbaDoVisorDeMidiaDoNavegador {
    const val ENDERECO = "about:blank"
    const val TITULO = "Visor"

    /** Ao fechar a aba Visor: a aba de onde veio, se ainda existe; senão null (a gestão escolhe a vizinha). */
    fun abaParaVoltar(idDeOndeVeio: Long?, idDoVisor: Long, idsAbertos: List<Long>): Long? =
        idDeOndeVeio?.takeIf { it != idDoVisor && it in idsAbertos }

    /** Ao ativar a aba Visor vindo de [idAtiva]: lembra de onde veio (ficar no Visor não sobrescreve). */
    fun deOndeVeio(idAtiva: Long, idDoVisor: Long?, anterior: Long?): Long? =
        if (idAtiva == idDoVisor) anterior else idAtiva

    /**
     * A aba Visor navegou para um endereço de verdade (barra, link, busca): vira aba comum e o visor sai.
     * A própria página vazia do Visor (e endereço vazio) não conta.
     */
    fun navegacaoTiraDoVisor(url: String?): Boolean {
        val alvo = url?.trim().orEmpty()
        return alvo.isNotEmpty() && !alvo.equals(ENDERECO, ignoreCase = true)
    }

    /** Abas a gravar (sem a Visor) e o índice da ativa nessa lista (Visor ativa → a de onde veio). */
    fun paraGravar(
        ids: List<Long>,
        idAtiva: Long,
        idDoVisor: Long?,
        idDeOndeVeio: Long?,
    ): Pair<List<Long>, Int> {
        val salvas = ids.filter { it != idDoVisor }
        val alvo = if (idAtiva == idDoVisor) idDeOndeVeio else idAtiva
        return salvas to salvas.indexOf(alvo).coerceAtLeast(0)
    }
}
