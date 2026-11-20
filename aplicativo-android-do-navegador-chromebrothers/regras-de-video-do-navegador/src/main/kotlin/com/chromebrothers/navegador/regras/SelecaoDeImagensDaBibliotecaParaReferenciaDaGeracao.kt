package com.chromebrothers.navegador.regras

/**
 * Seleção das imagens da biblioteca no modal de referência da geração MCP.
 * A ordem do toque vira a ordem das referências (o selo mostra 1, 2, 3...).
 */
object SelecaoDeImagensDaBibliotecaParaReferenciaDaGeracao {
    /** Marca/desmarca. Com o limite cheio, tocar numa nova não faz nada. */
    fun alternar(selecionados: List<String>, id: String, limite: Int): List<String> = when {
        id in selecionados -> selecionados - id
        selecionados.size >= limite -> selecionados
        else -> selecionados + id
    }

    /** Número do selo (1..n) ou null se não estiver escolhida. */
    fun numero(selecionados: List<String>, id: String): Int? =
        selecionados.indexOf(id).takeIf { it >= 0 }?.plus(1)

    /** Com o limite cheio, as não escolhidas ficam apagadas. */
    fun bloqueada(selecionados: List<String>, id: String, limite: Int): Boolean =
        id !in selecionados && selecionados.size >= limite

    fun subtitulo(quantidade: Int, limite: Int): String = when {
        limite == 1 && quantidade == 0 -> "Toque na imagem que vai servir de referência"
        quantidade == 0 -> "Toque para escolher até $limite imagens"
        quantidade >= limite -> "$quantidade de $limite escolhidas · limite atingido"
        quantidade == 1 -> "1 de $limite escolhida"
        else -> "$quantidade de $limite escolhidas"
    }

    fun rotuloDoBotao(quantidade: Int): String =
        if (quantidade <= 0) "Adicionar" else "Adicionar ($quantidade)"

    enum class Filtro(val rotulo: String) { TUDO("Tudo"), IMAGENS("Imagens"), VIDEOS("Vídeos") }

    fun <T> filtrar(itens: List<T>, filtro: Filtro, ehVideo: (T) -> Boolean): List<T> = when (filtro) {
        Filtro.TUDO -> itens
        Filtro.IMAGENS -> itens.filterNot(ehVideo)
        Filtro.VIDEOS -> itens.filter(ehVideo)
    }

    /** Só vale mostrar o filtro quando há imagens e vídeos juntos. */
    fun <T> mostraFiltro(itens: List<T>, ehVideo: (T) -> Boolean): Boolean =
        itens.any(ehVideo) && itens.any { !ehVideo(it) }
}
