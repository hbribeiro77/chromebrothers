package com.chromebrothers.navegador.regras

/**
 * O Mover gera vários passos (Destaque + arrastes + encerrar/fundir).
 * O desfazer do usuário deve reverter o pacote inteiro até a imagem original.
 */
object PacoteDoDesfazerDoMoverDaSelecaoAtiva {
    enum class TipoDePassoDoMover { DESTAQUE_NOVA, TRANSFORMACAO, MODO_TRANSFORMAR, FUNDIR, OUTRO }

    data class Entrada(val tipo: TipoDePassoDoMover, val idRelacionado: Int)

    /**
     * @return índice do [TipoDePassoDoMover.DESTAQUE_NOVA] do pacote no topo, ou -1 se não houver pacote.
     */
    fun indiceInicioDoPacoteNoTopo(
        passos: List<Entrada>,
        idPecaEmTransformar: Int,
    ): Int {
        if (passos.isEmpty()) return -1
        val idPeca = if (idPecaEmTransformar != -1) {
            idPecaEmTransformar
        } else {
            val topo = passos.last()
            when (topo.tipo) {
                TipoDePassoDoMover.DESTAQUE_NOVA,
                TipoDePassoDoMover.TRANSFORMACAO,
                TipoDePassoDoMover.MODO_TRANSFORMAR,
                TipoDePassoDoMover.FUNDIR,
                -> topo.idRelacionado
                TipoDePassoDoMover.OUTRO -> return -1
            }
        }
        val indiceDestaque = passos.indexOfLast {
            it.tipo == TipoDePassoDoMover.DESTAQUE_NOVA && it.idRelacionado == idPeca
        }
        if (indiceDestaque < 0) return -1
        for (indice in (indiceDestaque + 1) until passos.size) {
            val passo = passos[indice]
            val doPacote = when (passo.tipo) {
                TipoDePassoDoMover.TRANSFORMACAO,
                TipoDePassoDoMover.MODO_TRANSFORMAR,
                TipoDePassoDoMover.FUNDIR,
                -> passo.idRelacionado == idPeca
                TipoDePassoDoMover.DESTAQUE_NOVA,
                TipoDePassoDoMover.OUTRO,
                -> false
            }
            if (!doPacote) return -1
        }
        return indiceDestaque
    }
}
