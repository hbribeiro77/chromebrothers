package com.chromebrothers.navegador.regras

/**
 * Onde o vídeo baixado fica: Downloads/ChromeBrothers (lista de opções, como sempre foi) ou a Galeria
 * (Movies/ChromeBrothers — botão de um toque). Áudio sem vídeo vai para Downloads mesmo indo à Galeria.
 */
enum class DestinoDoDownloadDeMidia {
    DOWNLOADS,
    GALERIA,
    ;

    companion object {
        fun peloNome(nome: String?): DestinoDoDownloadDeMidia =
            entries.firstOrNull { it.name == nome } ?: DOWNLOADS
    }
}

object PastaDoDownloadDeMidia {
    const val DOWNLOADS = "Download/ChromeBrothers"
    const val GALERIA = MovimentoDaMidiaGeradaParaAGaleria.PASTA_DOS_VIDEOS

    /** Vai para a coleção de vídeos (Galeria) só se pedir Galeria e o arquivo for vídeo. */
    fun vaiParaAGaleria(destino: DestinoDoDownloadDeMidia, mime: String): Boolean =
        destino == DestinoDoDownloadDeMidia.GALERIA && mime.startsWith("video/")

    fun avisoDePronto(naGaleria: Boolean): String =
        if (naGaleria) "Vídeo salvo na Galeria (Movies/ChromeBrothers)." else "Vídeo salvo em Downloads/ChromeBrothers."
}
