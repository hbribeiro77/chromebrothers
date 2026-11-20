package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador
import com.chromebrothers.navegador.regras.MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao
import java.io.File

/**
 * Exclui os arquivos e, se algum era mídia gerada via MCP, tira também a entrada das bibliotecas
 * (senão a gaveta ficaria com um item apontando para arquivo inexistente). Chamado fora da thread principal.
 */
object ExclusaoDaMidiaComLimpezaDasBibliotecas {
    /** @return a resposta JSON da exclusão e se alguma biblioteca mudou (para a tela recarregar). */
    fun excluir(context: Context, urisJson: String?): Pair<String, Boolean> {
        val resposta = ExclusaoDeArquivoDeMidiaDoComponenteDaColecaoNoAparelho.excluirVarios(context.contentResolver, urisJson)
        val regra = MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao
        val sumiram = ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.parseUris(urisJson).filter { uri ->
            regra.ehDoMcp(uri) && runCatching { !File(java.net.URI(uri.trim())).exists() }.getOrDefault(false)
        }
        if (sumiram.isEmpty()) return resposta to false
        runCatching {
            val gravacaoDosVideos = GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo(context)
            val videos = gravacaoDosVideos.carregar()
            val restantes = regra.videosRestantes(videos.listar(), sumiram)
            if (restantes.size != videos.listar().size) {
                videos.substituirPor(restantes)
                gravacaoDosVideos.gravar(videos)
            }
            val gravacaoDasImagens = GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo(context)
            val imagens = gravacaoDasImagens.carregar()
            val imagensRestantes = regra.imagensRestantes(
                imagens.listar(),
                sumiram,
                File(context.filesDir, regra.PASTA_DAS_IMAGENS).absolutePath,
            )
            if (imagensRestantes.size != imagens.listar().size) {
                imagens.substituirPor(imagensRestantes)
                gravacaoDasImagens.gravar(imagens)
            }
        }
        return resposta to true
    }
}
