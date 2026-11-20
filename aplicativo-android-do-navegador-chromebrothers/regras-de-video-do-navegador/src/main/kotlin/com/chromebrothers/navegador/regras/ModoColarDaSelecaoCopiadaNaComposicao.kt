package com.chromebrothers.navegador.regras

/**
 * Após Copiar: prévia flutuante até Colar (fundir na imagem destino) ou cancelar.
 */
object ModoColarDaSelecaoCopiadaNaComposicao {
    fun destinoAceitaFundir(
        idDestino: Int,
        idPrevia: Int,
        destinoEhImagem: Boolean,
        destinoEhFolha: Boolean = false,
    ): Boolean {
        if (idDestino == -1 || idPrevia == -1) return false
        if (idDestino == idPrevia) return false
        // Folha vazia (Nova camada) é promovida a imagem no Colar.
        return destinoEhImagem || destinoEhFolha
    }

    fun toqueForaCancelaModo(modoColarAtivo: Boolean): Boolean = modoColarAtivo

    fun trocaDeFerramentaCancelaModo(modoColarAtivo: Boolean): Boolean = modoColarAtivo

    fun deveOcultarPreviaNaListaDeCamadas(
        modoColarAtivo: Boolean,
        idDaCamada: Int,
        idPrevia: Int,
    ): Boolean = modoColarAtivo && idPrevia != -1 && idDaCamada == idPrevia

    fun gestosDaPreviaEntramNoHistorico(modoColarAtivo: Boolean): Boolean = !modoColarAtivo
}
