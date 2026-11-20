package com.chromebrothers.navegador.regras

/**
 * Mídia nova em ChromeBrothers com o app aberto: relê a lista, aquece a miniatura antes de mostrar
 * e insere só a carta nova na grade.
 */
object AtualizacaoAoVivoDaGradeDoVisualizadorDeMidiaAoChegarArquivoNovo {
    const val ESPERA_PARA_JUNTAR_AVISOS_MS: Long = 600L
    const val LIMITE_DE_AQUECIMENTO_ANTES_DE_MOSTRAR: Int = 24

    fun urisParaAquecerAntesDeMostrar(
        urisEmOrdem: List<String>,
        jaNaMemoria: (String) -> Boolean,
    ): List<String> =
        urisEmOrdem.asSequence()
            .filterNot(jaNaMemoria)
            .take(LIMITE_DE_AQUECIMENTO_ANTES_DE_MOSTRAR)
            .toList()

    fun inseriuNoTopo(antigos: List<String>, novos: List<String>): Boolean {
        val primeiroAntigo = antigos.firstOrNull() ?: return novos.isNotEmpty()
        val posicao = novos.indexOf(primeiroAntigo)
        return posicao > 0
    }

    /** Sem isso o RecyclerView ancora na carta antiga e a nova nasce acima da tela. */
    fun deveVoltarAoTopoAposInserir(primeiraVisivelAntes: Int, inseriuNoTopo: Boolean): Boolean =
        inseriuNoTopo && primeiraVisivelAntes <= 0
}
