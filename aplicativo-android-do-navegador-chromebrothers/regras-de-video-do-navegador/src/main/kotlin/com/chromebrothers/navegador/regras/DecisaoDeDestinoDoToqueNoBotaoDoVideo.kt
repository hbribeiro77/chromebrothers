package com.chromebrothers.navegador.regras

import java.net.URI

enum class DestinoDoToqueNoBotaoDoVideo {
    PLAYER_DE_ARQUIVO,
    PLAYER_DA_PAGINA,
}

object DecisaoDeDestinoDoToqueNoBotaoDoVideo {
    private val extensoesDeArquivo = listOf(
        ".mp4", ".m4v", ".webm", ".mkv", ".mov", ".ogv", ".m3u8", ".mpd",
    )

    fun destino(endereco: String): DestinoDoToqueNoBotaoDoVideo {
        if (!DecisaoDeAbrirVideoNoPlayerNativo.podeAbrir(endereco)) {
            return DestinoDoToqueNoBotaoDoVideo.PLAYER_DA_PAGINA
        }
        val caminho = try {
            URI(endereco.trim()).path?.lowercase().orEmpty()
        } catch (_: Exception) {
            return DestinoDoToqueNoBotaoDoVideo.PLAYER_DA_PAGINA
        }
        val arquivo = extensoesDeArquivo.any { caminho.endsWith(it) }
        return if (arquivo) {
            DestinoDoToqueNoBotaoDoVideo.PLAYER_DE_ARQUIVO
        } else {
            DestinoDoToqueNoBotaoDoVideo.PLAYER_DA_PAGINA
        }
    }
}
