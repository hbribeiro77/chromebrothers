package com.chromebrothers.navegador.regras

/**
 * Decide o que fazer quando um item da gaveta da biblioteca é solto sobre a coleção
 * que mostra o gerador MCP (imagem ou vídeo) para virar imagem de referência.
 */
object DestinoDoArrasteDaBibliotecaParaReferenciaMcp {
    enum class Acao { USAR_ARQUIVO, USAR_FRAME, USAR_VIDEO, RECUSAR }

    const val COMPONENTE_IMAGEM: String = "imagem"
    const val COMPONENTE_VIDEO: String = "video"

    /**
     * [tipoComponente]: "imagem" | "video" (gerador MCP); [mime]: mime do conteúdo ou vazio;
     * [tipoArraste]: "image" | "video" vindo do EstadoDoArrasteDaBiblioteca.
     */
    fun decidir(
        tipoArraste: String,
        mime: String,
        tipoComponente: String,
        modeloAceitaVideo: Boolean = false,
    ): Acao {
        val componente = tipoComponente.trim().lowercase()
        if (componente != COMPONENTE_IMAGEM && componente != COMPONENTE_VIDEO) return Acao.RECUSAR
        val tipo = tipoArraste.trim().lowercase()
        val mimeNormalizado = mime.trim().lowercase()
        if (tipo == "image" || mimeNormalizado.startsWith("image/")) return Acao.USAR_ARQUIVO
        if (tipo == "video" || mimeNormalizado.startsWith("video/")) {
            // Gerador de vídeo com modelo que aceita vídeo: entra o vídeo inteiro; senão vira um frame.
            return if (componente == COMPONENTE_VIDEO && modeloAceitaVideo) Acao.USAR_VIDEO else Acao.USAR_FRAME
        }
        return Acao.RECUSAR
    }

    /** Mapeia o id fixo do componente da coleção para "imagem"/"video"; vazio se não for gerador MCP. */
    fun tipoDoComponentePeloId(idDoComponente: String?): String = when (idDoComponente) {
        HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.ID -> COMPONENTE_IMAGEM
        HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.ID -> COMPONENTE_VIDEO
        else -> ""
    }
}
