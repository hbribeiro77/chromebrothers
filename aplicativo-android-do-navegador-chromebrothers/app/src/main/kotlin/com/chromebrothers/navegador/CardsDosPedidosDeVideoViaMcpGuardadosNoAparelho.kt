package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.CardDoPedidoDeVideoViaMcp
import com.chromebrothers.navegador.regras.MensagemDeFalhaDeRedeNaGeracaoMcp
import com.chromebrothers.navegador.regras.FilaDosCardsDosPedidosDeVideoViaMcpComEstadoPorCard as Fila
import java.io.File
import java.util.UUID

/**
 * Cards dos pedidos via MCP, em memória e num JSON em filesDir (prontos inclusive, até o limite da fila).
 * Vídeo e imagem usam o mesmo formato, cada um no seu arquivo.
 */
class CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho(
    context: Context,
    nomeDoArquivo: String = ARQUIVO_DE_VIDEO,
) {
    private val arquivo = File(context.filesDir, nomeDoArquivo)
    private val trava = Any()
    private var carregado = false
    private var ultimoNumero = 0
    private var cards: List<CardDoPedidoDeVideoViaMcp> = emptyList()

    fun listar(): List<CardDoPedidoDeVideoViaMcp> = synchronized(trava) {
        carregarSePreciso()
        cards
    }

    fun obter(id: String): CardDoPedidoDeVideoViaMcp? = listar().firstOrNull { it.id == id }

    fun criar(
        quantidade: Int,
        mcps: List<String>,
        modeloDoMcp: (String) -> String,
        prompt: String,
        idDaReceita: String = "",
    ): List<CardDoPedidoDeVideoViaMcp> = synchronized(trava) {
        carregarSePreciso()
        val novos = Fila.novosCards(
            ultimoNumero = ultimoNumero,
            quantidade = quantidade,
            mcps = mcps,
            modeloDoMcp = modeloDoMcp,
            prompt = prompt,
            agoraMs = System.currentTimeMillis(),
            gerarId = { UUID.randomUUID().toString().take(12) },
            idDaReceita = idDaReceita,
        )
        ultimoNumero = novos.maxOfOrNull { it.numero } ?: ultimoNumero
        cards = Fila.aparar(cards + novos)
        gravar()
        novos
    }

    fun atualizar(card: CardDoPedidoDeVideoViaMcp): CardDoPedidoDeVideoViaMcp = synchronized(trava) {
        carregarSePreciso()
        // Erro de rede cru vira instrução: sem tarefa → Tentar de novo; com tarefa → Verificar.
        val ajustado = if (card.status == Fila.STATUS_FALHOU || card.status == Fila.STATUS_DEMOROU) {
            card.copy(mensagem = MensagemDeFalhaDeRedeNaGeracaoMcp.amigavel(card.mensagem, card.idDaTarefa.isNotBlank()))
        } else {
            card
        }
        cards = Fila.aparar(Fila.atualizar(cards, ajustado))
        gravar()
        ajustado
    }

    fun remover(id: String) = synchronized(trava) {
        carregarSePreciso()
        cards = Fila.remover(cards, id)
        gravar()
    }

    private fun carregarSePreciso() {
        if (carregado) return
        carregado = true
        if (!arquivo.isFile) return
        val lido = Fila.deJson(runCatching { arquivo.readText() }.getOrDefault(""))
        ultimoNumero = lido.ultimoNumero
        cards = Fila.aoRestaurar(lido.cards)
    }

    companion object {
        const val ARQUIVO_DE_VIDEO = "cards-dos-pedidos-de-video-via-mcp.json"
        const val ARQUIVO_DE_IMAGEM = "cards-dos-pedidos-de-imagem-via-mcp.json"
    }

    private fun gravar() {
        runCatching {
            arquivo.parentFile?.mkdirs()
            arquivo.writeText(Fila.paraJson(cards, ultimoNumero))
        }
    }
}
