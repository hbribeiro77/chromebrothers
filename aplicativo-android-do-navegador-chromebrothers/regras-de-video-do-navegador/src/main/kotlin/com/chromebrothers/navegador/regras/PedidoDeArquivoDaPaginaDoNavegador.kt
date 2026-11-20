package com.chromebrothers.navegador.regras

enum class CameraDoPedidoDeArquivoDaPagina { FOTO, VIDEO }

object PedidoDeArquivoDaPaginaDoNavegador {
    const val MODO_DE_ESCOLHER_PASTA = 2
    private val extensoesDeFoto = setOf("jpg", "jpeg", "png", "gif", "webp", "heic", "bmp")
    private val extensoesDeVideo = setOf("mp4", "webm", "mov", "mkv", "3gp")

    fun cameras(tiposAceitos: List<String>, escolhePasta: Boolean): List<CameraDoPedidoDeArquivoDaPagina> {
        if (escolhePasta) return emptyList()
        val tipos = tiposAceitos.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (tipos.isEmpty() || tipos.any { it == "*/*" }) {
            return listOf(CameraDoPedidoDeArquivoDaPagina.FOTO, CameraDoPedidoDeArquivoDaPagina.VIDEO)
        }
        val cameras = mutableListOf<CameraDoPedidoDeArquivoDaPagina>()
        if (tipos.any { aceita(it, "image", extensoesDeFoto) }) cameras += CameraDoPedidoDeArquivoDaPagina.FOTO
        if (tipos.any { aceita(it, "video", extensoesDeVideo) }) cameras += CameraDoPedidoDeArquivoDaPagina.VIDEO
        return cameras
    }

    private fun aceita(tipo: String, familia: String, extensoes: Set<String>): Boolean {
        val semPonto = tipo.removePrefix(".")
        return tipo == familia || tipo.startsWith("$familia/") || semPonto in extensoes
    }
}
