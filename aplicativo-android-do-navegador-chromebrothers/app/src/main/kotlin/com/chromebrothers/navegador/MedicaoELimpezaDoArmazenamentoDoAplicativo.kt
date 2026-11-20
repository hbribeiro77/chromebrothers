package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.ArmazenamentoDoAplicativo
import com.chromebrothers.navegador.regras.ArquivoMedido
import com.chromebrothers.navegador.regras.CategoriaDoArmazenamento
import com.chromebrothers.navegador.regras.FilaDosCardsDosPedidosDeVideoViaMcpComEstadoPorCard
import com.chromebrothers.navegador.regras.ReceitaDeGeracaoMcp
import com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
import com.chromebrothers.navegador.regras.TotalDoArmazenamento
import java.io.File

/**
 * Mede o espaço do app por categoria e faz as limpezas seguras da tela de Armazenamento.
 * Tudo aqui é bloqueante (disco): chamar fora da thread principal, salvo o indicado.
 */
class MedicaoELimpezaDoArmazenamentoDoAplicativo(private val context: Context) {

    data class Medicao(val total: Long, val porCategoria: LinkedHashMap<CategoriaDoArmazenamento, TotalDoArmazenamento>)

    /** Arquivos que dá para apagar e quanto liberam. */
    data class Liberavel(val arquivos: List<File>, val bytes: Long) {
        val quantidade: Int get() = arquivos.size
    }

    private val dados: File = context.dataDir
    private val files: File = context.filesDir
    private val cache: File = context.cacheDir

    fun medir(): Medicao {
        val medidos = mutableListOf<ArquivoMedido>()
        dados.listFiles()?.filter { it.name != "lib" }?.forEach { topo ->
            topo.walkTopDown().onFail { _, _ -> }.filter { it.isFile }.forEach { arquivo ->
                medidos += ArquivoMedido(arquivo.relativeTo(dados).path.replace('\\', '/'), arquivo.length())
            }
        }
        return Medicao(medidos.sumOf { it.bytes }, ArmazenamentoDoAplicativo.agrupar(medidos))
    }

    // ---------- Referências sem uso ----------

    /**
     * Refs das receitas que nenhuma receita viva usa, receitas órfãs, refs do formato antigo que nenhum item
     * lista e miniaturas de cards que já saíram. Receita viva = apontada por um card ou item da biblioteca.
     * Nada com menos de [margemMs] (uma geração em andamento grava refs antes dos cards).
     */
    fun referenciasSemUso(margemMs: Long = DEZ_MINUTOS, somenteMaisAntigasQueDias: Int = 0): Liberavel {
        val agora = System.currentTimeMillis()
        fun pode(arquivo: File) = agora - arquivo.lastModified() > margemMs &&
            (somenteMaisAntigasQueDias <= 0 || ArmazenamentoDoAplicativo.antigo(arquivo.lastModified(), agora, somenteMaisAntigasQueDias))

        val cards = lerCards()
        val videos = runCatching { GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo(context).carregar().listar() }.getOrDefault(emptyList())
        val imagens = runCatching { GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo(context).carregar().listar() }.getOrDefault(emptyList())
        val idsVivos = (cards.map { it.idDaReceita } + videos.map { it.idDaReceita } + imagens.map { it.idDaReceita })
            .filter { it.isNotBlank() }.toSet()

        val pastaDasReceitas = File(files, ReceitasDeGeracaoMcpGuardadasNoAparelho.PASTA_DAS_RECEITAS)
        val arquivosDeReceita = pastaDasReceitas.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }.orEmpty()
        val receitas = arquivosDeReceita.mapNotNull { a -> runCatching { ReceitaDeGeracaoMcp.deJson(a.readText()) }.getOrNull() }
        val vivas = receitas.filter { it.id in idsVivos }
        val orfas = ArmazenamentoDoAplicativo.receitasOrfas(arquivosDeReceita.map { it.nameWithoutExtension }.toSet(), idsVivos)

        val apagar = mutableListOf<File>()
        arquivosDeReceita.filter { it.nameWithoutExtension in orfas && pode(it) }.forEach { apagar += it }

        val pastaDasRefs = File(files, ReceitasDeGeracaoMcpGuardadasNoAparelho.PASTA_DAS_REFS)
        val refs = pastaDasRefs.listFiles()?.filter { it.isFile }.orEmpty()
        val livres = ArmazenamentoDoAplicativo.refsDeReceitaSemUso(refs.map { it.name }.toSet(), vivas)
        refs.filter { it.name in livres && pode(it) }.forEach { apagar += it }

        val pastaAntiga = File(files, "refs-de-geracao-da-biblioteca")
        val antigas = pastaAntiga.listFiles()?.filter { it.isFile }.orEmpty()
        val listadas = (videos.flatMap { it.refs } + imagens.flatMap { it.refs }).toSet()
        val antigasLivres = ArmazenamentoDoAplicativo.refsAntigasSemUso(antigas.map { it.name }.toSet(), listadas)
        antigas.filter { it.name in antigasLivres && pode(it) }.forEach { apagar += it }

        val pastaDasMiniaturas = File(files, "miniaturas-dos-cards-mcp")
        val miniaturasEmUso = cards.map { File(it.caminhoMiniatura).name }.toSet()
        pastaDasMiniaturas.listFiles()?.filter { it.isFile && it.name !in miniaturasEmUso && pode(it) }?.forEach { apagar += it }

        return Liberavel(apagar, apagar.sumOf { it.length() })
    }

    // ---------- Cache ----------

    /**
     * Cache do próprio app (miniaturas, downloads temporários, capturas, diagnósticos). Fica de fora o que os
     * motores do navegador gerenciam (limpos pela API deles em [limparCacheDosMotores]) e os vídeos de
     * referência dos slots abertos há menos de um dia.
     */
    fun cacheProprio(somenteMaisAntigoQueDias: Int = 0): Liberavel {
        val agora = System.currentTimeMillis()
        val apagar = mutableListOf<File>()
        cache.listFiles()?.forEach { item ->
            if (item.name in PROTEGIDOS_NO_CACHE || item.name.endsWith(".default")) return@forEach
            item.walkTopDown().onFail { _, _ -> }.filter { it.isFile }.forEach { arquivo ->
                val recente = agora - arquivo.lastModified() < UM_DIA
                if (item.name == "referencias-de-video" && recente) return@forEach
                if (somenteMaisAntigoQueDias > 0 && !ArmazenamentoDoAplicativo.antigo(arquivo.lastModified(), agora, somenteMaisAntigoQueDias)) return@forEach
                apagar += arquivo
            }
        }
        return Liberavel(apagar, apagar.sumOf { it.length() })
    }

    /** Cache HTTP/imagens do Gecko e do WebView, pelos próprios motores. Chamar na thread principal. */
    fun limparCacheDosMotores() {
        runCatching {
            MotorDoFirefoxDoNavegadorChromebrothers.obter(context).storageController.clearData(
                org.mozilla.geckoview.StorageController.ClearFlags.NETWORK_CACHE or
                    org.mozilla.geckoview.StorageController.ClearFlags.IMAGE_CACHE,
            )
        }
        runCatching { android.webkit.WebView(context).apply { clearCache(true); destroy() } }
    }

    // ---------- Rascunhos ----------

    /** Rascunhos do editor parados há mais de [dias]; o mais recente nunca entra (pode estar aberto). */
    fun rascunhosAntigos(dias: Int = 30): Pair<List<String>, Long> {
        val gravacao = GravacaoDoRascunhoDaEdicaoDeImagem(context)
        val agora = System.currentTimeMillis()
        val ids = gravacao.listar().drop(1).filter { ArmazenamentoDoAplicativo.antigo(gravacao.instante(it), agora, dias) }
        val pasta = File(files, "rascunhos-da-edicao-de-imagem")
        val bytes = ids.sumOf { id -> File(pasta, id).walkTopDown().filter { it.isFile }.sumOf { it.length() } }
        return ids to bytes
    }

    fun apagarRascunhos(ids: List<String>) {
        val gravacao = GravacaoDoRascunhoDaEdicaoDeImagem(context)
        ids.forEach { runCatching { gravacao.apagar(it) } }
    }

    // ---------- Comum ----------

    /** Apaga e devolve quanto liberou de fato. */
    fun apagar(liberavel: Liberavel): Long =
        liberavel.arquivos.sumOf { arquivo ->
            val tamanho = arquivo.length()
            if (runCatching { arquivo.delete() }.getOrDefault(false)) tamanho else 0L
        }

    /** Mídias marcadas como favoritas (file:// como na lista do visualizador): nunca entram em "Selecionar tudo". */
    fun urisFavoritas(): Set<String> {
        val videos = runCatching { GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo(context).carregar().favoritos() }.getOrDefault(emptyList())
        val imagens = runCatching { GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo(context).carregar().favoritos() }.getOrDefault(emptyList())
        val pastaDasImagens = File(files, "biblioteca-de-imagens-capturadas").absolutePath
        return (
            videos.mapNotNull { it.caminhoLocal.takeIf { c -> c.isNotBlank() } } +
                imagens.map { it.caminhoLocal.ifBlank { "$pastaDasImagens/${it.nomeDoArquivo}" } }
            ).map { RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho(it) }.toSet()
    }

    // ---------- Limpeza automática ----------

    fun diasDaLimpezaAutomatica(): Int = preferencias().getInt(CHAVE_DIAS, 0)

    fun definirDiasDaLimpezaAutomatica(dias: Int) {
        preferencias().edit().putInt(CHAVE_DIAS, dias).apply()
    }

    /** Ao abrir o app: refs sem uso e cache próprio mais antigos que o prazo escolhido; nunca mídia nem favoritos. */
    fun rodarLimpezaAutomaticaSeVencida() {
        val dias = diasDaLimpezaAutomatica()
        val agora = System.currentTimeMillis()
        if (!ArmazenamentoDoAplicativo.deveRodarLimpezaAutomatica(dias, preferencias().getLong(CHAVE_ULTIMA, 0L), agora)) return
        preferencias().edit().putLong(CHAVE_ULTIMA, agora).apply()
        runCatching { apagar(referenciasSemUso(somenteMaisAntigasQueDias = dias)) }
        runCatching { apagar(cacheProprio(somenteMaisAntigoQueDias = dias)) }
    }

    private fun preferencias() = context.getSharedPreferences("armazenamento-do-aplicativo", Context.MODE_PRIVATE)

    private fun lerCards() = listOf(
        CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho.ARQUIVO_DE_VIDEO,
        CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho.ARQUIVO_DE_IMAGEM,
    ).flatMap { nome ->
        val arquivo = File(files, nome)
        if (!arquivo.isFile) emptyList()
        else runCatching { FilaDosCardsDosPedidosDeVideoViaMcpComEstadoPorCard.deJson(arquivo.readText()).cards }.getOrDefault(emptyList())
    }

    private companion object {
        const val DEZ_MINUTOS = 10 * 60 * 1000L
        const val UM_DIA = 24 * 60 * 60 * 1000L
        const val CHAVE_DIAS = "dias-da-limpeza-automatica"
        const val CHAVE_ULTIMA = "ultima-limpeza-automatica"
        /** Gerenciados pelos motores do navegador: limpos só pela API deles. */
        val PROTEGIDOS_NO_CACHE = setOf("WebView", "gecko_temp", "Crash Reports", "org.chromium.android_webview")
    }
}
