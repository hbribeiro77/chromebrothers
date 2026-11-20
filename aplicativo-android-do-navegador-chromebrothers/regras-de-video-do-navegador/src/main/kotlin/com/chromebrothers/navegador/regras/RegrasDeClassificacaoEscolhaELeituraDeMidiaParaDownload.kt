package com.chromebrothers.navegador.regras

import java.net.URI
import kotlin.math.ceil
import kotlin.math.roundToInt

enum class TipoDePedidoDeMidiaParaDownload {
    IGNORAR,
    ARQUIVO,
    PLAYLIST_HLS,
    PLAYLIST_DASH,
    TRECHO,
}

object ClassificacaoDoPedidoDeMidiaParaDownload {
    private val extensoesDeArquivo = listOf(".mp4", ".m4v", ".webm", ".mkv", ".mov", ".ogv", ".m4a", ".mp3", ".aac")
    private val extensoesDeTrecho = listOf(".ts", ".m4s", ".cmfv", ".cmfa")

    fun classificar(endereco: String): TipoDePedidoDeMidiaParaDownload {
        val texto = endereco.trim()
        val esquema = texto.substringBefore(':', "").lowercase()
        if (esquema != "http" && esquema != "https") return TipoDePedidoDeMidiaParaDownload.IGNORAR
        val resto = texto.substringAfter("://", "")
        val caminhoEConsulta = if ('/' in resto) resto.substringAfter('/') else ""
        val caminho = caminhoEConsulta.substringBefore('?').substringBefore('#').lowercase()
        val consulta = caminhoEConsulta.substringAfter('?', "").substringBefore('#').lowercase()
        return when {
            caminho.endsWith(".m3u8") || "m3u8" in consulta -> TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS
            caminho.endsWith(".mpd") || ".mpd" in consulta -> TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH
            extensoesDeTrecho.any { caminho.endsWith(it) } -> TipoDePedidoDeMidiaParaDownload.TRECHO
            extensoesDeArquivo.any { caminho.endsWith(it) } -> TipoDePedidoDeMidiaParaDownload.ARQUIVO
            "videoplayback" in caminho -> TipoDePedidoDeMidiaParaDownload.ARQUIVO
            "mime=video" in consulta || "mime=audio" in consulta -> TipoDePedidoDeMidiaParaDownload.ARQUIVO
            else -> TipoDePedidoDeMidiaParaDownload.IGNORAR
        }
    }
}

data class PedidoDeMidiaObservado(
    val endereco: String,
    val tipo: TipoDePedidoDeMidiaParaDownload,
    val ordem: Long,
    val tamanho: Long = -1,
    /** Content-Type da resposta (ex.: "video/mp4", "audio/mp4"); vazio se não veio. */
    val mime: String = "",
) {
    val soAudio: Boolean get() = mime.startsWith("audio/", ignoreCase = true)
}

object EscolhaDoPedidoDeMidiaParaBaixar {
    const val TAMANHO_MINIMO_DE_VIDEO: Long = 5L * 1024 * 1024

    fun listar(pedidos: List<PedidoDeMidiaObservado>): List<PedidoDeMidiaObservado> {
        val uteis = pedidos.filter {
            it.tipo == TipoDePedidoDeMidiaParaDownload.ARQUIVO ||
                it.tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS ||
                it.tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH
        }
        val unicos = uteis.groupBy { it.endereco }.map { (_, grupo) ->
            val recente = grupo.maxBy { it.ordem }
            recente.copy(tamanho = grupo.maxOf { it.tamanho }, mime = grupo.firstOrNull { it.mime.isNotBlank() }?.mime.orEmpty())
        }
        val ordenados = unicos.sortedWith(
            compareBy<PedidoDeMidiaObservado> { pareceAudio(it) }
                .thenBy { it.tipo == TipoDePedidoDeMidiaParaDownload.ARQUIVO }
                .thenByDescending { it.tamanho }
                .thenByDescending { it.ordem },
        )
        val promissores = ordenados.filter { prometeArquivoInteiro(it) }
        return if (promissores.isNotEmpty()) promissores else ordenados
    }

    /**
     * Escolha automática do "Enviar para a Galeria" (um toque): a primeira opção da lista que não parece
     * só áudio. A lista já põe playlists (vídeo inteiro) e arquivos maiores na frente.
     */
    fun melhor(pedidos: List<PedidoDeMidiaObservado>): PedidoDeMidiaObservado? = candidatosParaAGaleria(pedidos).firstOrNull()

    /**
     * Ordem de tentativa do envio automático: primeiro o arquivo de vídeo inteiro com tamanho conhecido
     * (o mais confiável — é o que funciona em sites como o Boosty), do maior ao menor; depois as playlists
     * HLS/DASH; por último arquivos sem tamanho. Só áudio fica de fora. Se um falhar, tenta o próximo.
     */
    fun candidatosParaAGaleria(pedidos: List<PedidoDeMidiaObservado>): List<PedidoDeMidiaObservado> {
        val videos = listar(pedidos).filter { !pareceAudio(it) }
        val arquivosInteiros = videos
            .filter { it.tipo == TipoDePedidoDeMidiaParaDownload.ARQUIVO && it.tamanho >= TAMANHO_MINIMO_DE_VIDEO }
            .sortedByDescending { it.tamanho }
        val playlists = videos.filter { it.tipo != TipoDePedidoDeMidiaParaDownload.ARQUIVO }
        val resto = videos - arquivosInteiros.toSet() - playlists.toSet()
        return arquivosInteiros + playlists + resto
    }

    private fun prometeArquivoInteiro(pedido: PedidoDeMidiaObservado): Boolean {
        if (pedido.tipo != TipoDePedidoDeMidiaParaDownload.ARQUIVO) return true
        return pedido.tamanho < 0 || pedido.tamanho >= TAMANHO_MINIMO_DE_VIDEO
    }

    /**
     * Players como o do VK/Boosty mandam vídeo e áudio separados: o melhor áudio (arquivo inteiro, só
     * áudio, maior) para juntar ao vídeo escolhido. Null se a página não tem áudio separado.
     */
    fun melhorAudio(pedidos: List<PedidoDeMidiaObservado>): PedidoDeMidiaObservado? =
        pedidos.filter { it.tipo == TipoDePedidoDeMidiaParaDownload.ARQUIVO && pareceAudio(it) }
            .maxByOrNull { it.tamanho }

    fun pareceAudio(pedido: PedidoDeMidiaObservado): Boolean {
        if (pedido.mime.isNotBlank()) return pedido.soAudio
        return pareceAudio(pedido.endereco)
    }

    private fun pareceAudio(endereco: String): Boolean {
        val texto = endereco.lowercase()
        if ("video" in texto) return false
        val caminho = texto.substringAfter("://", texto).substringAfter('/').substringBefore('?')
        return "/audio" in texto ||
            "audioonly" in texto ||
            "mime=audio" in texto ||
            caminho.endsWith(".m4a") ||
            caminho.endsWith(".mp3") ||
            caminho.endsWith(".aac")
    }
}

data class ParteDoArquivoDeMidia(val inicio: Long, val fimInclusivo: Long)

object PlanoDePartesDoArquivoDeMidia {
    fun planejar(tamanho: Long, partes: Int = 4, minimoPorParte: Long = 1_048_576): List<ParteDoArquivoDeMidia> {
        if (tamanho <= 0) return emptyList()
        val quantidade = if (tamanho < minimoPorParte * 2) 1 else partes.coerceIn(1, 8)
        val resultado = ArrayList<ParteDoArquivoDeMidia>(quantidade)
        val tamanhoParte = tamanho / quantidade
        var inicio = 0L
        for (indice in 0 until quantidade) {
            val fimExclusivo = if (indice == quantidade - 1) tamanho else inicio + tamanhoParte
            resultado.add(ParteDoArquivoDeMidia(inicio, fimExclusivo - 1))
            inicio = fimExclusivo
        }
        return resultado
    }
}

data class VarianteDePlaylistHls(val banda: Long, val endereco: String)

/**
 * Chave AES-128 de um trecho HLS (criptografia simples, não é DRM): o player do site baixa a chave
 * em [endereco] e decifra com AES-CBC; [ivHex] tem 32 dígitos hexadecimais.
 */
data class ChaveDeTrechoHls(val endereco: String, val ivHex: String)

data class PlaylistHlsLida(
    val variantes: List<VarianteDePlaylistHls>,
    val trechos: List<String>,
    val mapa: String?,
    /** DRM de verdade (SAMPLE-AES, Widevine/FairPlay…): não dá para salvar. AES-128 simples não conta. */
    val protegida: Boolean,
    /** Uma chave por trecho (null = trecho sem criptografia). Vazia quando nada é cifrado. */
    val chaves: List<ChaveDeTrechoHls?> = emptyList(),
    /** Chave do EXT-X-MAP, quando ele vem depois de uma EXT-X-KEY com IV explícito. */
    val chaveDoMapa: ChaveDeTrechoHls? = null,
) {
    val cifrada: Boolean get() = chaves.any { it != null } || chaveDoMapa != null
}

object LeituraDePlaylistHlsParaDownload {
    fun ler(texto: String, enderecoDaPlaylist: String): PlaylistHlsLida {
        val base = try {
            URI(enderecoDaPlaylist)
        } catch (_: Exception) {
            return PlaylistHlsLida(emptyList(), emptyList(), null, false)
        }
        val variantes = mutableListOf<VarianteDePlaylistHls>()
        val trechos = mutableListOf<String>()
        val chaves = mutableListOf<ChaveDeTrechoHls?>()
        var mapa: String? = null
        var chaveDoMapa: ChaveDeTrechoHls? = null
        var protegida = false
        var bandaPendente: Long? = null
        var sequenciaInicial = 0L
        // Chave em vigor: endereço + IV explícito (null = IV vem do número de sequência do trecho).
        var chaveAtual: Pair<String, String?>? = null
        texto.split('\n').map { it.trim() }.filter { it.isNotEmpty() }.forEach { linha ->
            when {
                linha.startsWith("#EXT-X-STREAM-INF", ignoreCase = true) -> {
                    bandaPendente = atributo(linha, "BANDWIDTH")?.toLongOrNull() ?: 0L
                }
                linha.startsWith("#EXT-X-MEDIA-SEQUENCE", ignoreCase = true) -> {
                    sequenciaInicial = linha.substringAfter(':', "0").trim().toLongOrNull() ?: 0L
                }
                linha.startsWith("#EXT-X-MAP", ignoreCase = true) -> {
                    valorEntreAspas(linha, "URI")?.let { mapa = resolver(base, it) }
                    val chave = chaveAtual
                    val iv = chave?.second
                    if (chave != null && iv != null) chaveDoMapa = ChaveDeTrechoHls(chave.first, iv)
                }
                linha.startsWith("#EXT-X-KEY", ignoreCase = true) -> {
                    val metodo = atributo(linha, "METHOD")?.uppercase()
                    val formato = valorEntreAspas(linha, "KEYFORMAT") ?: atributo(linha, "KEYFORMAT")
                    val enderecoDaChave = valorEntreAspas(linha, "URI")
                    when {
                        metodo == null || metodo == "NONE" -> chaveAtual = null
                        metodo == "AES-128" &&
                            (formato == null || formato.equals("identity", ignoreCase = true)) &&
                            !enderecoDaChave.isNullOrBlank() &&
                            !enderecoDaChave.startsWith("skd:", ignoreCase = true) -> {
                            chaveAtual = resolver(base, enderecoDaChave) to ivHex(atributo(linha, "IV"))
                        }
                        else -> protegida = true
                    }
                }
                linha.startsWith("#") -> Unit
                else -> {
                    val absoluto = resolver(base, linha)
                    val banda = bandaPendente
                    if (banda != null) {
                        variantes.add(VarianteDePlaylistHls(banda, absoluto))
                        bandaPendente = null
                    } else {
                        val chave = chaveAtual
                        chaves.add(
                            chave?.let {
                                ChaveDeTrechoHls(it.first, it.second ?: ivDaSequencia(sequenciaInicial + trechos.size))
                            },
                        )
                        trechos.add(absoluto)
                    }
                }
            }
        }
        val algumaChave = chaves.any { it != null } || chaveDoMapa != null
        return PlaylistHlsLida(
            variantes,
            trechos,
            mapa,
            protegida,
            if (algumaChave) chaves else emptyList(),
            chaveDoMapa,
        )
    }

    /** IV padrão do AES-128 do HLS: o número de sequência do trecho em 16 bytes big-endian. */
    fun ivDaSequencia(sequencia: Long): String = "%032x".format(sequencia)

    /** `IV=0x...` → 32 dígitos hex (completa com zeros à esquerda); inválido → null. */
    fun ivHex(bruto: String?): String? {
        val limpo = bruto?.trim()?.removePrefix("0x")?.removePrefix("0X") ?: return null
        if (limpo.isEmpty() || limpo.length > 32 || !limpo.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
        return limpo.lowercase().padStart(32, '0')
    }

    fun melhorVariante(playlist: PlaylistHlsLida): VarianteDePlaylistHls? {
        if (playlist.variantes.isEmpty()) return null
        return playlist.variantes.maxBy { it.banda }
    }

    private fun resolver(base: URI, relativo: String): String {
        return try {
            base.resolve(relativo.trim()).toString()
        } catch (_: Exception) {
            relativo.trim()
        }
    }

    private fun atributo(linha: String, nome: String): String? {
        return Regex("(?i)$nome=([^,\\s]*)").find(linha)?.groupValues?.get(1)?.trim('"')
    }

    private fun valorEntreAspas(linha: String, nome: String): String? {
        return Regex("(?i)$nome=\"([^\"]*)\"").find(linha)?.groupValues?.get(1)
    }
}

data class ArquivoDeManifestoDash(val banda: Long, val endereco: String)

object LeituraDeManifestoDashParaDownload {
    private val extensoesCompletas = listOf(".mp4", ".m4v", ".webm", ".mkv", ".mov", ".ogv")

    fun arquivosCompletos(texto: String, enderecoDoManifesto: String): List<ArquivoDeManifestoDash> {
        val base = try {
            URI(enderecoDoManifesto)
        } catch (_: Exception) {
            return emptyList()
        }
        val blocos = Regex("(?is)<Representation\\b([^>]*)>(.*?)</Representation>")
        val arquivos = mutableListOf<ArquivoDeManifestoDash>()
        blocos.findAll(texto).forEach { bloco ->
            val banda = Regex("(?i)bandwidth\\s*=\\s*\"(\\d+)\"").find(bloco.groupValues[1])
                ?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val bruto = Regex("(?is)<BaseURL>(.*?)</BaseURL>").find(bloco.groupValues[2])
                ?.groupValues?.get(1)?.trim().orEmpty()
            if (bruto.isEmpty()) return@forEach
            val endereco = try {
                base.resolve(bruto).toString()
            } catch (_: Exception) {
                bruto
            }
            val caminho = endereco.substringBefore('?').lowercase()
            if (extensoesCompletas.any { caminho.endsWith(it) }) {
                arquivos.add(ArquivoDeManifestoDash(banda, endereco))
            }
        }
        return arquivos.sortedByDescending { it.banda }
    }

    fun plano(texto: String, enderecoDoManifesto: String): PlanoDeDownloadDash {
        val manifesto = try {
            URI(enderecoDoManifesto)
        } catch (_: Exception) {
            return PlanoDeDownloadDash(false, null, null)
        }
        val duracaoDoManifesto = atributo(abertura(texto, "MPD"), "mediaPresentationDuration")?.let { segundos(it) }
        val baseDoManifesto = encadear(manifesto, primeiraBase(semBlocos(texto, "Period")))
        val videos = mutableListOf<TrilhaDeMidiaDash>()
        val audios = mutableListOf<TrilhaDeMidiaDash>()
        var viuProtecao = false
        Regex("(?is)<Period\\b([^>]*)>(.*?)</Period>").findAll(texto).forEach { periodo ->
            val duracao = atributo(periodo.groupValues[1], "duration")?.let { segundos(it) } ?: duracaoDoManifesto
            val baseDoPeriodo = encadear(
                baseDoManifesto,
                primeiraBase(semBlocos(periodo.groupValues[2], "AdaptationSet")),
            )
            Regex("(?is)<AdaptationSet\\b([^>]*)>(.*?)</AdaptationSet>").findAll(periodo.groupValues[2]).forEach { conjunto ->
                val tagDoConjunto = conjunto.groupValues[1]
                val corpoDoConjunto = conjunto.groupValues[2]
                val conjuntoProtegido = Regex("(?is)<ContentProtection\\b").containsMatchIn(tagDoConjunto + corpoDoConjunto)
                if (conjuntoProtegido) viuProtecao = true
                val corpoSemRepresentacao = semBlocos(corpoDoConjunto, "Representation")
                val baseDoConjunto = encadear(baseDoPeriodo, primeiraBase(corpoSemRepresentacao))
                val templateDoConjunto = modeloDeTemplate(corpoSemRepresentacao)
                val listaDoConjunto = if (templateDoConjunto == null) corpoDaLista(corpoSemRepresentacao) else null
                representacoes(corpoDoConjunto).forEach { (tag, corpo) ->
                    if (conjuntoProtegido || Regex("(?is)<ContentProtection\\b").containsMatchIn(corpo)) {
                        viuProtecao = true
                        return@forEach
                    }
                    val trilha = montarTrilha(
                        tag,
                        corpo,
                        tagDoConjunto,
                        baseDoConjunto,
                        templateDoConjunto,
                        listaDoConjunto,
                        duracao,
                    ) ?: return@forEach
                    val tipoDeConteudo = atributo(tag, "contentType") ?: atributo(tagDoConjunto, "contentType").orEmpty()
                    val mime = atributo(tag, "mimeType") ?: atributo(tagDoConjunto, "mimeType").orEmpty()
                    val codecs = atributo(tag, "codecs") ?: atributo(tagDoConjunto, "codecs").orEmpty()
                    when {
                        ehTexto(tipoDeConteudo, mime, codecs) -> Unit
                        ehAudio(tipoDeConteudo, mime, codecs) -> audios.add(trilha)
                        else -> videos.add(trilha)
                    }
                }
            }
        }
        return PlanoDeDownloadDash(
            protegido = videos.isEmpty() && viuProtecao,
            video = videos.maxByOrNull { it.banda },
            audio = audios.maxByOrNull { it.banda },
        )
    }

    private fun montarTrilha(
        tag: String,
        corpo: String,
        tagDoConjunto: String,
        baseDoConjunto: URI,
        templateDoConjunto: Pair<String, String>?,
        listaDoConjunto: String?,
        duracao: Double?,
    ): TrilhaDeMidiaDash? {
        val id = atributo(tag, "id").orEmpty()
        val banda = atributo(tag, "bandwidth")?.toLongOrNull()
            ?: atributo(tagDoConjunto, "bandwidth")?.toLongOrNull()
            ?: 0L
        val template = modeloDeTemplate(corpo) ?: templateDoConjunto
        val lista = if (modeloDeTemplate(corpo) != null) null else corpoDaLista(corpo) ?: listaDoConjunto
        val base = encadear(baseDoConjunto, primeiraBase(corpo))
        if (template != null) {
            return trilhaDoTemplate(template, if (primeiraBase(corpo) == null) baseDoConjunto else base, id, banda, duracao)
        }
        if (lista != null) return trilhaDaLista(lista, base, banda)
        val arquivo = primeiraBase(corpo) ?: return null
        return TrilhaDeMidiaDash(banda, encadear(baseDoConjunto, arquivo).toString(), null, emptyList())
    }

    private fun trilhaDoTemplate(
        template: Pair<String, String>,
        base: URI,
        id: String,
        banda: Long,
        duracao: Double?,
    ): TrilhaDeMidiaDash? {
        val atributos = template.first
        val media = atributo(atributos, "media") ?: return null
        val timescale = atributo(atributos, "timescale")?.toLongOrNull()?.takeIf { it > 0 } ?: 1L
        val inicio = atributo(atributos, "startNumber")?.toLongOrNull() ?: 1L
        val trechos = mutableListOf<String>()
        val linhas = Regex("(?is)<S\\b([^>]*)>").findAll(template.second).toList()
        if (linhas.isNotEmpty()) {
            var relogio = 0L
            var numero = inicio
            linhas.forEach { linha ->
                val campos = linha.groupValues[1]
                val passo = atributo(campos, "d")?.toLongOrNull() ?: return@forEach
                atributo(campos, "t")?.toLongOrNull()?.let { relogio = it }
                val repeticoes = atributo(campos, "r")?.toLongOrNull() ?: 0L
                val vezes = if (repeticoes >= 0) {
                    repeticoes + 1
                } else if (duracao == null) {
                    1
                } else {
                    val limite = (duracao * timescale).toLong()
                    var quantidade = 0L
                    var cursor = relogio
                    while (cursor < limite && quantidade < 8000) {
                        quantidade += 1
                        cursor += passo
                    }
                    quantidade
                }
                var indice = 0L
                while (indice < vezes && trechos.size < 8000) {
                    trechos.add(resolverUrl(base, substituir(media, numero, relogio, id, banda)))
                    relogio += passo
                    numero += 1
                    indice += 1
                }
            }
        } else {
            val fim = atributo(atributos, "endNumber")?.toLongOrNull()
            val duracaoDoTrecho = atributo(atributos, "duration")?.toLongOrNull()
            val quantidade = when {
                fim != null -> (fim - inicio + 1).coerceAtLeast(0)
                duracaoDoTrecho != null && duracao != null && duracaoDoTrecho > 0 ->
                    ceil(duracao / (duracaoDoTrecho.toDouble() / timescale.toDouble()) - 1e-6).toLong().coerceAtLeast(0)
                else -> 0
            }
            var numero = inicio
            var gerados = 0L
            while (gerados < quantidade && trechos.size < 8000) {
                val tempo = (numero - inicio) * (duracaoDoTrecho ?: 0L)
                trechos.add(resolverUrl(base, substituir(media, numero, tempo, id, banda)))
                numero += 1
                gerados += 1
            }
        }
        if (trechos.isEmpty()) return null
        val inicializacao = atributo(atributos, "initialization")?.let {
            resolverUrl(base, substituir(it, inicio, 0, id, banda))
        }
        return TrilhaDeMidiaDash(banda, null, inicializacao, trechos)
    }

    private fun trilhaDaLista(corpo: String, base: URI, banda: Long): TrilhaDeMidiaDash? {
        val inicializacao = Regex("(?is)<Initialization\\b([^>]*)>").find(corpo)?.let {
            atributo(it.groupValues[1], "sourceURL")?.let { url -> resolverUrl(base, url) }
        }
        val trechos = Regex("(?is)<SegmentURL\\b([^>]*)>").findAll(corpo).mapNotNull {
            atributo(it.groupValues[1], "media")?.let { url -> resolverUrl(base, url) }
        }.toList()
        if (trechos.isEmpty()) return null
        return TrilhaDeMidiaDash(banda, null, inicializacao, trechos)
    }

    private fun substituir(modelo: String, numero: Long, tempo: Long, id: String, banda: Long): String {
        val reservado = modelo.replace("$$", "\u0000")
        val comNumero = Regex("\\\$Number(?:%0(\\d+)d)?\\\$").replace(reservado) { encontrado ->
            val largura = encontrado.groupValues[1].toIntOrNull()
            if (largura == null) numero.toString() else numero.toString().padStart(largura, '0')
        }
        return comNumero
            .replace("$" + "Time" + "$", tempo.toString())
            .replace("$" + "RepresentationID" + "$", id)
            .replace("$" + "Bandwidth" + "$", banda.toString())
            .replace("\u0000", "$")
    }

    private fun representacoes(corpo: String): List<Pair<String, String>> {
        val resultado = mutableListOf<Pair<String, String>>()
        Regex("(?is)<Representation\\b([^>]*)>").findAll(corpo).forEach { encontrado ->
            val bruto = encontrado.groupValues[1]
            val fechou = bruto.trimEnd().endsWith("/")
            val atributos = if (fechou) bruto.trimEnd().removeSuffix("/").trim() else bruto
            if (fechou) {
                resultado.add(atributos to "")
                return@forEach
            }
            val resto = corpo.substring(encontrado.range.last + 1)
            val fim = Regex("(?is)</Representation>").find(resto) ?: return@forEach
            resultado.add(atributos to resto.substring(0, fim.range.first))
        }
        return resultado
    }

    private fun modeloDeTemplate(xml: String): Pair<String, String>? {
        val encontrado = Regex("(?is)<SegmentTemplate\\b([^>]*)>").find(xml) ?: return null
        val bruto = encontrado.groupValues[1]
        val fechou = bruto.trimEnd().endsWith("/")
        val atributos = if (fechou) bruto.trimEnd().removeSuffix("/").trim() else bruto
        if (fechou) return atributos to ""
        val resto = xml.substring(encontrado.range.last + 1)
        val fim = Regex("(?is)</SegmentTemplate>").find(resto) ?: return atributos to ""
        return atributos to resto.substring(0, fim.range.first)
    }

    private fun corpoDaLista(xml: String): String? {
        return Regex("(?is)<SegmentList\\b[^>]*>(.*?)</SegmentList>").find(xml)?.groupValues?.get(1)
    }

    private fun semBlocos(xml: String, nome: String): String {
        return xml
            .replace(Regex("(?is)<$nome\\b[^>]*/>"), "")
            .replace(Regex("(?is)<$nome\\b.*?</$nome>"), "")
    }

    private fun abertura(xml: String, nome: String): String {
        return Regex("(?is)<$nome\\b([^>]*)>").find(xml)?.groupValues?.get(1).orEmpty()
    }

    private fun primeiraBase(xml: String): String? {
        val valor = Regex("(?is)<BaseURL>(.*?)</BaseURL>").find(xml)?.groupValues?.get(1)?.trim().orEmpty()
        return valor.ifBlank { null }
    }

    private fun encadear(base: URI, relativo: String?): URI {
        if (relativo.isNullOrBlank()) return base
        return try {
            base.resolve(textoXml(relativo))
        } catch (_: Exception) {
            base
        }
    }

    private fun resolverUrl(base: URI, relativo: String): String {
        return try {
            base.resolve(textoXml(relativo)).toString()
        } catch (_: Exception) {
            textoXml(relativo)
        }
    }

    private fun atributo(tag: String, nome: String): String? {
        val dupla = Regex("(?i)\\b${Regex.escape(nome)}\\s*=\\s*\"([^\"]*)\"").find(tag)?.groupValues?.get(1)
        if (dupla != null) return textoXml(dupla)
        val simples = Regex("(?i)\\b${Regex.escape(nome)}\\s*=\\s*'([^']*)'").find(tag)?.groupValues?.get(1)
        return simples?.let { textoXml(it) }
    }

    private fun textoXml(valor: String): String {
        return valor
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
            .trim()
    }

    private fun segundos(iso: String): Double? {
        val encontrado = Regex("(?i)PT(?:(\\d+(?:\\.\\d+)?)H)?(?:(\\d+(?:\\.\\d+)?)M)?(?:(\\d+(?:\\.\\d+)?)S)?").find(iso.trim())
            ?: return null
        val horas = encontrado.groupValues[1].toDoubleOrNull() ?: 0.0
        val minutos = encontrado.groupValues[2].toDoubleOrNull() ?: 0.0
        val segundos = encontrado.groupValues[3].toDoubleOrNull() ?: 0.0
        val total = horas * 3600 + minutos * 60 + segundos
        return total.takeIf { it > 0 }
    }

    private fun ehAudio(tipoDeConteudo: String, mime: String, codecs: String): Boolean {
        val tipo = tipoDeConteudo.lowercase()
        val tipoMime = mime.substringBefore(';').trim().lowercase()
        val codec = codecs.lowercase()
        return tipo == "audio" ||
            tipoMime.startsWith("audio/") ||
            codec.startsWith("mp4a") ||
            codec.startsWith("opus") ||
            codec.startsWith("ac-3") ||
            codec.startsWith("ec-3") ||
            codec.startsWith("flac")
    }

    private fun ehTexto(tipoDeConteudo: String, mime: String, codecs: String): Boolean {
        val tipo = tipoDeConteudo.lowercase()
        val tipoMime = mime.substringBefore(';').trim().lowercase()
        val codec = codecs.lowercase()
        return tipo == "text" ||
            tipoMime.startsWith("text/") ||
            tipoMime.contains("ttml") ||
            codec.startsWith("stpp") ||
            codec.startsWith("wvtt")
    }
}

data class TrilhaDeMidiaDash(
    val banda: Long,
    val arquivo: String?,
    val inicializacao: String?,
    val trechos: List<String>,
)

data class PlanoDeDownloadDash(
    val protegido: Boolean,
    val video: TrilhaDeMidiaDash?,
    val audio: TrilhaDeMidiaDash?,
)

object ClassificacaoDoTipoDeRespostaDeMidiaParaDownload {
    fun classificar(tipo: String): TipoDePedidoDeMidiaParaDownload {
        val mime = tipo.substringBefore(';').trim().lowercase()
        return when {
            mime.isEmpty() -> TipoDePedidoDeMidiaParaDownload.IGNORAR
            mime == "application/vnd.apple.mpegurl" || mime == "application/x-mpegurl" || mime == "audio/mpegurl" ->
                TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS
            mime == "application/dash+xml" -> TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH
            mime == "video/mp2t" -> TipoDePedidoDeMidiaParaDownload.TRECHO
            mime.startsWith("video/") || mime.startsWith("audio/") -> TipoDePedidoDeMidiaParaDownload.ARQUIVO
            else -> TipoDePedidoDeMidiaParaDownload.IGNORAR
        }
    }
}

object RotuloDoPedidoDeMidiaParaBaixar {
    fun texto(pedido: PedidoDeMidiaObservado): String {
        val tipo = when (pedido.tipo) {
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS -> "Playlist"
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH -> "DASH"
            TipoDePedidoDeMidiaParaDownload.ARQUIVO -> when {
                pedido.tamanho < 0 -> "Arquivo"
                pedido.tamanho >= EscolhaDoPedidoDeMidiaParaBaixar.TAMANHO_MINIMO_DE_VIDEO -> "Vídeo"
                else -> "Pedaço"
            }
            else -> "Mídia"
        }
        val tamanho = if (pedido.tipo == TipoDePedidoDeMidiaParaDownload.ARQUIVO) {
            FormatoDoTamanhoDeMidiaParaDownload.texto(pedido.tamanho)
        } else {
            ""
        }
        val host = try {
            URI(pedido.endereco).host
        } catch (_: Exception) {
            null
        }
        return listOf(tipo, tamanho, host)
            .filter { !it.isNullOrBlank() }
            .joinToString(" · ")
    }
}

object FormatoDoTamanhoDeMidiaParaDownload {
    fun texto(bytes: Long): String {
        if (bytes < 0) return ""
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "${kb.toInt()} KB"
        val mb = kb / 1024.0
        if (mb < 1024) return casaDecimal(mb, "MB")
        return casaDecimal(mb / 1024.0, "GB")
    }

    private fun casaDecimal(valor: Double, unidade: String): String {
        val decimos = (valor * 10.0).roundToInt()
        return "${decimos / 10},${decimos % 10} $unidade"
    }
}

object LeituraDoTamanhoNoCabecalhoContentRange {
    fun total(cabecalho: String?): Long {
        if (cabecalho.isNullOrBlank()) return -1
        return Regex("""(?i)bytes\s+\d+-\d+/(\d+)""").find(cabecalho)?.groupValues?.get(1)?.toLongOrNull() ?: -1
    }
}
