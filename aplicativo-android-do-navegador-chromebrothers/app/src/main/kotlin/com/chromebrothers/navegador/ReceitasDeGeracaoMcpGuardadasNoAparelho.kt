package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.ReceitaDeGeracaoMcp
import com.chromebrothers.navegador.regras.RefDaReceitaDeGeracaoMcp
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID

/**
 * Receitas das gerações MCP: `receitas-de-geracao/<id>.json` + referências por conteúdo em
 * `refs-das-receitas/<sha256>.<ext>` (dedupe: a mesma imagem usada em várias gerações ocupa espaço uma vez).
 * Os vídeos de referência saem do cache (que o Android pode limpar) e passam a ficar aqui.
 */
class ReceitasDeGeracaoMcpGuardadasNoAparelho(context: Context) {

    /** Uma referência no momento do Gerar: imagem em [bytes] ou vídeo em [video] (+ quadro JPEG em [bytes]). */
    class RefParaGuardar(
        val bytes: ByteArray,
        val nome: String,
        val mime: String,
        val video: File? = null,
        val duracaoMs: Long = 0L,
    )

    private val pastaDasReceitas = File(context.filesDir, PASTA_DAS_RECEITAS)
    private val pastaDasRefs = File(context.filesDir, PASTA_DAS_REFS)
    private val trava = Any()

    /** Bloqueante (disco): copia/deduplica as refs e grava a receita. */
    fun criar(tipo: String, pedidoJson: String, refs: List<RefParaGuardar>): ReceitaDeGeracaoMcp {
        val guardadas = refs.mapNotNull { guardarRef(it) }
        val receita = ReceitaDeGeracaoMcp(
            id = UUID.randomUUID().toString().take(12),
            tipo = tipo,
            criadoEmMs = System.currentTimeMillis(),
            pedidoJson = pedidoJson,
            refs = guardadas,
        )
        synchronized(trava) {
            pastaDasReceitas.mkdirs()
            escreverAtomico(File(pastaDasReceitas, "${receita.id}.json")) { it.write(ReceitaDeGeracaoMcp.paraJson(receita).toByteArray()) }
        }
        return receita
    }

    fun obter(id: String): ReceitaDeGeracaoMcp? {
        if (id.isBlank() || id.any { !it.isLetterOrDigit() && it != '-' }) return null
        val arquivo = File(pastaDasReceitas, "$id.json")
        if (!arquivo.isFile) return null
        return ReceitaDeGeracaoMcp.deJson(runCatching { arquivo.readText() }.getOrDefault(""))
    }

    fun arquivoDaRef(nomeDoArquivo: String): File? {
        if (nomeDoArquivo.isBlank() || nomeDoArquivo.contains('/') || nomeDoArquivo.contains('\\')) return null
        return File(pastaDasRefs, nomeDoArquivo).takeIf { it.isFile }
    }

    /**
     * Refs no formato que a biblioteca grava nos metadados: imagens e um quadro de cada vídeo.
     * Usado quando o card fica pronto depois (ex.: "Verificar") e as refs já não estão em memória.
     */
    fun refsParaMetadadosDaBiblioteca(receita: ReceitaDeGeracaoMcp): List<Pair<ByteArray, String>> =
        receita.refs.mapNotNull { ref ->
            val nome = if (ref.ehVideo) ref.nomeDoQuadro else ref.nomeDoArquivo
            val arquivo = arquivoDaRef(nome) ?: return@mapNotNull null
            runCatching { arquivo.readBytes() }.getOrNull()?.let { it to (if (ref.ehVideo) "quadro.jpg" else ref.nome) }
        }

    private fun guardarRef(ref: RefParaGuardar): RefDaReceitaDeGeracaoMcp? = runCatching {
        val video = ref.video
        if (video != null) {
            if (!video.isFile) return null
            val hash = video.inputStream().use { sha256(it) }
            val ext = ReceitaDeGeracaoMcp.extensaoSegura(video.extension.ifBlank { "mp4" })
            copiarSeAusente("$hash.$ext") { saida -> video.inputStream().use { it.copyTo(saida) } }
            val hashDoQuadro = if (ref.bytes.isNotEmpty()) guardarBytes(ref.bytes, "jpg") else ""
            RefDaReceitaDeGeracaoMcp(
                tipo = ReceitaDeGeracaoMcp.TIPO_VIDEO,
                nome = ref.nome,
                hash = hash,
                extensao = ext,
                duracaoMs = ref.duracaoMs,
                hashDoQuadro = hashDoQuadro,
            )
        } else {
            if (ref.bytes.isEmpty()) return null
            val ext = extensaoDaImagem(ref.mime, ref.nome)
            RefDaReceitaDeGeracaoMcp(
                tipo = ReceitaDeGeracaoMcp.TIPO_IMAGEM,
                nome = ref.nome,
                hash = guardarBytes(ref.bytes, ext),
                extensao = ext,
            )
        }
    }.getOrNull()

    private fun guardarBytes(bytes: ByteArray, ext: String): String {
        val hash = sha256(bytes.inputStream())
        copiarSeAusente("$hash.$ext") { it.write(bytes) }
        return hash
    }

    private fun copiarSeAusente(nome: String, escrever: (java.io.OutputStream) -> Unit) {
        synchronized(trava) {
            pastaDasRefs.mkdirs()
            val destino = File(pastaDasRefs, nome)
            if (destino.isFile && destino.length() > 0L) return
            escreverAtomico(destino, escrever)
        }
    }

    private fun escreverAtomico(destino: File, escrever: (java.io.OutputStream) -> Unit) {
        val temporario = File(destino.parentFile, "${destino.name}.tmp")
        temporario.outputStream().use(escrever)
        if (!temporario.renameTo(destino)) {
            destino.delete()
            if (!temporario.renameTo(destino)) {
                temporario.delete()
                error("Não consegui gravar ${destino.name}")
            }
        }
    }

    private fun sha256(entrada: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val lidos = entrada.read(buffer)
            if (lidos < 0) break
            digest.update(buffer, 0, lidos)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun extensaoDaImagem(mime: String, nome: String): String {
        when (mime.trim().lowercase().substringBefore(';')) {
            "image/png" -> return "png"
            "image/webp" -> return "webp"
            "image/gif" -> return "gif"
            "image/jpeg", "image/jpg" -> return "jpg"
        }
        val ext = nome.substringAfterLast('.', "").lowercase()
        return if (ext in setOf("png", "webp", "gif", "jpg", "jpeg")) ext else "jpg"
    }

    companion object {
        const val PASTA_DAS_RECEITAS = "receitas-de-geracao"
        const val PASTA_DAS_REFS = "refs-das-receitas"
    }
}
