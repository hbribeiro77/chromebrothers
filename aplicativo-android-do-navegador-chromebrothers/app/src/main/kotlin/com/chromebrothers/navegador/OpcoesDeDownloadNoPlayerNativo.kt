package com.chromebrothers.navegador

import android.content.Intent
import com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar
import com.chromebrothers.navegador.regras.PedidoDeMidiaObservado
import com.chromebrothers.navegador.regras.TipoDePedidoDeMidiaParaDownload

/**
 * Leva as opções de vídeo detectadas na página para o player nativo (tela própria), para o botão de
 * download dele escolher a melhor (toque) ou mostrar a lista com resolução/duração/tamanho (segurar).
 */
object OpcoesDeDownloadNoPlayerNativo {
    private const val ENDERECOS = "opcoes_de_download_enderecos"
    private const val TIPOS = "opcoes_de_download_tipos"
    private const val TAMANHOS = "opcoes_de_download_tamanhos"
    private const val MIMES = "opcoes_de_download_mimes"
    private const val LIMITE = 40

    fun colocar(intent: Intent, pedidos: List<PedidoDeMidiaObservado>) {
        // Vídeos da lista + os áudios separados (para juntar quando o vídeo vem mudo).
        val uteis = (EscolhaDoPedidoDeMidiaParaBaixar.listar(pedidos) +
            listOfNotNull(EscolhaDoPedidoDeMidiaParaBaixar.melhorAudio(pedidos)))
            .distinctBy { it.endereco }
            .take(LIMITE)
        if (uteis.isEmpty()) return
        intent.putExtra(ENDERECOS, uteis.map { it.endereco }.toTypedArray())
        intent.putExtra(TIPOS, uteis.map { it.tipo.name }.toTypedArray())
        intent.putExtra(TAMANHOS, uteis.map { it.tamanho }.toLongArray())
        intent.putExtra(MIMES, uteis.map { it.mime }.toTypedArray())
    }

    fun ler(intent: Intent?): List<PedidoDeMidiaObservado> {
        val enderecos = intent?.getStringArrayExtra(ENDERECOS) ?: return emptyList()
        val tipos = intent.getStringArrayExtra(TIPOS) ?: return emptyList()
        val tamanhos = intent.getLongArrayExtra(TAMANHOS) ?: LongArray(enderecos.size) { -1L }
        val mimes = intent.getStringArrayExtra(MIMES) ?: Array(enderecos.size) { "" }
        return enderecos.indices.mapNotNull { i ->
            val tipo = runCatching { TipoDePedidoDeMidiaParaDownload.valueOf(tipos.getOrNull(i).orEmpty()) }.getOrNull()
                ?: return@mapNotNull null
            PedidoDeMidiaObservado(
                endereco = enderecos[i],
                tipo = tipo,
                ordem = (enderecos.size - i).toLong(),
                tamanho = tamanhos.getOrElse(i) { -1L },
                mime = mimes.getOrNull(i).orEmpty(),
            )
        }
    }
}
