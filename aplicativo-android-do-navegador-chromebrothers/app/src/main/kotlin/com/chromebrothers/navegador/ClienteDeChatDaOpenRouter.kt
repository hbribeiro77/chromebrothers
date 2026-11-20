package com.chromebrothers.navegador

import android.os.Handler
import android.os.Looper
import com.chromebrothers.navegador.regras.AcumuladorDeChamadasDeFerramenta
import com.chromebrothers.navegador.regras.CatalogoDeModelosPorFamiliaDaOpenRouter
import com.chromebrothers.navegador.regras.FimDoEnvioDoChat
import com.chromebrothers.navegador.regras.MensagemDeChat
import com.chromebrothers.navegador.regras.ModeloListadoDaOpenRouter
import com.chromebrothers.navegador.regras.PedidoDeChatParaAOpenRouter
import com.chromebrothers.navegador.regras.TextoJsonDoChat
import com.chromebrothers.navegador.regras.TrechosDaRespostaEmFluxoDaOpenRouter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class ClienteDeChatDaOpenRouter {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()
    private val httpDaLista = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val principal = Handler(Looper.getMainLooper())
    private var chamada: Call? = null

    fun enviar(
        chave: String,
        modelo: String,
        mensagens: List<MensagemDeChat>,
        comFerramentas: Boolean = false,
        somenteLeitura: Boolean = false,
        aoTrecho: (String) -> Unit,
        aoTerminar: (FimDoEnvioDoChat) -> Unit,
    ) {
        cancelar()
        val corpo = PedidoDeChatParaAOpenRouter.corpo(modelo, mensagens, comFerramentas, somenteLeitura)
        val pedido = Request.Builder()
            .url(PedidoDeChatParaAOpenRouter.URL)
            .post(corpo.toRequestBody("application/json".toMediaType()))
        PedidoDeChatParaAOpenRouter.cabecalhos(chave).forEach { (nome, valor) ->
            pedido.header(nome, valor)
        }
        val call = http.newCall(pedido.build())
        chamada = call
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (call.isCanceled()) return
                principal.post { aoTerminar(FimDoEnvioDoChat(e.message ?: "Sem conexão com a OpenRouter.")) }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { ler(call, it, aoTrecho, aoTerminar) }
            }
        })
    }

    fun cancelar() {
        chamada?.cancel()
        chamada = null
    }

    private fun ler(
        call: Call,
        response: Response,
        aoTrecho: (String) -> Unit,
        aoTerminar: (FimDoEnvioDoChat) -> Unit,
    ) {
        val brutoInicial = if (!response.isSuccessful) response.body?.string().orEmpty() else null
        if (brutoInicial != null) {
            val mensagem = TextoJsonDoChat.valorDaChave(brutoInicial, "message")
                ?: "A OpenRouter respondeu ${response.code}."
            principal.post { aoTerminar(FimDoEnvioDoChat(mensagem)) }
            return
        }
        val tipo = response.header("Content-Type").orEmpty()
        val corpo = response.body ?: run {
            principal.post { aoTerminar(FimDoEnvioDoChat("A OpenRouter não devolveu texto.")) }
            return
        }
        if (!tipo.contains("text/event-stream")) {
            val leitura = TrechosDaRespostaEmFluxoDaOpenRouter.lerDados(corpo.string())
            if (leitura.falha != null) {
                val falha = leitura.falha
                principal.post { aoTerminar(FimDoEnvioDoChat(falha)) }
                return
            }
            val acumulado = AcumuladorDeChamadasDeFerramenta()
            leitura.chamadas.forEach(acumulado::acrescentar)
            val chamadas = acumulado.listar()
            val texto = leitura.texto
            if (texto != null) {
                principal.post {
                    aoTrecho(texto)
                    aoTerminar(FimDoEnvioDoChat(chamadas = chamadas, uso = leitura.uso))
                }
            } else if (chamadas.isNotEmpty()) {
                principal.post { aoTerminar(FimDoEnvioDoChat(chamadas = chamadas, uso = leitura.uso)) }
            } else {
                principal.post { aoTerminar(FimDoEnvioDoChat("Resposta inesperada da OpenRouter.")) }
            }
            return
        }
        val texto = StringBuilder()
        val chamadas = AcumuladorDeChamadasDeFerramenta()
        var uso: com.chromebrothers.navegador.regras.UsoDoChat? = null
        val fonte = corpo.source()
        while (!call.isCanceled()) {
            val linha = fonte.readUtf8Line() ?: break
            val leitura = TrechosDaRespostaEmFluxoDaOpenRouter.lerLinha(linha) ?: continue
            if (leitura.falha != null) {
                val falha = leitura.falha
                principal.post { aoTerminar(FimDoEnvioDoChat(falha)) }
                return
            }
            if (leitura.fim) break
            leitura.uso?.let { uso = it }
            leitura.chamadas.forEach(chamadas::acrescentar)
            val trecho = leitura.texto
            if (trecho != null) {
                texto.append(trecho)
                val parcial = texto.toString()
                principal.post { aoTrecho(parcial) }
            }
        }
        if (!call.isCanceled()) {
            val finais = chamadas.listar()
            val usoFinal = uso
            principal.post { aoTerminar(FimDoEnvioDoChat(chamadas = finais, uso = usoFinal)) }
        }
    }

    fun listarModelos(
        chave: String,
        aoReceber: (List<ModeloListadoDaOpenRouter>, String?) -> Unit,
    ) {
        Thread {
            try {
                val todos = mutableListOf<ModeloListadoDaOpenRouter>()
                var offset = 0
                while (offset <= 10000) {
                    val pedido = Request.Builder()
                        .url(CatalogoDeModelosPorFamiliaDaOpenRouter.url(offset))
                        .get()
                    PedidoDeChatParaAOpenRouter.cabecalhos(chave).forEach { (nome, valor) ->
                        pedido.header(nome, valor)
                    }
                    val resposta = httpDaLista.newCall(pedido.build()).execute()
                    val pagina = resposta.use { lerPagina(it) }
                    if (pagina.erro != null) {
                        principal.post { aoReceber(emptyList(), pagina.erro) }
                        return@Thread
                    }
                    todos += pagina.modelos
                    if (pagina.quantidadeBruta < CatalogoDeModelosPorFamiliaDaOpenRouter.LIMITE_DA_PAGINA) break
                    offset += CatalogoDeModelosPorFamiliaDaOpenRouter.LIMITE_DA_PAGINA
                }
                val unicos = todos.distinctBy { it.id }
                principal.post { aoReceber(unicos, null) }
            } catch (e: IOException) {
                principal.post { aoReceber(emptyList(), e.message ?: "Sem conexão com a OpenRouter.") }
            }
        }.start()
    }

    private data class PaginaLida(
        val modelos: List<ModeloListadoDaOpenRouter>,
        val quantidadeBruta: Int,
        val erro: String?,
    )

    private fun lerPagina(response: Response): PaginaLida {
        val bruto = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            val mensagem = TextoJsonDoChat.valorDaChave(bruto, "message")
                ?: "A OpenRouter respondeu ${response.code}."
            return PaginaLida(emptyList(), 0, mensagem)
        }
        val pagina = CatalogoDeModelosPorFamiliaDaOpenRouter.pagina(bruto)
        return PaginaLida(pagina.modelos, pagina.quantidadeBruta, null)
    }
}
