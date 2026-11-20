package com.chromebrothers.navegador

import android.content.ContentResolver
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import com.chromebrothers.navegador.regras.ChaveDeTrechoHls
import com.chromebrothers.navegador.regras.DecifragemDeTrechoHlsAes128
import com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia
import com.chromebrothers.navegador.regras.LeituraDeManifestoDashParaDownload
import com.chromebrothers.navegador.regras.LeituraDePlaylistHlsParaDownload
import com.chromebrothers.navegador.regras.LeituraDoTamanhoNoCabecalhoContentRange
import com.chromebrothers.navegador.regras.MensagensDeFalhaDoDownloadDeMidia
import com.chromebrothers.navegador.regras.PastaDoDownloadDeMidia
import com.chromebrothers.navegador.regras.PlanoDeDownloadDash
import com.chromebrothers.navegador.regras.PlanoDePartesDoArquivoDeMidia
import com.chromebrothers.navegador.regras.TipoDePedidoDeMidiaParaDownload
import com.chromebrothers.navegador.regras.TrilhaDeMidiaDash
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class FalhaNoDownloadDeMidia(val aviso: String) : Exception(aviso)

class DownloadEmPartesDaMidiaDetectadaNaPagina(
    private val pastaTemporaria: File,
    private val resolvedor: ContentResolver,
    private val cancelado: AtomicBoolean,
    private val aoProgresso: (baixado: Long, total: Long) -> Unit,
    /** Motor do Firefox (sessão das páginas) com a conexão comum de reserva. */
    private val transporte: TransporteDoDownloadDeMidia = TransportePorConexaoComum(),
) {
    /** Chaves AES-128 já baixadas (uma playlist costuma repetir a mesma chave em todos os trechos). */
    private val chavesBaixadas = ConcurrentHashMap<String, ByteArray>()

    fun executar(
        endereco: String,
        tipo: TipoDePedidoDeMidiaParaDownload,
        referer: String,
        agente: String,
        titulo: String,
        outrosManifestos: List<String> = emptyList(),
        destino: DestinoDoDownloadDeMidia = DestinoDoDownloadDeMidia.DOWNLOADS,
        audioSeparado: String? = null,
    ): ArquivoPublicadoDoDownloadDeMidia {
        pastaTemporaria.mkdirs()
        val temporario = File(pastaTemporaria, "midia-${System.nanoTime()}")
        try {
            val extensao = when (tipo) {
                TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS -> baixarHls(endereco, temporario, referer, agente)
                TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH ->
                    baixarDash(endereco, temporario, referer, agente, outrosManifestos)
                else -> {
                    baixarArquivo(endereco, temporario, referer, agente)
                    juntarAudioSeparadoSePreciso(temporario, audioSeparado, referer, agente)
                        ?: extensaoDoEndereco(endereco)
                }
            }
            conferirCancelamento()
            val nome = nomeDoArquivo(titulo, extensao)
            val mime = mimeDaExtensao(extensao)
            val naGaleria = PastaDoDownloadDeMidia.vaiParaAGaleria(destino, mime)
            val uri = if (naGaleria) publicarNaGaleria(temporario, nome, mime) else publicar(temporario, nome, mime)
            return ArquivoPublicadoDoDownloadDeMidia(nome, uri, naGaleria)
        } finally {
            if (temporario.exists()) temporario.delete()
        }
    }

    /**
     * Vídeo que veio mudo (players que separam vídeo e áudio, como o do VK/Boosty): baixa o áudio e junta
     * num MP4. Devolve "mp4" se juntou; null se não precisava (ou não deu, e fica o vídeo como veio).
     */
    private fun juntarAudioSeparadoSePreciso(video: File, audio: String?, referer: String, agente: String): String? {
        if (audio.isNullOrBlank()) return null
        if (runCatching { MuxDeTrilhaDeVideoETrilhaDeAudioEmUmArquivoMp4.temAudio(video) }.getOrDefault(true)) return null
        val pasta = File(pastaTemporaria, "audio-${System.nanoTime()}")
        pasta.mkdirs()
        try {
            val arquivoDeAudio = File(pasta, "audio.midia")
            baixarArquivo(audio, arquivoDeAudio, referer, agente)
            val juntado = File(pasta, "juntado.mp4")
            MuxDeTrilhaDeVideoETrilhaDeAudioEmUmArquivoMp4.juntar(video, arquivoDeAudio, juntado)
            juntado.copyTo(video, overwrite = true)
            return "mp4"
        } catch (erro: DownloadCancelado) {
            throw erro
        } catch (_: Exception) {
            return null
        } finally {
            pasta.deleteRecursively()
        }
    }

    private fun baixarDash(
        endereco: String,
        destino: File,
        referer: String,
        agente: String,
        outrosManifestos: List<String>,
    ): String {
        var plano = lerPlanoDash(endereco, referer, agente)
        if (plano.protegido) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.DRM)
        for (outro in outrosManifestos) {
            if (plano.video != null && plano.audio != null) break
            val extra = try {
                lerPlanoDash(outro, referer, agente)
            } catch (erro: DownloadCancelado) {
                throw erro
            } catch (_: Exception) {
                continue
            }
            if (extra.protegido) continue
            plano = PlanoDeDownloadDash(
                protegido = false,
                video = plano.video ?: extra.video,
                audio = plano.audio ?: extra.audio,
            )
        }
        val video = plano.video ?: throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.DASH_SEM_MONTAGEM)
        val pasta = File(pastaTemporaria, "dash-${System.nanoTime()}")
        pasta.mkdirs()
        try {
            val arquivoDeVideo = File(pasta, "video.midia")
            val origem = referer.ifBlank { endereco }
            baixarTrilhaDash(video, arquivoDeVideo, origem, agente)
            val audio = plano.audio
            if (audio == null || MuxDeTrilhaDeVideoETrilhaDeAudioEmUmArquivoMp4.temAudio(arquivoDeVideo)) {
                arquivoDeVideo.copyTo(destino, overwrite = true)
                return extensaoDaTrilhaDash(video)
            }
            val arquivoDeAudio = File(pasta, "audio.midia")
            baixarTrilhaDash(audio, arquivoDeAudio, origem, agente)
            MuxDeTrilhaDeVideoETrilhaDeAudioEmUmArquivoMp4.juntar(arquivoDeVideo, arquivoDeAudio, destino)
            return "mp4"
        } finally {
            pasta.deleteRecursively()
        }
    }

    private fun lerPlanoDash(endereco: String, referer: String, agente: String): PlanoDeDownloadDash {
        return LeituraDeManifestoDashParaDownload.plano(baixarTexto(endereco, referer, agente), endereco)
    }

    private fun baixarTrilhaDash(trilha: TrilhaDeMidiaDash, destino: File, referer: String, agente: String) {
        val arquivo = trilha.arquivo
        if (arquivo != null) {
            baixarArquivo(arquivo, destino, referer, agente)
            return
        }
        if (trilha.trechos.isEmpty()) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.DASH_SEM_MONTAGEM)
        if (trilha.trechos.size > 8000) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.TRECHOS_DEMAIS)
        val pasta = File(destino.parentFile, destino.name + "-trechos")
        pasta.mkdirs()
        try {
            val partes = trilha.trechos.mapIndexed { indice, _ -> File(pasta, "%05d.parte".format(indice)) }
            baixarVarios(trilha.trechos, partes, referer, agente)
            val mapa = trilha.inicializacao?.let { enderecoDoMapa ->
                val arquivoDoMapa = File(pasta, "mapa.parte")
                baixarInteiro(enderecoDoMapa, arquivoDoMapa, referer, agente)
                arquivoDoMapa
            }
            FileOutputStream(destino).use { saida ->
                mapa?.inputStream()?.use { it.copyTo(saida) }
                partes.forEach { parte -> parte.inputStream().use { it.copyTo(saida) } }
            }
        } finally {
            pasta.deleteRecursively()
        }
    }

    private fun extensaoDaTrilhaDash(trilha: TrilhaDeMidiaDash): String {
        val endereco = trilha.arquivo ?: trilha.inicializacao ?: trilha.trechos.firstOrNull().orEmpty()
        val caminho = endereco.substringBefore('?').lowercase()
        return when {
            caminho.endsWith(".webm") -> "webm"
            caminho.endsWith(".mkv") -> "mkv"
            caminho.endsWith(".mov") -> "mov"
            caminho.endsWith(".ts") -> "ts"
            else -> "mp4"
        }
    }

    private fun baixarHls(endereco: String, destino: File, referer: String, agente: String): String {
        var atual = endereco
        var lida = LeituraDePlaylistHlsParaDownload.ler(baixarTexto(atual, referer, agente), atual)
        if (lida.protegida) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.DRM)
        val melhor = LeituraDePlaylistHlsParaDownload.melhorVariante(lida)
        if (melhor != null) {
            atual = melhor.endereco
            lida = LeituraDePlaylistHlsParaDownload.ler(baixarTexto(atual, referer, agente), atual)
            if (lida.protegida) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.DRM)
        }
        if (lida.trechos.isEmpty()) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.SEM_TRECHOS)
        if (lida.trechos.size > 8000) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.TRECHOS_DEMAIS)
        val origem = referer.ifBlank { atual }
        val pasta = File(pastaTemporaria, "trechos-${System.nanoTime()}")
        pasta.mkdirs()
        try {
            val arquivos = lida.trechos.mapIndexed { indice, _ -> File(pasta, "%05d.parte".format(indice)) }
            baixarVarios(lida.trechos, arquivos, origem, agente)
            val mapa = lida.mapa?.let { enderecoDoMapa ->
                val arquivo = File(pasta, "mapa.parte")
                baixarInteiro(enderecoDoMapa, arquivo, origem, agente)
                arquivo
            }
            FileOutputStream(destino).use { saida ->
                mapa?.let { copiarTrecho(it, lida.chaveDoMapa, saida, origem, agente) }
                arquivos.forEachIndexed { indice, parte ->
                    conferirCancelamento()
                    copiarTrecho(parte, lida.chaves.getOrNull(indice), saida, origem, agente)
                }
            }
            val primeiro = lida.trechos.first().substringBefore('?').lowercase()
            return if (primeiro.endsWith(".m4s") || lida.mapa != null) "mp4" else "ts"
        } finally {
            pasta.deleteRecursively()
        }
    }

    /** Junta um trecho no arquivo final; se ele vier cifrado com AES-128, decifra no caminho. */
    private fun copiarTrecho(parte: File, chave: ChaveDeTrechoHls?, saida: FileOutputStream, referer: String, agente: String) {
        if (chave == null) {
            parte.inputStream().use { it.copyTo(saida) }
            return
        }
        val bytesDaChave = chaveAes(chave.endereco, referer, agente)
        try {
            DecifragemDeTrechoHlsAes128.decifrando(parte.inputStream(), bytesDaChave, chave.ivHex).use { it.copyTo(saida) }
        } catch (erro: java.io.IOException) {
            throw FalhaNoDownloadDeMidia("Não deu para decifrar os trechos desse vídeo.")
        }
    }

    private fun chaveAes(endereco: String, referer: String, agente: String): ByteArray =
        chavesBaixadas.getOrPut(endereco) {
            val bytes = transporte.abrir(endereco, null, referer, agente).use { resposta ->
                if (resposta.codigo !in 200..299) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.doCodigo(resposta.codigo))
                resposta.corpo.readBytes()
            }
            if (!DecifragemDeTrechoHlsAes128.chaveValida(bytes)) {
                throw FalhaNoDownloadDeMidia("O site não entregou a chave desse vídeo (pede login?).")
            }
            bytes
        }

    private fun baixarVarios(enderecos: List<String>, destinos: List<File>, referer: String, agente: String) {
        val pool = Executors.newFixedThreadPool(4)
        try {
            val tarefas = enderecos.mapIndexed { indice, endereco ->
                Callable {
                    conferirCancelamento()
                    repetir { baixarInteiro(endereco, destinos[indice], referer, agente) }
                }
            }
            pool.invokeAll(tarefas).forEach { futuro ->
                try {
                    futuro.get()
                } catch (erro: ExecutionException) {
                    val causa = erro.cause
                    if (causa is Exception) throw causa else throw erro
                }
            }
        } finally {
            pool.shutdownNow()
        }
    }

    private fun baixarArquivo(endereco: String, destino: File, referer: String, agente: String) {
        val sonda = transporte.abrir(endereco, "bytes=0-0", referer, agente)
        try {
            val codigo = sonda.codigo
            if (codigo !in 200..299) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.doCodigo(codigo))
            recusarPagina(sonda)
            if (codigo == HttpURLConnection.HTTP_PARTIAL) {
                val total = LeituraDoTamanhoNoCabecalhoContentRange.total(sonda.cabecalho("Content-Range"))
                sonda.close()
                if (total <= 0) {
                    baixarInteiro(endereco, destino, referer, agente)
                    return
                }
                val partes = PlanoDePartesDoArquivoDeMidia.planejar(total)
                if (partes.size <= 1) {
                    baixarIntervalo(endereco, destino, 0, total - 1, referer, agente, total)
                    return
                }
                java.io.RandomAccessFile(destino, "rw").use { it.setLength(total) }
                val progresso = AtomicLong(0)
                val pool = Executors.newFixedThreadPool(4)
                var intervaloIgnorado = false
                try {
                    val tarefas = partes.map { parte ->
                        Callable {
                            conferirCancelamento()
                            baixarIntervaloNoArquivo(
                                endereco, destino, parte.inicio, parte.fimInclusivo, referer, agente, progresso, total,
                            )
                        }
                    }
                    pool.invokeAll(tarefas).forEach { futuro ->
                        try {
                            futuro.get()
                        } catch (erro: ExecutionException) {
                            when (val causa = erro.cause) {
                                is FalhaDeIntervaloIgnorado -> intervaloIgnorado = true
                                is Exception -> throw causa
                                else -> throw erro
                            }
                        }
                    }
                } finally {
                    pool.shutdownNow()
                }
                if (intervaloIgnorado) baixarInteiro(endereco, destino, referer, agente)
                return
            }
            if (codigo !in 200..299) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.doCodigo(codigo))
            copiar(sonda.corpo, destino, sonda.tamanho.coerceAtLeast(0))
        } finally {
            sonda.close()
        }
    }

    private fun baixarIntervalo(
        endereco: String,
        destino: File,
        inicio: Long,
        fim: Long,
        referer: String,
        agente: String,
        total: Long,
    ) {
        transporte.abrir(endereco, "bytes=$inicio-$fim", referer, agente).use { resposta ->
            val codigo = resposta.codigo
            if (codigo !in 200..299 && codigo != HttpURLConnection.HTTP_PARTIAL) {
                throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.doCodigo(codigo))
            }
            copiar(resposta.corpo, destino, total)
        }
    }

    private fun baixarIntervaloNoArquivo(
        endereco: String,
        destino: File,
        inicio: Long,
        fim: Long,
        referer: String,
        agente: String,
        progresso: AtomicLong,
        total: Long,
    ): Long {
        transporte.abrir(endereco, "bytes=$inicio-$fim", referer, agente).use { resposta ->
            val codigo = resposta.codigo
            if (codigo == HttpURLConnection.HTTP_OK && inicio > 0) throw FalhaDeIntervaloIgnorado()
            if (codigo != HttpURLConnection.HTTP_PARTIAL && codigo != HttpURLConnection.HTTP_OK) {
                throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.doCodigo(codigo))
            }
            java.io.RandomAccessFile(destino, "rw").use { arquivo ->
                val entrada = resposta.corpo
                val buffer = ByteArray(64 * 1024)
                var posicao = inicio
                while (posicao <= fim) {
                    conferirCancelamento()
                    val lido = entrada.read(buffer)
                    if (lido < 0) break
                    val restante = (fim - posicao + 1).toInt()
                    val quantidade = minOf(lido, restante)
                    arquivo.seek(posicao)
                    arquivo.write(buffer, 0, quantidade)
                    posicao += quantidade
                    aoProgresso(progresso.addAndGet(quantidade.toLong()), total)
                }
            }
            return fim - inicio + 1
        }
    }

    private fun baixarInteiro(endereco: String, destino: File, referer: String, agente: String) {
        transporte.abrir(endereco, null, referer, agente).use { resposta ->
            val codigo = resposta.codigo
            if (codigo !in 200..299) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.doCodigo(codigo))
            recusarPagina(resposta)
            copiar(resposta.corpo, destino, resposta.tamanho.coerceAtLeast(0))
        }
    }

    private fun baixarTexto(endereco: String, referer: String, agente: String): String {
        transporte.abrir(endereco, null, referer, agente).use { resposta ->
            val codigo = resposta.codigo
            if (codigo !in 200..299) throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.doCodigo(codigo))
            resposta.corpo.bufferedReader().let { leitor ->
                val texto = StringBuilder()
                val buffer = CharArray(4096)
                while (texto.length < 2_000_000) {
                    conferirCancelamento()
                    val lido = leitor.read(buffer)
                    if (lido < 0) break
                    texto.append(buffer, 0, lido)
                }
                return texto.toString()
            }
        }
    }

    /** O site mandou uma página (login, erro) no lugar da mídia. */
    private fun recusarPagina(resposta: RespostaDoDownloadDeMidia) {
        if (MensagensDeFalhaDoDownloadDeMidia.ehPaginaEmVezDeMidia(resposta.cabecalho("Content-Type"))) {
            throw FalhaNoDownloadDeMidia(MensagensDeFalhaDoDownloadDeMidia.PAGINA_EM_VEZ_DO_VIDEO)
        }
    }

    private fun copiar(entrada: InputStream, destino: File, total: Long) {
        val progresso = AtomicLong(0)
        FileOutputStream(destino).use { saida ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                conferirCancelamento()
                val lido = entrada.read(buffer)
                if (lido < 0) break
                saida.write(buffer, 0, lido)
                aoProgresso(progresso.addAndGet(lido.toLong()), total)
            }
        }
    }

    private fun publicar(arquivo: File, nome: String, mime: String): String {
        val valores = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, nome)
            put(MediaStore.Downloads.MIME_TYPE, mime)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val colecao = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        return gravarNaColecao(colecao, valores, arquivo, "Downloads")
    }

    /** Botão "Enviar para a Galeria": Movies/ChromeBrothers, na coleção de vídeos (Galeria e Visualizador). */
    private fun publicarNaGaleria(arquivo: File, nome: String, mime: String): String {
        val valores = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, nome)
            put(MediaStore.Video.Media.MIME_TYPE, mime)
            put(MediaStore.Video.Media.RELATIVE_PATH, PastaDoDownloadDeMidia.GALERIA)
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val colecao = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        return gravarNaColecao(colecao, valores, arquivo, "Galeria")
    }

    private fun gravarNaColecao(colecao: android.net.Uri, valores: ContentValues, arquivo: File, onde: String): String {
        val uri = resolvedor.insert(colecao, valores)
            ?: throw FalhaNoDownloadDeMidia("Não consegui criar o arquivo na $onde.")
        try {
            resolvedor.openOutputStream(uri)?.use { saida ->
                arquivo.inputStream().use { it.copyTo(saida) }
            } ?: throw FalhaNoDownloadDeMidia("Não consegui gravar o arquivo na $onde.")
            valores.clear()
            valores.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolvedor.update(uri, valores, null, null)
            return uri.toString()
        } catch (erro: Exception) {
            resolvedor.delete(uri, null, null)
            if (erro is FalhaNoDownloadDeMidia) throw erro
            throw FalhaNoDownloadDeMidia("Não consegui gravar o arquivo na $onde.")
        }
    }

    private fun conferirCancelamento() {
        if (cancelado.get()) throw DownloadCancelado()
    }

    private fun repetir(bloco: () -> Unit) {
        try {
            bloco()
        } catch (erro: DownloadCancelado) {
            throw erro
        } catch (erro: FalhaNoDownloadDeMidia) {
            throw erro
        } catch (_: Exception) {
            bloco()
        }
    }

    private fun extensaoDoEndereco(endereco: String): String {
        val caminho = endereco.substringBefore('?').substringBefore('#').lowercase()
        val conhecidas = listOf("mp4", "m4v", "webm", "mkv", "mov", "ogv", "m4a", "mp3", "ts")
        return conhecidas.firstOrNull { caminho.endsWith(".$it") } ?: "mp4"
    }

    private fun mimeDaExtensao(extensao: String): String {
        return when (extensao) {
            "webm" -> "video/webm"
            "mkv" -> "video/x-matroska"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "ts" -> "video/mp2t"
            else -> "video/mp4"
        }
    }

    private fun nomeDoArquivo(titulo: String, extensao: String): String {
        val base = titulo.replace(Regex("""[\\/:*?"<>|]"""), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(60)
            .ifBlank { "video" }
        return "chromebrothers-$base-${System.currentTimeMillis()}.$extensao"
    }
}

private class FalhaDeIntervaloIgnorado : Exception()

class DownloadCancelado : Exception()

data class ArquivoPublicadoDoDownloadDeMidia(
    val nome: String,
    val uri: String,
    val naGaleria: Boolean = false,
)
