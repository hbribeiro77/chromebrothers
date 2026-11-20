package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.chromebrothers.navegador.regras.AjusteDoPedidoAoModeloDoMagnific
import com.chromebrothers.navegador.regras.CardDoPedidoDeVideoViaMcp
import com.chromebrothers.navegador.regras.CatalogoDeModelosDoMagnific
import com.chromebrothers.navegador.regras.CatalogoDeModelosDeVideoDoTopviewNaColecao
import com.chromebrothers.navegador.regras.CatalogoDeModelosDeVideoDoVivixNaColecao
import com.chromebrothers.navegador.regras.FilaDosCardsDosPedidosDeVideoViaMcpComEstadoPorCard as Fila
import com.chromebrothers.navegador.regras.EquivalenciaDeModelosDeImagemEntreMcps
import com.chromebrothers.navegador.regras.EquivalenciaDeModelosDeVideoEntreMcps
import com.chromebrothers.navegador.regras.ImagemGeradaNaColecao
import com.chromebrothers.navegador.regras.GeracaoDeImagemEVideoPeloHiggsfieldNoChat
import com.chromebrothers.navegador.regras.GeracaoDeVideoPeloTopviewNoChat
import com.chromebrothers.navegador.regras.ListagemDeModelosDoHiggsfieldNoChat
import com.chromebrothers.navegador.regras.PedidoDeGeracaoDeImagemViaMcpNaColecao
import com.chromebrothers.navegador.regras.PedidoDeGeracaoDeVideoViaMcpNaColecao
import com.chromebrothers.navegador.regras.ReceitaDeGeracaoMcp
import com.chromebrothers.navegador.regras.ReferenciaDeVideoNaGeracaoMcp
import com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoDeVideoMcp
import com.chromebrothers.navegador.regras.RespostaJsonDaPonteDeGeracaoMcp
import com.chromebrothers.navegador.regras.ResultadoDoPedidoDeGeracaoDeImagemViaMcp
import com.chromebrothers.navegador.regras.ResultadoDoPedidoDeGeracaoDeVideoViaMcp
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Fachada usada pela ponte JS da coleção: lista MCPs/modelos, guarda até 4 imagens de referência
 * e gera N imagens em background, devolvendo ids da biblioteca + preview JPEG pequeno.
 */
class ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers(
    private val cliente: ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers,
    private val sessao: SessaoDaContaHiggsfieldNoAparelho,
    private val gravacao: GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers,
    private val clienteSjinn: ClienteApiDoSjinnParaGeracaoDeImagemNoChromebrothers,
    private val sessaoSjinn: SessaoDaContaSjinnNoAparelho,
    private val clienteTopview: ClienteMcpDoTopviewParaGeracaoNoChromebrothers,
    private val sessaoTopview: SessaoDaContaTopviewNoAparelho,
    private val cardsDeVideo: CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho,
    private val cardsDeImagem: CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho,
    private val catalogos: CatalogosDeModelosDosMcpsGuardadosNoAparelho,
    private val clienteMagnific: ClienteMcpDoMagnificParaGeracaoNoChromebrothers,
    private val sessaoMagnific: SessaoDaContaMagnificNoAparelho,
    private val receitas: ReceitasDeGeracaoMcpGuardadasNoAparelho,
    private val clienteVivix: ClienteApiDoVivixParaGeracaoDeVideoNoChromebrothers,
    private val sessaoVivix: SessaoDaContaVivixNoAparelho,
    /** Miniaturas JPEG dos cards de imagem prontos (o card volta com prévia ao reabrir o gerador). */
    private val pastaDasMiniaturasDosCards: File,
    /** `content://` ainda existe? (card cuja mídia foi movida para a Galeria). */
    private val existeConteudo: (String) -> Boolean = { true },
) {
    /**
     * Imagem: [bytes] é a própria imagem. Vídeo: [bytes] é um quadro JPEG (prévia/metadados)
     * e o vídeo fica em [video] (cópia em cache, apagada ao limpar).
     */
    private class Referencia(
        val bytes: ByteArray,
        val nome: String,
        val mime: String,
        val video: File? = null,
        val duracaoMs: Long = 0L,
        /** false quando [video] é a cópia permanente da receita ("Usar de novo"): limpar o slot não apaga. */
        val apagarAoLimpar: Boolean = true,
    ) {
        val ehVideo: Boolean get() = video != null
    }

    private fun apagarArquivoDoSlot(ref: Referencia) {
        if (ref.apagarAoLimpar) ref.video?.let { runCatching { it.delete() } }
    }

    private val trava = Any()

    /**
     * Uma lista de referências por gerador ("imagem" / "video"): antes era uma só, e o que entrava num
     * gerador aparecia no outro (ou voltava escondido ao reabrir).
     */
    private val referenciasPorTipo = mapOf(
        ReceitaDeGeracaoMcp.TIPO_IMAGEM to mutableListOf<Referencia>(),
        ReceitaDeGeracaoMcp.TIPO_VIDEO to mutableListOf<Referencia>(),
    )

    /** Tipo desconhecido cai no de imagem. Chamar com [trava]. */
    private fun referencias(tipo: String): MutableList<Referencia> =
        referenciasPorTipo[tipo.trim().lowercase()] ?: referenciasPorTipo.getValue(ReceitaDeGeracaoMcp.TIPO_IMAGEM)

    /** MCPs e modelo escolhidos agora no componente de vídeo (informados pelo HTML). */
    @Volatile private var mcpsDoVideoAtual: List<String> = emptyList()
    @Volatile private var modeloDoVideoAtual: String = ""

    /** Guarda a seleção do componente de vídeo e diz se ela aceita vídeo de referência. */
    fun definirModeloDeVideoAtual(mcpsCsv: String, modelo: String): String {
        val mcps = mcpsCsv.split(',', ' ').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()
        mcpsDoVideoAtual = mcps
        modeloDoVideoAtual = modelo.trim()
        val aceita = ReferenciaDeVideoNaGeracaoMcp.aceitaEmTodos(mcps, modelo)
        val dica = ReferenciaDeVideoNaGeracaoMcp.dica(mcps, modelo)
        return org.json.JSONObject().put("ok", true).put("aceitaVideo", aceita).put("dica", dica).toString()
    }

    fun aceitaVideoNoModeloAtual(): Boolean =
        ReferenciaDeVideoNaGeracaoMcp.aceitaEmTodos(mcpsDoVideoAtual, modeloDoVideoAtual)

    /** Acrescenta um vídeo de referência (já copiado para o cache do app). */
    fun adicionarReferenciaDeVideo(video: File, nome: String, quadroJpeg: ByteArray, duracaoMs: Long): String {
        synchronized(trava) {
            // Vídeo de referência só existe no gerador de vídeo.
            val referencias = referencias(ReceitaDeGeracaoMcp.TIPO_VIDEO)
            if (referencias.size >= PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS) {
                runCatching { video.delete() }
                return RespostaJsonDaPonteDeGeracaoMcp.erro(
                    "No máximo ${PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS} referências.",
                )
            }
            if (referencias.count { it.ehVideo } >= ReferenciaDeVideoNaGeracaoMcp.LIMITE_DE_VIDEOS) {
                runCatching { video.delete() }
                return RespostaJsonDaPonteDeGeracaoMcp.erro(
                    "No máximo ${ReferenciaDeVideoNaGeracaoMcp.LIMITE_DE_VIDEOS} vídeos de referência.",
                )
            }
            referencias += Referencia(quadroJpeg, nome.ifBlank { video.name }, "video/mp4", video, duracaoMs)
            return jsonDasReferenciasLocked(ReceitaDeGeracaoMcp.TIPO_VIDEO)
        }
    }

    fun quantidadeDeVideosDeReferencia(): Int =
        synchronized(trava) { referencias(ReceitaDeGeracaoMcp.TIPO_VIDEO).count { it.ehVideo } }

    fun listarMcpsJson(): String = RespostaJsonDaPonteDeGeracaoMcp.okMcps()

    fun listarMcpsDeVideoJson(): String = RespostaJsonDaPonteDeGeracaoDeVideoMcp.okMcps()

    fun listarModelosDeVideoJson(mcpId: String): String {
        val ids = mcpId.split(',', ' ')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
        if (ids.isEmpty()) {
            return listarModelosDeVideoDeUmMcp(PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_SJINN)
        }
        if (ids.size >= 2) {
            val desconhecido = ids.firstOrNull { it !in PedidoDeGeracaoDeVideoViaMcpNaColecao.MCPS_CONHECIDOS }
            if (desconhecido != null) {
                return RespostaJsonDaPonteDeGeracaoMcp.erro("MCP não suportado: $desconhecido")
            }
            return EquivalenciaDeModelosDeVideoEntreMcps.jsonDaIntersecao(ids)
        }
        return listarModelosDeVideoDeUmMcp(ids.first())
    }

    private fun listarModelosDeVideoDeUmMcp(id: String): String {
        if (id !in PedidoDeGeracaoDeVideoViaMcpNaColecao.MCPS_CONHECIDOS) {
            return RespostaJsonDaPonteDeGeracaoMcp.erro("MCP não suportado: $id")
        }
        if (id == PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_HIGGSFIELD) {
            if (!sessao.temSessao()) {
                return RespostaJsonDaPonteDeGeracaoMcp.erro(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO)
            }
            return catalogoDeVideoDoHiggsfield.obter()
        }
        if (id == PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_MAGNIFIC) {
            if (!sessaoMagnific.temSessao()) return RespostaJsonDaPonteDeGeracaoMcp.erro(SessaoDaContaMagnificNoAparelho.SEM_SESSAO)
            return catalogoDeVideoDoMagnific.obter()
        }
        if (id == PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_VIVIX) {
            if (!sessaoVivix.temSessao()) return RespostaJsonDaPonteDeGeracaoMcp.erro(SessaoDaContaVivixNoAparelho.SEM_SESSAO)
            return CatalogoDeModelosDeVideoDoVivixNaColecao.jsonDosItensParaPonte()
        }
        if (id == PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_TOPVIEW) {
            if (!sessaoTopview.temSessao()) {
                return RespostaJsonDaPonteDeGeracaoMcp.erro(GeracaoDeVideoPeloTopviewNoChat.SEM_SESSAO)
            }
            return catalogoDeVideoDoTopview.obter()
        }
        if (!sessaoSjinn.temSessao()) {
            return RespostaJsonDaPonteDeGeracaoMcp.erro(SessaoDaContaSjinnNoAparelho.SEM_SESSAO)
        }
        return clienteSjinn.listarModelosDeVideoJson()
    }

    private fun modeloLocalParaMcp(modeloPedido: String, mcp: String): String {
        // Só traduz id de família (vários MCPs marcados). Com um MCP, o id veio da lista dele:
        // adivinhar pelo nome trocaria "Seedance 2.0 Fast" por "Seedance 2.0".
        val idFamilia = EquivalenciaDeModelosDeVideoEntreMcps.familiaPorId(modeloPedido)?.id
        val mapeado = idFamilia?.let { EquivalenciaDeModelosDeVideoEntreMcps.idParaMcp(it, mcp) }
        return when {
            mapeado != null -> mapeado
            mcp == PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_HIGGSFIELD ->
                GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloVideo(modeloPedido)
            mcp == PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_TOPVIEW ->
                CatalogoDeModelosDeVideoDoTopviewNaColecao.displayNameDe(modeloPedido)
            else -> modeloPedido
        }
    }

    /** Bloqueante (rede): chamar fora da thread principal. */
    fun listarModelosJson(mcpId: String): String {
        val ids = mcpId.split(',', ' ').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()
        if (ids.size >= 2) {
            val desconhecido = ids.firstOrNull { it !in PedidoDeGeracaoDeImagemViaMcpNaColecao.MCPS_CONHECIDOS }
            if (desconhecido != null) return RespostaJsonDaPonteDeGeracaoMcp.erro("MCP não suportado: $desconhecido")
            return EquivalenciaDeModelosDeImagemEntreMcps.jsonDaIntersecao(ids)
        }
        val id = ids.firstOrNull() ?: PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_HIGGSFIELD
        if (id !in PedidoDeGeracaoDeImagemViaMcpNaColecao.MCPS_CONHECIDOS) {
            return RespostaJsonDaPonteDeGeracaoMcp.erro("MCP não suportado: $id")
        }
        if (id == PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_SJINN) {
            if (!sessaoSjinn.temSessao()) {
                return RespostaJsonDaPonteDeGeracaoMcp.erro(SessaoDaContaSjinnNoAparelho.SEM_SESSAO)
            }
            return clienteSjinn.listarModelosJson()
        }
        if (id == PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_MAGNIFIC) {
            if (!sessaoMagnific.temSessao()) return RespostaJsonDaPonteDeGeracaoMcp.erro(SessaoDaContaMagnificNoAparelho.SEM_SESSAO)
            return catalogoDeImagemDoMagnific.obter()
        }
        if (!sessao.temSessao()) {
            return RespostaJsonDaPonteDeGeracaoMcp.erro(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO)
        }
        return catalogoDeImagemDoHiggsfield.obter()
    }

    private val catalogoDeImagemDoHiggsfield = catalogos.registrar(CATALOGO_HIGGSFIELD_IMAGEM) {
        try {
            ListagemDeModelosDoHiggsfieldNoChat.jsonDosItensParaPonte(cliente.listarModelosDeImagemBruto())
        } catch (e: Exception) {
            RespostaJsonDaPonteDeGeracaoMcp.erro(e.message ?: "Falha ao listar modelos.")
        }
    }

    /** Topview: lista vinda do próprio serviço; reserva = última lista boa guardada. */
    private val catalogoDeVideoDoTopview = catalogos.registrar(CATALOGO_TOPVIEW_VIDEO) {
        try {
            LeituraDosModelosDaConfiguracaoDoTopview.jsonParaPonte(clienteTopview.buscarConfiguracaoDeVideoBruta())
        } catch (e: Exception) {
            RespostaJsonDaPonteDeGeracaoMcp.erro(e.message ?: "Falha ao listar modelos do Topview.")
        }
    }

    private val catalogoDeVideoDoHiggsfield = catalogos.registrar(CATALOGO_HIGGSFIELD_VIDEO) {
        try {
            ListagemDeModelosDoHiggsfieldNoChat.jsonDosItensParaPonte(
                cliente.listarModelosDeVideoBruto(),
                idPreferido = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_VIDEO,
            )
        } catch (e: Exception) {
            RespostaJsonDaPonteDeGeracaoMcp.erro(e.message ?: "Falha ao listar modelos de vídeo.")
        }
    }

    /** Magnific: listas vindas do próprio serviço, com durações/resoluções/proporções de cada modelo. */
    private val catalogoDeImagemDoMagnific = catalogos.registrar(CATALOGO_MAGNIFIC_IMAGEM) {
        listaDoMagnific(ClienteMcpDoMagnificParaGeracaoNoChromebrothers.TOOL_IMAGES_MODELS_LIST) {
            CatalogoDeModelosDoMagnific.serveParaImagem(it)
        }
    }

    private val catalogoDeVideoDoMagnific = catalogos.registrar(CATALOGO_MAGNIFIC_VIDEO) {
        listaDoMagnific(ClienteMcpDoMagnificParaGeracaoNoChromebrothers.TOOL_VIDEO_MODELS_LIST) {
            CatalogoDeModelosDoMagnific.serveParaVideo(it)
        }
    }

    private fun listaDoMagnific(ferramenta: String, serve: (CatalogoDeModelosDoMagnific.Modelo) -> Boolean): String =
        try {
            val bruto = clienteMagnific.listarModelosBruto(ferramenta)
            val texto = runCatching {
                org.json.JSONObject(bruto).optJSONArray("content")?.optJSONObject(0)?.optString("text")
            }.getOrNull().orEmpty()
            val modelos = CatalogoDeModelosDoMagnific.ler(texto).filter(serve)
            if (modelos.isEmpty()) error("O Magnific não devolveu modelos.")
            CatalogoDeModelosDoMagnific.jsonParaPonte(modelos)
        } catch (e: Exception) {
            RespostaJsonDaPonteDeGeracaoMcp.erro(e.message ?: "Falha ao listar modelos do Magnific.")
        }

    /** Detalhes de um modelo do Magnific, lidos da lista guardada (sem rede se houver cache). */
    private class DetalheDoModeloMagnific(
        val duracoes: List<Int>,
        val resolucoes: List<String>,
        val proporcoes: List<String>,
        val aceitaImagem: Boolean,
    )

    private fun detalheDoModeloMagnific(json: String, slug: String): DetalheDoModeloMagnific? = runCatching {
        val itens = org.json.JSONObject(json).optJSONArray("itens") ?: return null
        for (i in 0 until itens.length()) {
            val it = itens.optJSONObject(i) ?: continue
            if (it.optString("id") != slug) continue
            fun textos(nome: String) = it.optJSONArray(nome)?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty()
            val duracoes = it.optJSONArray("duracoes")?.let { a -> (0 until a.length()).map { a.optInt(it) } }.orEmpty()
            return DetalheDoModeloMagnific(duracoes, textos("resolucoes"), textos("proporcoes"), it.optBoolean("aceitaImagem", true))
        }
        null
    }.getOrNull()

    /** Só busca catálogo de MCP com login feito (sem login a busca só daria erro). */
    private fun podeBuscarCatalogo(id: String): Boolean = when {
        id.startsWith("higgsfield") -> sessao.temSessao()
        id.startsWith("sjinn") -> sessaoSjinn.temSessao()
        id.startsWith("topview") -> sessaoTopview.temSessao()
        id.startsWith("magnific") -> sessaoMagnific.temSessao()
        else -> false
    }

    /** Início do app e abertura da gaveta do chat: busca só o que passou de 6 h, sem travar a tela. */
    fun preaquecerCatalogosEmSegundoPlano() {
        catalogos.atualizarVencidosEmSegundoPlano(::podeBuscarCatalogo)
    }

    /** Menu "Atualizar listas de modelos". [aoTerminar] roda fora da thread principal. */
    fun apagarEBuscarCatalogosDeNovo(aoTerminar: (ok: Int, total: Int) -> Unit) {
        catalogos.apagarEBuscarDeNovo(::podeBuscarCatalogo, aoTerminar)
    }

    /** Acrescenta uma referência (máx. 4) ao gerador [tipo]. Devolve JSON `{ok, itens, quantidade}` ou erro. */
    fun adicionarReferencia(tipo: String, bytes: ByteArray, nome: String, mime: String): String {
        synchronized(trava) {
            val referencias = referencias(tipo)
            if (referencias.size >= PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS) {
                return RespostaJsonDaPonteDeGeracaoMcp.erro(
                    "No máximo ${PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS} referências.",
                )
            }
            referencias += Referencia(
                bytes = bytes,
                nome = nome.ifBlank { "referencia.jpg" },
                mime = mime.ifBlank { "image/jpeg" },
            )
            return jsonDasReferenciasLocked(tipo)
        }
    }

    fun limparReferencia(tipo: String) {
        synchronized(trava) {
            val referencias = referencias(tipo)
            referencias.forEach(::apagarArquivoDoSlot)
            referencias.clear()
        }
    }

    fun limparReferenciaNoIndice(tipo: String, indice: Int): String {
        synchronized(trava) {
            val referencias = referencias(tipo)
            if (indice !in referencias.indices) {
                return RespostaJsonDaPonteDeGeracaoMcp.erro("Índice de referência inválido.")
            }
            apagarArquivoDoSlot(referencias.removeAt(indice))
            return jsonDasReferenciasLocked(tipo)
        }
    }

    fun quantidadeDeReferencias(tipo: String): Int = synchronized(trava) { referencias(tipo).size }

    /**
     * Um item por referência do gerador [tipo], na ordem dos slots: (é vídeo, `file://`), ou null se falhou
     * (o índice continua batendo com o do HTML).
     * Imagem: grava em [pasta] os bytes exatos que serão enviados (apaga as prévias anteriores deste tipo).
     * Vídeo: o próprio arquivo do slot. Bloqueante (disco): chamar fora da thread principal.
     */
    fun arquivosDasReferencias(tipo: String, pasta: File): List<Pair<Boolean, String>?> {
        val regra = com.chromebrothers.navegador.regras.PreviaDaReferenciaDaGeracaoMcpNoVisor
        val refs = synchronized(trava) { referencias(tipo).toList() }
        pasta.mkdirs()
        val prefixo = regra.prefixo(tipo)
        pasta.listFiles()?.filter { it.name.startsWith(prefixo) }?.forEach { runCatching { it.delete() } }
        val carimbo = System.currentTimeMillis()
        return refs.mapIndexed { i, ref ->
            val video = ref.video
            if (video != null) {
                return@mapIndexed if (video.isFile) true to video.toURI().toString() else null
            }
            val arquivo = File(pasta, regra.nomeDoArquivo(tipo, i, carimbo, regra.extensaoDoMime(mimeDaImagem(ref.bytes))))
            runCatching { arquivo.writeBytes(ref.bytes) }.getOrNull() ?: return@mapIndexed null
            false to arquivo.toURI().toString()
        }
    }

    /** O que está guardado agora para o gerador [tipo] (o HTML pinta isso ao abrir/reabrir). */
    fun jsonDasReferencias(tipo: String): String = synchronized(trava) { jsonDasReferenciasLocked(tipo) }

    private fun jsonDasReferenciasLocked(tipo: String): String {
        val referencias = referencias(tipo)
        val itens = org.json.JSONArray()
        referencias.forEach { ref ->
            val item = org.json.JSONObject()
                .put("previewDataUrl", previewDataUrl(ref.bytes))
                .put("nome", ref.nome)
            if (ref.ehVideo) {
                item.put("video", true)
                item.put("duracaoMs", ref.duracaoMs)
                item.put("duracao", ReferenciaDeVideoNaGeracaoMcp.duracaoCurta(ref.duracaoMs))
            }
            itens.put(item)
        }
        return org.json.JSONObject()
            .put("ok", true)
            .put("itens", itens)
            .put("quantidade", referencias.size)
            .put("videos", referencias.count { it.ehVideo })
            .toString()
    }

    private fun previewDataUrlDeArquivoJpeg(caminho: String?): String {
        if (caminho.isNullOrBlank()) return ""
        val arquivo = File(caminho)
        if (!arquivo.isFile) return ""
        return try {
            "data:image/jpeg;base64," + Base64.encodeToString(arquivo.readBytes(), Base64.NO_WRAP)
        } catch (_: Exception) {
            ""
        }
    }

    /** Preview JPEG de até 320px de largura, como data URL; vazio se não decodificar. */
    fun previewDataUrl(bytes: ByteArray): String {
        return try {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
            if (limites.outWidth <= 0 || limites.outHeight <= 0) return ""
            var amostra = 1
            while (limites.outWidth / (amostra * 2) >= LARGURA_MAXIMA_DO_PREVIEW) amostra *= 2
            val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
            val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opcoes) ?: return ""
            val reduzido = if (original.width > LARGURA_MAXIMA_DO_PREVIEW) {
                val altura = (original.height.toLong() * LARGURA_MAXIMA_DO_PREVIEW / original.width)
                    .toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(original, LARGURA_MAXIMA_DO_PREVIEW, altura, true)
            } else {
                original
            }
            val saida = ByteArrayOutputStream()
            reduzido.compress(Bitmap.CompressFormat.JPEG, 80, saida)
            if (reduzido !== original) reduzido.recycle()
            original.recycle()
            "data:image/jpeg;base64," + Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP)
        } catch (e: Throwable) {
            ""
        }
    }

    /**
     * Cria um card por imagem esperada (quantidade × MCPs) e devolve na hora `{ok, cards:[...]}`.
     * Cada card evolui em thread própria; [aoCard] recebe `{ok, card}` a cada mudança (em qualquer thread).
     * Mesmo fluxo do vídeo: enviando → gerando → pronto / demorou / falhou.
     */
    fun iniciarImagensPorCards(pedidoJson: String, aoCard: (String) -> Unit, aoTerminarCard: () -> Unit): String =
        iniciarImagens(pedidoJson, aoCard, aoTerminarCard, refsFixas = null, receitaExistente = null)

    private fun iniciarImagens(
        pedidoJson: String,
        aoCard: (String) -> Unit,
        aoTerminarCard: () -> Unit,
        refsFixas: List<Referencia>?,
        receitaExistente: ReceitaDeGeracaoMcp?,
    ): String {
        val pedido = when (val lido = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(pedidoJson)) {
            is ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado -> return RespostaJsonDaPonteDeGeracaoMcp.erro(lido.mensagem)
            is ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok -> lido.pedido
        }
        // Vídeos de referência só servem ao gerador de vídeo.
        val refs = (refsFixas ?: synchronized(trava) { referencias(ReceitaDeGeracaoMcp.TIPO_IMAGEM).toList() })
            .filterNot { it.ehVideo }
        if (pedido.temReferencia && refs.isEmpty()) {
            return RespostaJsonDaPonteDeGeracaoMcp.erro(
                "Escolha a imagem de referência antes de gerar (vídeos só valem no gerador de vídeo).",
            )
        }
        val listaRefs = if (pedido.temReferencia) refs.map { it.bytes to it.nome } else emptyList()
        val receita = receitaExistente ?: criarReceita(
            ReceitaDeGeracaoMcp.TIPO_IMAGEM,
            pedidoJson,
            if (pedido.temReferencia) refs else emptyList(),
        )
        val novos = cardsDeImagem.criar(
            quantidade = pedido.quantidade,
            mcps = pedido.mcps,
            modeloDoMcp = { pedido.modeloPara(it) },
            prompt = pedido.prompt,
            idDaReceita = receita?.id.orEmpty(),
        )
        val emitir: (CardDoPedidoDeVideoViaMcp, ImagemGeradaNaColecao?) -> Unit = { card, pronta ->
            aoCard("""{"ok":true,"card":${jsonDoCardDeImagem(card, pronta)}}""")
            if (card.status != Fila.STATUS_ENVIANDO && card.status != Fila.STATUS_GERANDO) aoTerminarCard()
        }
        for ((mcp, doMcp) in novos.groupBy { it.mcp }) {
            Thread {
                try {
                    gerarCardsDeImagemDoMcp(mcp, doMcp, pedido, listaRefs, emitir)
                } catch (e: Throwable) {
                    doMcp.forEach { card ->
                        val atual = cardsDeImagem.obter(card.id) ?: card
                        if (atual.status == Fila.STATUS_ENVIANDO || atual.status == Fila.STATUS_GERANDO) {
                            emitir(cardsDeImagem.atualizar(atual.copy(status = Fila.STATUS_FALHOU, mensagem = e.message ?: "Falha.")), null)
                        }
                    }
                }
            }.start()
        }
        return jsonDosCards(novos)
    }

    private fun gerarCardsDeImagemDoMcp(
        mcp: String,
        cards: List<CardDoPedidoDeVideoViaMcp>,
        pedido: PedidoDeGeracaoDeImagemViaMcpNaColecao,
        listaRefs: List<Pair<ByteArray, String>>,
        emitir: (CardDoPedidoDeVideoViaMcp, ImagemGeradaNaColecao?) -> Unit,
    ) {
        val falharTodos: (String) -> Unit = { msg ->
            cards.forEach { emitir(cardsDeImagem.atualizar(it.copy(status = Fila.STATUS_FALHOU, mensagem = msg)), null) }
        }
        mcpSemSessao(mcp)?.let { return falharTodos(it) }
        val modelo = pedido.modeloPara(mcp)
        if (mcp == PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_MAGNIFIC) {
            // Uma chamada com count = N; cada criação devolvida vira a tarefa de um card.
            val detalhe = detalheDoModeloMagnific(catalogoDeImagemDoMagnific.obter(), modelo)
            val ids = try {
                val refs = listaRefs.map { (bytes, nome) -> clienteMagnific.enviarArquivo(bytes, mimeDaImagem(bytes), nome) }
                clienteMagnific.pedirImagens(
                    prompt = pedido.prompt,
                    slug = modelo,
                    proporcao = AjusteDoPedidoAoModeloDoMagnific.proporcao(pedido.proporcao, detalhe?.proporcoes.orEmpty()),
                    resolucao = AjusteDoPedidoAoModeloDoMagnific.resolucao(pedido.resolucao, detalhe?.resolucoes.orEmpty()),
                    referencias = refs,
                    quantidade = cards.size,
                )
            } catch (e: Exception) {
                return falharTodos(e.message ?: "Falha ao pedir as imagens ao Magnific.")
            }
            cards.forEachIndexed { i, card ->
                val id = ids.getOrNull(i)
                if (id == null) {
                    emitir(cardsDeImagem.atualizar(card.copy(status = Fila.STATUS_FALHOU, mensagem = "O Magnific devolveu menos imagens.")), null)
                    return@forEachIndexed
                }
                val gerando = marcarImagemGerando(card, id, emitir)
                Thread {
                    val inicio = System.currentTimeMillis()
                    concluirCardDeImagem(
                        gerando,
                        clienteMagnific.esperarImagem(id),
                        inicio,
                        ClienteMcpDoMagnificParaGeracaoNoChromebrothers.TIMEOUT_IMAGEM_MS,
                        listaRefs,
                        emitir,
                    )
                }.start()
            }
            return
        }
        // Higgsfield e Sjinn: uma chamada por imagem, sem identificador para consultar depois.
        val ehSjinn = mcp == PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_SJINN
        cards.forEach { card ->
            Thread {
                val gerando = marcarImagemGerando(card, "", emitir)
                val inicio = System.currentTimeMillis()
                val resultado = if (ehSjinn) {
                    clienteSjinn.gerarImagem(
                        prompt = pedido.prompt,
                        modelo = modelo,
                        aspectRatio = pedido.proporcao,
                        resolution = pedido.resolucao,
                        referenciasLocais = listaRefs,
                    )
                } else {
                    cliente.gerarImagem(
                        prompt = pedido.prompt,
                        modelo = modelo,
                        referencias = listaRefs,
                        aspectRatio = pedido.proporcao,
                        resolution = pedido.resolucao,
                        quantidade = 1,
                    )
                }
                concluirCardDeImagem(gerando, resultado, inicio, ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers.TIMEOUT_MS, listaRefs, emitir)
            }.start()
        }
    }

    private fun marcarImagemGerando(
        card: CardDoPedidoDeVideoViaMcp,
        idDaTarefa: String,
        emitir: (CardDoPedidoDeVideoViaMcp, ImagemGeradaNaColecao?) -> Unit,
    ): CardDoPedidoDeVideoViaMcp {
        val gerando = cardsDeImagem.atualizar(card.copy(status = Fila.STATUS_GERANDO, idDaTarefa = idDaTarefa))
        emitir(gerando, null)
        return gerando
    }

    private fun concluirCardDeImagem(
        card: CardDoPedidoDeVideoViaMcp,
        resultado: ResultadoDaGeracaoMcpHiggsfield,
        inicioDaEsperaMs: Long,
        limiteMs: Long,
        listaRefs: List<Pair<ByteArray, String>>,
        emitir: (CardDoPedidoDeVideoViaMcp, ImagemGeradaNaColecao?) -> Unit,
    ) {
        when (resultado) {
            is ResultadoDaGeracaoMcpHiggsfield.Pronto -> guardarCardDeImagemPronto(card, resultado.bytes, listaRefs, emitir)
            is ResultadoDaGeracaoMcpHiggsfield.Erro -> {
                val status = Fila.statusAposErroDaEspera(
                    temTarefa = card.idDaTarefa.isNotBlank(),
                    decorridoMs = System.currentTimeMillis() - inicioDaEsperaMs,
                    limiteMs = limiteMs,
                )
                emitir(cardsDeImagem.atualizar(card.copy(status = status, mensagem = resultado.mensagem)), null)
            }
        }
    }

    private fun guardarCardDeImagemPronto(
        card: CardDoPedidoDeVideoViaMcp,
        bytes: ByteArray,
        listaRefs: List<Pair<ByteArray, String>>,
        emitir: (CardDoPedidoDeVideoViaMcp, ImagemGeradaNaColecao?) -> Unit,
    ) {
        val modeloGuardado = if (card.mcp == PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_HIGGSFIELD) {
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloImagem(card.modelo)
        } else {
            card.modelo
        }
        val guardada = gravacao.guardarImagemComMetadados(
            bytes = bytes,
            prompt = card.prompt,
            origem = card.mcp,
            modelo = modeloGuardado,
            refsBytes = listaRefs,
            idDaReceita = card.idDaReceita,
        )
        if (guardada == null) {
            emitir(
                cardsDeImagem.atualizar(
                    card.copy(status = Fila.STATUS_FALHOU, mensagem = "Gerei a imagem, mas não consegui salvar na biblioteca."),
                ),
                null,
            )
            return
        }
        val preview = previewDataUrl(bytes)
        val fileUrl = RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho(guardada.caminhoArquivo)
        val pronta = ImagemGeradaNaColecao(guardada.id, fileUrl, preview, card.mcp)
        val atualizado = card.copy(
            status = Fila.STATUS_PRONTO,
            mensagem = "",
            idBiblioteca = guardada.id,
            fileUrl = fileUrl,
            caminhoMiniatura = salvarMiniaturaDoCard(card.id, preview),
        )
        emitir(cardsDeImagem.atualizar(atualizado), pronta)
    }

    fun listarCardsDeImagemJson(): String = jsonDosCardsGuardados(cardsDeImagem.listar())

    fun removerCardDeImagem(id: String): String {
        apagarMiniaturaDoCard(cardsDeImagem.obter(id))
        cardsDeImagem.remover(id)
        return """{"ok":true}"""
    }

    private val cardsDeImagemEmVerificacao = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    /** Bloqueante (rede): uma consulta da criação do card (só Magnific tem identificador). */
    fun verificarCardDeImagem(id: String, aoCard: (String) -> Unit) {
        val original = cardsDeImagem.obter(id) ?: return
        if (!cardsDeImagemEmVerificacao.add(id)) return
        val emitir: (CardDoPedidoDeVideoViaMcp, ImagemGeradaNaColecao?) -> Unit = { c, p ->
            aoCard("""{"ok":true,"card":${jsonDoCardDeImagem(c, p)}}""")
        }
        try {
            val card = comTarefaRecuperada(original, TIPO_IMAGEM_NO_HIGGSFIELD, cardsDeImagem) { emitir(it, null) } ?: return
            val consulta = when (card.mcp) {
                PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_MAGNIFIC -> clienteMagnific.consultarCriacaoUmaVez(card.idDaTarefa)
                // job_status serve para imagem e vídeo (a URL do resultado aceita os dois).
                PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_HIGGSFIELD -> cliente.consultarVideoDoJobUmaVez(card.idDaTarefa)
                else -> ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("Este serviço não permite consultar a imagem depois.")
            }
            when (consulta) {
                is ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto ->
                    guardarCardDeImagemPronto(card, consulta.bytes, refsDaReceita(card), emitir)
                ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando ->
                    emitir(
                        cardsDeImagem.atualizar(
                            card.copy(status = Fila.STATUS_DEMOROU, mensagem = "Ainda gerando. Tente de novo em instantes."),
                        ),
                        null,
                    )
                is ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou ->
                    emitir(cardsDeImagem.atualizar(card.copy(status = Fila.STATUS_FALHOU, mensagem = consulta.mensagem)), null)
            }
        } finally {
            cardsDeImagemEmVerificacao.remove(id)
        }
    }

    private fun jsonDoCardDeImagem(card: CardDoPedidoDeVideoViaMcp, pronta: ImagemGeradaNaColecao?): String =
        if (pronta == null) {
            Fila.jsonDoCard(card)
        } else {
            Fila.jsonDoCard(card, idBiblioteca = pronta.idBiblioteca, fileUrl = pronta.fileUrl, previewDataUrl = pronta.previewDataUrl)
        }

    private fun mimeDaImagem(bytes: ByteArray): String = when {
        bytes.size > 3 && bytes[0] == 0x89.toByte() && bytes[1] == 'P'.code.toByte() -> "image/png"
        bytes.size > 11 && String(bytes, 8, 4, Charsets.US_ASCII) == "WEBP" -> "image/webp"
        else -> "image/jpeg"
    }

    private fun mcpSemSessao(mcp: String): String? = when {
        mcp == PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_SJINN ->
            if (sessaoSjinn.temSessao()) null else SessaoDaContaSjinnNoAparelho.SEM_SESSAO
        mcp == PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_MAGNIFIC ->
            if (sessaoMagnific.temSessao()) null else SessaoDaContaMagnificNoAparelho.SEM_SESSAO
        sessao.temSessao() -> null
        else -> GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO
    }

    private fun nomeDoMcp(mcp: String): String = when (mcp) {
        PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_SJINN -> "Sjinn"
        PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_MAGNIFIC -> "Magnific"
        else -> "Higgsfield"
    }

    fun listarCardsDeVideoJson(): String = jsonDosCardsGuardados(cardsDeVideo.listar())

    fun removerCardDeVideo(id: String): String {
        cardsDeVideo.remover(id)
        return """{"ok":true}"""
    }

    /**
     * Cria um card por vídeo esperado e devolve na hora `{ok, cards:[...]}`.
     * Cada card evolui em thread própria; [aoCard] recebe `{ok, card}` a cada mudança (em qualquer thread).
     */
    fun iniciarVideosPorCards(pedidoJson: String, aoCard: (String) -> Unit, aoTerminarCard: () -> Unit): String =
        iniciarVideos(pedidoJson, aoCard, aoTerminarCard, refsFixas = null, receitaExistente = null)

    private fun iniciarVideos(
        pedidoJson: String,
        aoCard: (String) -> Unit,
        aoTerminarCard: () -> Unit,
        refsFixas: List<Referencia>?,
        receitaExistente: ReceitaDeGeracaoMcp?,
    ): String {
        val pedido = when (val lido = PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(pedidoJson)) {
            is ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado -> return RespostaJsonDaPonteDeGeracaoMcp.erro(lido.mensagem)
            is ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok -> lido.pedido
        }
        val refs = refsFixas ?: synchronized(trava) { referencias(ReceitaDeGeracaoMcp.TIPO_VIDEO).toList() }
        if (pedido.temReferencia && refs.isEmpty()) {
            return RespostaJsonDaPonteDeGeracaoMcp.erro("Escolha a imagem de referência antes de gerar.")
        }
        val refsUsadas = if (pedido.temReferencia) refs else emptyList()
        val imagens = refsUsadas.filterNot { it.ehVideo }.map { it.bytes to it.nome }
        val videosDoSlot = refsUsadas.mapNotNull { it.video }
        if (videosDoSlot.any { !it.isFile }) {
            return RespostaJsonDaPonteDeGeracaoMcp.erro("Um vídeo de referência sumiu do aparelho. Tire e escolha de novo.")
        }
        val modelosLocais = pedido.mcps.associateWith { modeloLocalParaMcp(pedido.modelo, it) }
        ReferenciaDeVideoNaGeracaoMcp.validar(
            refsUsadas.filter { it.ehVideo }.map { it.duracaoMs },
            pedido.mcps,
            pedido.modelo,
        )?.let { return RespostaJsonDaPonteDeGeracaoMcp.erro(it) }
        // Metadados da biblioteca: imagens + um quadro de cada vídeo de referência.
        val listaRefs = refsUsadas.map { it.bytes to it.nome }
        val receita = receitaExistente ?: criarReceita(ReceitaDeGeracaoMcp.TIPO_VIDEO, pedidoJson, refsUsadas)
        // Os vídeos saem da cópia permanente da receita: o slot em cache pode ser limpo durante a geração.
        val videosDaReceita = receita?.refs.orEmpty().filter { it.ehVideo }.mapNotNull { receitas.arquivoDaRef(it.nomeDoArquivo) }
        val videos = if (videosDaReceita.size == videosDoSlot.size) videosDaReceita else videosDoSlot
        val quantidade = pedido.quantidade.coerceIn(1, PedidoDeGeracaoDeVideoViaMcpNaColecao.QUANTIDADE_MAXIMA)
        val novos = cardsDeVideo.criar(
            quantidade = quantidade,
            mcps = pedido.mcps,
            modeloDoMcp = { modeloLocalParaMcp(pedido.modelo, it) },
            prompt = pedido.prompt,
            idDaReceita = receita?.id.orEmpty(),
        )
        val emitir: (CardDoPedidoDeVideoViaMcp, VideoHiggsfieldGuardado?) -> Unit = { card, guardado ->
            aoCard("""{"ok":true,"card":${jsonDoCard(card, guardado)}}""")
            if (card.status != Fila.STATUS_ENVIANDO && card.status != Fila.STATUS_GERANDO) aoTerminarCard()
        }
        for ((mcp, doMcp) in novos.groupBy { it.mcp }) {
            val local = pedido.copy(mcps = listOf(mcp), modelo = modelosLocais.getValue(mcp))
            Thread {
                try {
                    gerarCardsDoMcp(mcp, doMcp, local, imagens, videos, listaRefs, emitir)
                } catch (e: Throwable) {
                    doMcp.forEach { card ->
                        val atual = cardsDeVideo.obter(card.id) ?: card
                        if (atual.status == Fila.STATUS_ENVIANDO || atual.status == Fila.STATUS_GERANDO) {
                            emitir(cardsDeVideo.atualizar(atual.copy(status = Fila.STATUS_FALHOU, mensagem = e.message ?: "Falha.")), null)
                        }
                    }
                }
            }.start()
        }
        return jsonDosCards(novos)
    }

    private fun gerarCardsDoMcp(
        mcp: String,
        cards: List<CardDoPedidoDeVideoViaMcp>,
        pedido: PedidoDeGeracaoDeVideoViaMcpNaColecao,
        imagens: List<Pair<ByteArray, String>>,
        videos: List<File>,
        listaRefs: List<Pair<ByteArray, String>>,
        emitir: (CardDoPedidoDeVideoViaMcp, VideoHiggsfieldGuardado?) -> Unit,
    ) {
        val falharTodos: (String) -> Unit = { msg ->
            cards.forEach { emitir(cardsDeVideo.atualizar(it.copy(status = Fila.STATUS_FALHOU, mensagem = msg)), null) }
        }
        when (mcp) {
            PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_HIGGSFIELD -> {
                if (!sessao.temSessao()) return falharTodos(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO)
                val modelo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloVideo(pedido.modelo)
                val mediaIds = when (val up = cliente.enviarReferenciasDeVideo(imagens, videos)) {
                    is ResultadoDoUploadDeReferenciasHiggsfield.Erro -> return falharTodos(up.mensagem)
                    is ResultadoDoUploadDeReferenciasHiggsfield.Ok -> up.mediaIds
                }
                val extras = ReferenciaDeVideoNaGeracaoMcp.parametrosExtrasDoHiggsfield(modelo, videos.isNotEmpty())
                cards.forEach { card ->
                    Thread {
                        val criado = cliente.criarJobDeVideo(
                            prompt = pedido.prompt,
                            modelo = modelo,
                            aspectRatio = pedido.proporcao,
                            resolution = pedido.resolucao,
                            duracaoSegundos = pedido.duracaoSegundos,
                            mediaIds = mediaIds,
                            parametrosExtras = extras,
                        )
                        when (criado) {
                            is ResultadoDaCriacaoDeJobHiggsfield.Erro ->
                                emitir(cardsDeVideo.atualizar(card.copy(status = Fila.STATUS_FALHOU, mensagem = criado.mensagem)), null)
                            is ResultadoDaCriacaoDeJobHiggsfield.Criado -> {
                                val gerando = marcarGerando(card, criado.jobId, "", emitir)
                                val inicio = System.currentTimeMillis()
                                concluirCard(
                                    gerando,
                                    cliente.aguardarEBaixarVideoDoJob(criado.jobId),
                                    inicio,
                                    ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers.TIMEOUT_MS,
                                    listaRefs,
                                    emitir,
                                )
                            }
                        }
                    }.start()
                }
            }
            PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_SJINN -> {
                if (!sessaoSjinn.temSessao()) return falharTodos(SessaoDaContaSjinnNoAparelho.SEM_SESSAO)
                cards.forEach { card ->
                    Thread {
                        var atual = card
                        var inicio = System.currentTimeMillis()
                        val resultado = clienteSjinn.gerarVideo(
                            prompt = pedido.prompt,
                            modelo = pedido.modelo,
                            aspectRatio = pedido.proporcao,
                            resolution = pedido.resolucao,
                            duracaoSegundos = pedido.duracaoSegundos,
                            referenciasLocais = imagens,
                            videosLocais = videos,
                            aoCriarTarefa = { taskId, urls ->
                                atual = marcarGerando(card, taskId, urls.joinToString("\n"), emitir)
                                inicio = System.currentTimeMillis()
                            },
                        )
                        concluirCard(atual, resultado, inicio, ClienteApiDoSjinnParaGeracaoDeImagemNoChromebrothers.TIMEOUT_MS, listaRefs, emitir)
                    }.start()
                }
            }
            PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_TOPVIEW -> {
                if (!sessaoTopview.temSessao()) return falharTodos(GeracaoDeVideoPeloTopviewNoChat.SEM_SESSAO)
                cards.forEach { card ->
                    Thread {
                        var atual = card
                        var inicio = System.currentTimeMillis()
                        val resultado = clienteTopview.gerarVideo(
                            prompt = pedido.prompt,
                            modelo = pedido.modelo,
                            aspectRatio = pedido.proporcao,
                            resolution = pedido.resolucao,
                            duracaoSegundos = pedido.duracaoSegundos,
                            referenciasLocais = imagens,
                            videosLocais = videos,
                            aoCriarTarefa = { taskId, taskType ->
                                atual = marcarGerando(card, taskId, taskType, emitir)
                                inicio = System.currentTimeMillis()
                            },
                        )
                        concluirCard(atual, resultado, inicio, ClienteMcpDoTopviewParaGeracaoNoChromebrothers.TIMEOUT_MS, listaRefs, emitir)
                    }.start()
                }
            }
            PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_MAGNIFIC -> {
                if (!sessaoMagnific.temSessao()) return falharTodos(SessaoDaContaMagnificNoAparelho.SEM_SESSAO)
                val detalhe = detalheDoModeloMagnific(catalogoDeVideoDoMagnific.obter(), pedido.modelo)
                // Referências enviadas uma vez e usadas por todos os cards deste pedido.
                val (idsImagens, idsVideos) = try {
                    imagens.map { (bytes, nome) -> clienteMagnific.enviarArquivo(bytes, mimeDaImagem(bytes), nome) } to
                        videos.map { clienteMagnific.enviarVideo(it) }
                } catch (e: Exception) {
                    return falharTodos(e.message ?: "Falha ao enviar as referências ao Magnific.")
                }
                cards.forEach { card ->
                    Thread {
                        var atual = card
                        var inicio = System.currentTimeMillis()
                        val resultado = clienteMagnific.gerarVideo(
                            prompt = pedido.prompt,
                            slug = pedido.modelo,
                            duracao = AjusteDoPedidoAoModeloDoMagnific.duracao(pedido.duracaoSegundos, detalhe?.duracoes.orEmpty()),
                            proporcao = AjusteDoPedidoAoModeloDoMagnific.proporcao(pedido.proporcao, detalhe?.proporcoes.orEmpty()),
                            resolucao = AjusteDoPedidoAoModeloDoMagnific.resolucao(pedido.resolucao, detalhe?.resolucoes.orEmpty()),
                            imagens = idsImagens,
                            videos = idsVideos,
                            imagemComoReferencia = detalhe?.aceitaImagem ?: true,
                            aoCriarTarefa = { id ->
                                atual = marcarGerando(card, id, "", emitir)
                                inicio = System.currentTimeMillis()
                            },
                        )
                        concluirCard(atual, resultado, inicio, ClienteMcpDoMagnificParaGeracaoNoChromebrothers.TIMEOUT_MS, listaRefs, emitir)
                    }.start()
                }
            }
            PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_VIVIX -> {
                if (!sessaoVivix.temSessao()) return falharTodos(SessaoDaContaVivixNoAparelho.SEM_SESSAO)
                cards.forEach { card ->
                    Thread {
                        var atual = card
                        var inicio = System.currentTimeMillis()
                        val resultado = clienteVivix.gerarVideo(
                            prompt = pedido.prompt,
                            modelo = pedido.modelo,
                            aspectRatio = pedido.proporcao,
                            resolution = pedido.resolucao,
                            duracaoSegundos = pedido.duracaoSegundos,
                            referenciasLocais = imagens,
                            videosLocais = videos,
                            aoCriarTarefa = { jobId ->
                                atual = marcarGerando(card, jobId, "", emitir)
                                inicio = System.currentTimeMillis()
                            },
                        )
                        concluirCard(atual, resultado, inicio, ClienteApiDoVivixParaGeracaoDeVideoNoChromebrothers.TIMEOUT_MS, listaRefs, emitir)
                    }.start()
                }
            }
            else -> falharTodos("MCP não suportado para vídeo: $mcp")
        }
    }

    private fun marcarGerando(
        card: CardDoPedidoDeVideoViaMcp,
        idDaTarefa: String,
        detalhe: String,
        emitir: (CardDoPedidoDeVideoViaMcp, VideoHiggsfieldGuardado?) -> Unit,
    ): CardDoPedidoDeVideoViaMcp {
        val gerando = cardsDeVideo.atualizar(
            card.copy(status = Fila.STATUS_GERANDO, idDaTarefa = idDaTarefa, detalheDaTarefa = detalhe),
        )
        emitir(gerando, null)
        return gerando
    }

    private fun concluirCard(
        card: CardDoPedidoDeVideoViaMcp,
        resultado: ResultadoDaGeracaoMcpHiggsfield,
        inicioDaEsperaMs: Long,
        limiteMs: Long,
        listaRefs: List<Pair<ByteArray, String>>,
        emitir: (CardDoPedidoDeVideoViaMcp, VideoHiggsfieldGuardado?) -> Unit,
    ) {
        when (resultado) {
            is ResultadoDaGeracaoMcpHiggsfield.Pronto -> guardarCardPronto(card, resultado.bytes, listaRefs, emitir)
            is ResultadoDaGeracaoMcpHiggsfield.Erro -> {
                val status = Fila.statusAposErroDaEspera(
                    temTarefa = card.idDaTarefa.isNotBlank(),
                    decorridoMs = System.currentTimeMillis() - inicioDaEsperaMs,
                    limiteMs = limiteMs,
                )
                emitir(cardsDeVideo.atualizar(card.copy(status = status, mensagem = resultado.mensagem)), null)
            }
        }
    }

    private fun guardarCardPronto(
        card: CardDoPedidoDeVideoViaMcp,
        bytes: ByteArray,
        listaRefs: List<Pair<ByteArray, String>>,
        emitir: (CardDoPedidoDeVideoViaMcp, VideoHiggsfieldGuardado?) -> Unit,
    ) {
        val guardado = gravacao.guardarVideoComMetadados(
            bytes = bytes,
            prompt = card.prompt,
            origem = card.mcp,
            modelo = card.modelo,
            refsBytes = listaRefs,
            idDaReceita = card.idDaReceita,
        )
        if (guardado == null) {
            emitir(
                cardsDeVideo.atualizar(
                    card.copy(status = Fila.STATUS_FALHOU, mensagem = "Gerei o vídeo, mas não consegui salvar na biblioteca."),
                ),
                null,
            )
            return
        }
        val atualizado = card.copy(
            status = Fila.STATUS_PRONTO,
            mensagem = "",
            idBiblioteca = guardado.id,
            fileUrl = RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho(guardado.caminhoArquivo),
            caminhoMiniatura = guardado.caminhoMiniatura.orEmpty(),
        )
        emitir(cardsDeVideo.atualizar(atualizado), guardado)
    }

    private val cardsEmVerificacao = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    /** Bloqueante (rede): uma consulta da tarefa do card; o resultado sai por [aoCard]. */
    fun verificarCardDeVideo(id: String, aoCard: (String) -> Unit) {
        val original = cardsDeVideo.obter(id) ?: return
        if (!cardsEmVerificacao.add(id)) return
        val emitir: (CardDoPedidoDeVideoViaMcp, VideoHiggsfieldGuardado?) -> Unit = { c, g ->
            aoCard("""{"ok":true,"card":${jsonDoCard(c, g)}}""")
        }
        try {
            val card = comTarefaRecuperada(original, TIPO_VIDEO_NO_HIGGSFIELD, cardsDeVideo) { emitir(it, null) } ?: return
            val consulta = when (card.mcp) {
                PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_HIGGSFIELD -> cliente.consultarVideoDoJobUmaVez(card.idDaTarefa)
                PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_SJINN -> clienteSjinn.consultarVideoDaTarefaUmaVez(
                    card.idDaTarefa,
                    card.detalheDaTarefa.lines().map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
                )
                PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_TOPVIEW ->
                    clienteTopview.consultarVideoDaTarefaUmaVez(card.idDaTarefa, card.detalheDaTarefa)
                PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_MAGNIFIC ->
                    clienteMagnific.consultarCriacaoUmaVez(card.idDaTarefa)
                PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_VIVIX ->
                    clienteVivix.consultarVideoDoJobUmaVez(card.idDaTarefa)
                else -> ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou("MCP desconhecido: ${card.mcp}")
            }
            when (consulta) {
                is ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Pronto ->
                    guardarCardPronto(card, consulta.bytes, refsDaReceita(card), emitir)
                ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.AindaGerando ->
                    emitir(
                        cardsDeVideo.atualizar(
                            card.copy(status = Fila.STATUS_DEMOROU, mensagem = "Ainda gerando. Tente de novo em alguns minutos."),
                        ),
                        null,
                    )
                is ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa.Falhou ->
                    emitir(cardsDeVideo.atualizar(card.copy(status = Fila.STATUS_FALHOU, mensagem = consulta.mensagem)), null)
            }
        } finally {
            cardsEmVerificacao.remove(id)
        }
    }

    private fun jsonDoCard(card: CardDoPedidoDeVideoViaMcp, guardado: VideoHiggsfieldGuardado?): String =
        if (guardado == null) {
            Fila.jsonDoCard(card)
        } else {
            Fila.jsonDoCard(
                card,
                idBiblioteca = guardado.id,
                fileUrl = RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho(guardado.caminhoArquivo),
                previewDataUrl = previewDataUrlDeArquivoJpeg(guardado.caminhoMiniatura),
            )
        }

    private fun jsonDosCards(cards: List<CardDoPedidoDeVideoViaMcp>): String =
        """{"ok":true,"cards":[${cards.joinToString(",") { Fila.jsonDoCard(it) }}]}"""

    // ---- "Usar de novo" e "Tentar de novo" ----

    /** Preenchimento pendente por tipo, para o componente buscar ao abrir (fluxo vindo do ⓘ da gaveta). */
    private val preenchimentosPendentes = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun tipoDaReceita(idDaReceita: String): String? = receitas.obter(idDaReceita)?.tipo

    fun guardarParaPreencher(tipo: String, json: String) {
        preenchimentosPendentes[tipo] = json
    }

    /** Entrega (uma vez) o preenchimento guardado para o tipo do componente. */
    fun receitaParaPreencher(tipo: String): String =
        preenchimentosPendentes.remove(tipo.trim().lowercase()) ?: """{"ok":false}"""

    /**
     * Recoloca as refs da receita nos slots e devolve `{ok, tipo, pedido, refs, avisos}`
     * para o componente preencher prompt, MCPs, modelo e parâmetros. Não gera nada.
     */
    fun prepararReceitaParaPreencher(idDaReceita: String): String {
        val receita = receitas.obter(idDaReceita)
            ?: return RespostaJsonDaPonteDeGeracaoMcp.erro("Não achei a receita deste pedido.")
        val avisos = mutableListOf<String>()
        val refs = referenciasDaReceita(receita, avisos)
        val pedido = runCatching { org.json.JSONObject(receita.pedidoJson) }.getOrElse { org.json.JSONObject() }
        return jsonDePreencher(receita.tipo, pedido, refs, avisos)
    }

    /**
     * "Usar de novo" num card do gerador: com receita, ela inteira; sem receita (card antigo),
     * o prompt, o MCP e o modelo que o próprio card guarda.
     */
    fun prepararPreenchimentoDoCard(tipo: String, idDoCard: String): String {
        val cards = if (tipo == ReceitaDeGeracaoMcp.TIPO_VIDEO) cardsDeVideo else cardsDeImagem
        val card = cards.obter(idDoCard) ?: return RespostaJsonDaPonteDeGeracaoMcp.erro("Card não encontrado.")
        if (card.idDaReceita.isNotBlank() && receitas.obter(card.idDaReceita) != null) {
            return prepararReceitaParaPreencher(card.idDaReceita)
        }
        return prepararReceitaParcial(
            tipo,
            card.prompt,
            card.mcp,
            card.modelo,
            emptyList(),
            aviso = "Este pedido é de antes das receitas: parâmetros e referências não foram guardados.",
        )
    }

    /** Item de antes das receitas: usa o prompt, a origem, o modelo e as imagens de referência que ele guardou. */
    fun prepararReceitaParcial(
        tipo: String,
        prompt: String,
        origem: String,
        modelo: String,
        imagens: List<File>,
        aviso: String = "Este item é de antes das receitas: parâmetros e vídeos de referência não foram guardados.",
    ): String {
        val avisos = mutableListOf(aviso)
        val refs = imagens.mapNotNull { arquivo ->
            val bytes = runCatching { arquivo.readBytes() }.getOrNull()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            Referencia(bytes, arquivo.name, mimeDaImagem(bytes))
        }
        val pedido = org.json.JSONObject().put("prompt", prompt).put("temReferencia", refs.isNotEmpty())
        val mcp = origem.trim().lowercase()
        if (mcp.isNotEmpty()) pedido.put("mcps", org.json.JSONArray(listOf(mcp))).put("mcp", mcp)
        if (modelo.isNotBlank()) pedido.put("modelo", modelo)
        return jsonDePreencher(tipo, pedido, refs, avisos)
    }

    private fun jsonDePreencher(
        tipo: String,
        pedido: org.json.JSONObject,
        refs: List<Referencia>,
        avisos: MutableList<String>,
    ): String {
        val usadas = if (tipo == ReceitaDeGeracaoMcp.TIPO_IMAGEM) {
            refs.filterNot { it.ehVideo }.also {
                if (it.size < refs.size) avisos += "Vídeos de referência só valem no gerador de vídeo."
            }
        } else {
            refs
        }
        val refsJson = synchronized(trava) {
            val referencias = referencias(tipo)
            referencias.forEach(::apagarArquivoDoSlot)
            referencias.clear()
            referencias += usadas.take(PedidoDeGeracaoDeImagemViaMcpNaColecao.LIMITE_DE_REFERENCIAS)
            jsonDasReferenciasLocked(tipo)
        }
        return org.json.JSONObject()
            .put("ok", true)
            .put("tipo", tipo)
            .put("pedido", pedido)
            .put("refs", org.json.JSONObject(refsJson))
            .put("avisos", org.json.JSONArray(avisos))
            .toString()
    }

    /** Refs da receita como slots; as que sumiram do disco viram aviso. */
    private fun referenciasDaReceita(receita: ReceitaDeGeracaoMcp, avisos: MutableList<String>): List<Referencia> =
        receita.refs.mapNotNull { ref ->
            if (ref.ehVideo) {
                val video = receitas.arquivoDaRef(ref.nomeDoArquivo)
                if (video == null) {
                    avisos += "Referência não encontrada: ${ref.nome.ifBlank { "vídeo" }}."
                    return@mapNotNull null
                }
                val quadro = receitas.arquivoDaRef(ref.nomeDoQuadro)?.let { runCatching { it.readBytes() }.getOrNull() }
                    ?: ByteArray(0)
                Referencia(quadro, ref.nome.ifBlank { video.name }, "video/mp4", video, ref.duracaoMs, apagarAoLimpar = false)
            } else {
                val bytes = receitas.arquivoDaRef(ref.nomeDoArquivo)?.let { runCatching { it.readBytes() }.getOrNull() }
                if (bytes == null || bytes.isEmpty()) {
                    avisos += "Referência não encontrada: ${ref.nome.ifBlank { "imagem" }}."
                    return@mapNotNull null
                }
                Referencia(bytes, ref.nome.ifBlank { "referencia.${ref.extensao}" }, mimeDaImagem(bytes))
            }
        }

    /** Card com falha: reenvia a mesma receita só para o MCP dele, 1 geração, sem mexer nos slots do componente. */
    fun repetirCardDeImagem(id: String, aoCard: (String) -> Unit, aoTerminarCard: () -> Unit): String =
        repetirCard(cardsDeImagem, id) { pedido, refs, receita -> iniciarImagens(pedido, aoCard, aoTerminarCard, refs, receita) }

    fun repetirCardDeVideo(id: String, aoCard: (String) -> Unit, aoTerminarCard: () -> Unit): String =
        repetirCard(cardsDeVideo, id) { pedido, refs, receita -> iniciarVideos(pedido, aoCard, aoTerminarCard, refs, receita) }

    private fun repetirCard(
        cards: CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho,
        id: String,
        iniciar: (String, List<Referencia>, ReceitaDeGeracaoMcp) -> String,
    ): String {
        val card = cards.obter(id) ?: return RespostaJsonDaPonteDeGeracaoMcp.erro("Card não encontrado.")
        val receita = receitas.obter(card.idDaReceita)
            ?: return RespostaJsonDaPonteDeGeracaoMcp.erro("Este pedido é de antes das receitas: gere de novo pelo formulário.")
        val avisos = mutableListOf<String>()
        val refs = referenciasDaReceita(receita, avisos)
        if (avisos.isNotEmpty()) {
            return RespostaJsonDaPonteDeGeracaoMcp.erro(avisos.first() + " Use \"Usar de novo\" para escolher outra.")
        }
        val pedido = runCatching {
            org.json.JSONObject(receita.pedidoJson)
                .put("mcps", org.json.JSONArray(listOf(card.mcp)))
                .put("mcp", card.mcp)
                .put("quantidade", 1)
                .toString()
        }.getOrElse { return RespostaJsonDaPonteDeGeracaoMcp.erro("A receita deste pedido está corrompida.") }
        val resposta = iniciar(pedido, refs, receita)
        val objeto = runCatching { org.json.JSONObject(resposta) }.getOrNull()
        if (objeto?.optBoolean("ok") == true) {
            apagarMiniaturaDoCard(card)
            cards.remover(card.id)
            return objeto.put("removido", card.id).toString()
        }
        return resposta
    }

    /**
     * Card já com id da tarefa: segue igual. Higgsfield sem id (a criação chegou lá, mas a resposta se perdeu
     * na rede): procura no histórico (`show_generations`) o job com o mesmo prompt, criado logo depois do card
     * e que nenhum outro card usa; achando, grava o id no card. Sem como consultar, avisa e devolve null.
     */
    private fun comTarefaRecuperada(
        card: CardDoPedidoDeVideoViaMcp,
        tipoNoHiggsfield: String,
        cards: CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho,
        emitirFalha: (CardDoPedidoDeVideoViaMcp) -> Unit,
    ): CardDoPedidoDeVideoViaMcp? {
        if (card.idDaTarefa.isNotBlank()) return card
        if (card.mcp != Fila.MCP_QUE_TEM_HISTORICO) return null
        if (!sessao.temSessao()) {
            emitirFalha(cards.atualizar(card.copy(mensagem = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO)))
            return null
        }
        val achado = runCatching { localizarJobPerdidoNoHiggsfield(card, tipoNoHiggsfield, cards) }
        val idDoJob = achado.getOrNull()
        if (idDoJob == null) {
            val mensagem = achado.exceptionOrNull()?.let { "Não consegui ler o histórico do Higgsfield: ${it.message}" }
                ?: "Não achei este pedido no histórico do Higgsfield. Se ele não aparece no site, use Tentar de novo."
            emitirFalha(cards.atualizar(card.copy(status = Fila.STATUS_FALHOU, mensagem = mensagem)))
            return null
        }
        return cards.atualizar(card.copy(idDaTarefa = idDoJob, status = Fila.STATUS_DEMOROU, mensagem = "Achei o pedido no Higgsfield."))
    }

    private fun localizarJobPerdidoNoHiggsfield(
        card: CardDoPedidoDeVideoViaMcp,
        tipoNoHiggsfield: String,
        cards: CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho,
    ): String? {
        val bruto = cliente.listarGeracoesRecentesBruto(tipoNoHiggsfield, 50)
        val itens = org.json.JSONObject(bruto).optJSONObject("structuredContent")?.optJSONArray("items")
            ?: error("resposta sem a lista de gerações")
        val historico = (0 until itens.length()).mapNotNull { i ->
            val item = itens.optJSONObject(i) ?: return@mapNotNull null
            com.chromebrothers.navegador.regras.ItemDoHistoricoDoHiggsfield(
                id = item.optString("id"),
                prompt = item.optJSONObject("params")?.optString("prompt").orEmpty(),
                modelo = item.optString("model"),
                criadoEmMs = (item.optDouble("createdAt", 0.0) * 1000).toLong(),
                status = item.optString("status"),
            )
        }
        val usados = cards.listar().map { it.idDaTarefa }.filter { it.isNotBlank() }.toSet()
        return com.chromebrothers.navegador.regras.LocalizacaoDoJobPerdidoNoHistoricoDoHiggsfield.escolher(
            itens = historico,
            prompt = card.prompt,
            modelo = card.modelo,
            criadoEmMs = card.criadoEmMs,
            idsJaUsados = usados,
        )
    }

    /** Cards guardados para o gerador reaberto: prontos levam a prévia; arquivo sumido vira aviso. */
    private fun jsonDosCardsGuardados(cards: List<CardDoPedidoDeVideoViaMcp>): String =
        """{"ok":true,"cards":[${cards.joinToString(",") { jsonDoCardGuardado(it) }}]}"""

    private fun jsonDoCardGuardado(card: CardDoPedidoDeVideoViaMcp): String {
        if (card.status != Fila.STATUS_PRONTO) return Fila.jsonDoCard(card)
        if (!arquivoDoCardExiste(card.fileUrl)) {
            return Fila.jsonDoCard(card.copy(fileUrl = "", mensagem = "Removido da biblioteca."))
        }
        return Fila.jsonDoCard(card, previewDataUrl = previewDataUrlDeArquivoJpeg(card.caminhoMiniatura))
    }

    private fun arquivoDoCardExiste(fileUrl: String): Boolean =
        if (fileUrl.startsWith("content:", ignoreCase = true)) {
            existeConteudo(fileUrl)
        } else {
            arquivoDeFileUrl(fileUrl)?.isFile == true
        }

    /**
     * Mídia movida para a Galeria ([novos] = uri interna → `content://` da cópia): os cards que apontavam
     * para o arquivo interno passam a apontar para a cópia. Chamar antes de apagar o original — a miniatura
     * do card de vídeo é da biblioteca e some junto, então é copiada para a pasta dos cards.
     * Devolve (tipo do gerador, `{ok, card, movido}`) de cada card mudado.
     */
    fun acompanharArquivosMovidos(novos: Map<String, String>): List<Pair<String, String>> {
        val porCaminho = novos.mapNotNull { (original, novo) ->
            val caminho = arquivoDeFileUrl(original.substringBefore('?'))?.let { runCatching { it.canonicalPath }.getOrNull() }
            caminho?.let { it to novo }
        }.toMap()
        if (porCaminho.isEmpty()) return emptyList()
        val saida = mutableListOf<Pair<String, String>>()
        listOf(ReceitaDeGeracaoMcp.TIPO_IMAGEM to cardsDeImagem, ReceitaDeGeracaoMcp.TIPO_VIDEO to cardsDeVideo)
            .forEach { (tipo, cards) ->
                cards.listar().forEach { card ->
                    val caminho = arquivoDeFileUrl(card.fileUrl)?.let { runCatching { it.canonicalPath }.getOrNull() }
                    val novo = caminho?.let { porCaminho[it] } ?: return@forEach
                    val atualizado = cards.atualizar(
                        card.copy(fileUrl = novo, caminhoMiniatura = miniaturaPropriaDoCard(card)),
                    )
                    val json = org.json.JSONObject(jsonDoCardGuardado(atualizado))
                    saida += tipo to org.json.JSONObject().put("ok", true).put("card", json).put("movido", true).toString()
                }
            }
        return saida
    }

    /** Miniatura na pasta dos cards (copia a da biblioteca, que é apagada ao mover). */
    private fun miniaturaPropriaDoCard(card: CardDoPedidoDeVideoViaMcp): String {
        val atual = card.caminhoMiniatura.takeIf { it.isNotBlank() }?.let { File(it) } ?: return ""
        if (!atual.isFile) return ""
        if (atual.parentFile?.canonicalPath == pastaDasMiniaturasDosCards.canonicalPath) return atual.absolutePath
        return runCatching {
            pastaDasMiniaturasDosCards.mkdirs()
            val destino = File(pastaDasMiniaturasDosCards, "${card.id}.jpg")
            atual.copyTo(destino, overwrite = true)
            destino.absolutePath
        }.getOrDefault("")
    }

    private fun arquivoDeFileUrl(fileUrl: String): File? =
        if (fileUrl.isBlank()) null else runCatching { File(java.net.URI(fileUrl)) }.getOrNull()

    /** Guarda a receita do pedido; se o disco falhar, a geração segue sem receita (não trava o Gerar). */
    private fun criarReceita(tipo: String, pedidoJson: String, refs: List<Referencia>): ReceitaDeGeracaoMcp? =
        runCatching {
            receitas.criar(
                tipo,
                pedidoJson,
                refs.map { ReceitasDeGeracaoMcpGuardadasNoAparelho.RefParaGuardar(it.bytes, it.nome, it.mime, it.video, it.duracaoMs) },
            )
        }.getOrNull()

    private fun refsDaReceita(card: CardDoPedidoDeVideoViaMcp): List<Pair<ByteArray, String>> =
        receitas.obter(card.idDaReceita)?.let { receitas.refsParaMetadadosDaBiblioteca(it) }.orEmpty()

    /** Grava a prévia (data URL JPEG) do card pronto em disco; devolve o caminho ou vazio. */
    private fun salvarMiniaturaDoCard(idDoCard: String, previewDataUrl: String): String {
        if (!previewDataUrl.startsWith("data:image/jpeg;base64,")) return ""
        return runCatching {
            pastaDasMiniaturasDosCards.mkdirs()
            val arquivo = File(pastaDasMiniaturasDosCards, "$idDoCard.jpg")
            arquivo.writeBytes(Base64.decode(previewDataUrl.substringAfter(','), Base64.NO_WRAP))
            arquivo.absolutePath
        }.getOrDefault("")
    }

    /** Só apaga miniaturas da pasta dos cards (as de vídeo pertencem à biblioteca). */
    private fun apagarMiniaturaDoCard(card: CardDoPedidoDeVideoViaMcp?) {
        val caminho = card?.caminhoMiniatura?.takeIf { it.isNotBlank() } ?: return
        val arquivo = File(caminho)
        if (arquivo.parentFile?.canonicalPath == pastaDasMiniaturasDosCards.canonicalPath) runCatching { arquivo.delete() }
    }


    companion object {
        const val LARGURA_MAXIMA_DO_PREVIEW = 320
        private const val TIPO_VIDEO_NO_HIGGSFIELD = "video"
        private const val TIPO_IMAGEM_NO_HIGGSFIELD = "image"
        const val CATALOGO_HIGGSFIELD_IMAGEM = "higgsfield-imagem"
        const val CATALOGO_HIGGSFIELD_VIDEO = "higgsfield-video"
        const val CATALOGO_TOPVIEW_VIDEO = "topview-video"
        const val CATALOGO_MAGNIFIC_IMAGEM = "magnific-imagem"
        const val CATALOGO_MAGNIFIC_VIDEO = "magnific-video"
    }
}
