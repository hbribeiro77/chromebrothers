package com.chromebrothers.navegador.regras

/**
 * Por que o download não deu certo, em linguagem clara (aparece na notificação e no aviso da gaveta).
 */
object MensagensDeFalhaDoDownloadDeMidia {
    const val DRM =
        "Este vídeo tem proteção DRM (como Netflix e Prime Video) e não pode ser baixado."
    const val PAGINA_EM_VEZ_DO_VIDEO =
        "O site devolveu uma página em vez do vídeo — geralmente pede login. Entre na sua conta, dê play e tente de novo."
    const val SEM_TRECHOS = "Não achei os trechos desse vídeo. Dê play por alguns segundos e tente de novo."
    const val TRECHOS_DEMAIS = "Esse vídeo tem trechos demais para montar (transmissão longa ou ao vivo)."
    const val DASH_SEM_MONTAGEM = "Esse vídeo vem em trechos DASH num formato que ainda não dá para montar."
    const val NAO_TERMINOU = "O download não terminou. Confira a internet e tente de novo."

    fun doCodigo(codigo: Int): String = when (codigo) {
        401, 403 -> "O site recusou o download (pede login ou o link é só para o player). " +
            "Entre na sua conta, dê play no vídeo e tente de novo."
        404, 410 -> "O link do vídeo expirou. Dê play de novo e tente baixar logo em seguida."
        429 -> "O site está limitando os downloads agora. Espere um pouco e tente de novo."
        in 500..599 -> "O servidor do site falhou (erro $codigo). Tente de novo mais tarde."
        else -> "O download parou (resposta $codigo)."
    }

    /** Resposta que é uma página (HTML/JSON de erro) e não mídia. */
    fun ehPaginaEmVezDeMidia(tipoDoConteudo: String?): Boolean {
        val tipo = tipoDoConteudo?.substringBefore(';')?.trim()?.lowercase().orEmpty()
        return tipo == "text/html" || tipo == "application/xhtml+xml"
    }
}
