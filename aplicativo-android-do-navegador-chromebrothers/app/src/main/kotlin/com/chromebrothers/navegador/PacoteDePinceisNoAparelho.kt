package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.chromebrothers.navegador.regras.PacoteDePinceisChromebrush
import com.chromebrothers.navegador.regras.PincelDoMotor
import com.chromebrothers.navegador.regras.PincelDoProcreate
import com.chromebrothers.navegador.regras.PlistBinarioDaApple
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Exporta/importa pincéis do motor em `.chromebrush`; importa também `.brush`/`.brushset` do Procreate
 * (bloqueante: chamar fora da thread principal).
 */
object PacoteDePinceisNoAparelho {

    /** Salva em Downloads/ChromeBrothers; devolve o nome do arquivo salvo ou null. */
    fun exportar(context: Context, pinceis: List<PincelDoMotor>, nomeBase: String): String? {
        if (pinceis.isEmpty()) return null
        CatalogoDePinceisDoMotor.iniciar(context)
        val zip = ByteArrayOutputStream()
        ZipOutputStream(zip).use { saida ->
            saida.putNextEntry(ZipEntry(PacoteDePinceisChromebrush.ENTRADA_DO_MANIFESTO))
            saida.write(PacoteDePinceisChromebrush.manifesto(pinceis).toByteArray())
            saida.closeEntry()
            PacoteDePinceisChromebrush.arquivosUsados(pinceis).forEach { nome ->
                val arquivo = CatalogoDePinceisDoMotor.arquivoDaForma(nome) ?: return@forEach
                saida.putNextEntry(ZipEntry(PacoteDePinceisChromebrush.PASTA_DAS_IMAGENS + nome))
                arquivo.inputStream().use { it.copyTo(saida) }
                saida.closeEntry()
            }
        }
        val nomeDoArquivo = PacoteDePinceisChromebrush.nomeDoArquivo(nomeBase)
        var uri: Uri? = null
        return try {
            val valores = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nomeDoArquivo)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            uri = context.contentResolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), valores)
                ?: return null
            context.contentResolver.openOutputStream(uri)?.use { it.write(zip.toByteArray()) } ?: error("sem saída")
            valores.clear()
            valores.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, valores, null, null)
            nomeDoArquivo
        } catch (_: Exception) {
            uri?.let { runCatching { context.contentResolver.delete(it, null, null) } }
            null
        }
    }

    sealed class Importacao {
        data class Ok(val pinceis: List<PincelDoMotor>, val aviso: String? = null) : Importacao()
        data class Falhou(val mensagem: String) : Importacao()
    }

    /** Arquivo do Procreate (`.brush`/`.brushset`) lido do ZIP: só o que serve, imagens já como máscara 256×256. */
    private class LidoDoProcreate {
        val configuracoes = mutableMapOf<String, ByteArray>()
        val formas = mutableMapOf<String, Bitmap>()
        val granulados = mutableMapOf<String, Bitmap>()
        var ordem: ByteArray? = null
        fun reciclar() {
            (formas.values + granulados.values).forEach { if (!it.isRecycled) it.recycle() }
        }
    }

    fun importar(context: Context, uri: Uri): Importacao {
        CatalogoDePinceisDoMotor.iniciar(context)
        var manifesto: String? = null
        val imagens = mutableMapOf<String, ByteArray>()
        val procreate = LidoDoProcreate()
        // Imagens do Procreate cruas (até saber se a forma é invertida): decodificadas reduzidas.
        val formasCruas = mutableMapOf<String, Bitmap>()
        val granuladosCrus = mutableMapOf<String, Bitmap>()
        try {
            val entrada = context.contentResolver.openInputStream(uri) ?: return Importacao.Falhou("Não consegui abrir o arquivo.")
            ZipInputStream(entrada).use { zip ->
                var lidas = 0
                while (lidas < 4000) {
                    val item = zip.nextEntry ?: break
                    lidas++
                    if (item.isDirectory) continue
                    val nome = item.name.replace('\\', '/')
                    val arquivo = nome.substringAfterLast('/')
                    val pasta = nome.substring(0, nome.length - arquivo.length)
                    when {
                        nome == PacoteDePinceisChromebrush.ENTRADA_DO_MANIFESTO ->
                            manifesto = lerAte(zip, PacoteDePinceisChromebrush.LIMITE_DO_MANIFESTO)?.toString(Charsets.UTF_8)
                        arquivo.equals(PincelDoProcreate.ARQUIVO_DAS_CONFIGURACOES, ignoreCase = true) ->
                            lerAte(zip, LIMITE_DA_CONFIGURACAO_DO_PROCREATE)?.let { procreate.configuracoes[nome] = it }
                        nome.equals(PincelDoProcreate.ARQUIVO_DA_ORDEM, ignoreCase = true) ->
                            procreate.ordem = lerAte(zip, LIMITE_DA_CONFIGURACAO_DO_PROCREATE)
                        arquivo.equals(PincelDoProcreate.ARQUIVO_DA_FORMA, ignoreCase = true) ->
                            lerAte(zip, LIMITE_DA_IMAGEM_DO_PROCREATE)?.let { b -> decodificarReduzida(b)?.let { formasCruas[pasta] = it } }
                        arquivo.equals(PincelDoProcreate.ARQUIVO_DO_GRANULADO, ignoreCase = true) ->
                            lerAte(zip, LIMITE_DA_IMAGEM_DO_PROCREATE)?.let { b -> decodificarReduzida(b)?.let { granuladosCrus[pasta] = it } }
                        else -> {
                            val nomeDaImagem = PacoteDePinceisChromebrush.imagemDaEntrada(nome) ?: continue
                            lerAte(zip, PacoteDePinceisChromebrush.LIMITE_POR_IMAGEM)?.let { imagens[nomeDaImagem] = it }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            (formasCruas.values + granuladosCrus.values).forEach { if (!it.isRecycled) it.recycle() }
            return Importacao.Falhou("Esse arquivo não é um pacote de pincéis (.chromebrush, .brush ou .brushset).")
        }
        if (procreate.configuracoes.isNotEmpty()) {
            return try {
                importarDoProcreate(context, procreate, formasCruas, granuladosCrus)
            } finally {
                (formasCruas.values + granuladosCrus.values).forEach { if (!it.isRecycled) it.recycle() }
                procreate.reciclar()
            }
        }
        (formasCruas.values + granuladosCrus.values).forEach { if (!it.isRecycled) it.recycle() }
        val lidos = manifesto?.let { PacoteDePinceisChromebrush.lerManifesto(it) }.orEmpty()
        if (lidos.isEmpty()) return Importacao.Falhou("Não achei pincéis nesse arquivo.")
        val prontos = PacoteDePinceisChromebrush.prepararImportacao(
            lidos,
            idsExistentes = CatalogoDePinceisDoMotor.todos().map { it.id }.toSet(),
            novoId = { CatalogoDePinceisDoMotor.novoId() },
            guardarImagem = { nome ->
                // Decodifica e regrava: só entra imagem válida, com nome novo (nunca sobrescreve).
                val bytes = imagens[nome] ?: return@prepararImportacao null
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@prepararImportacao null
                CatalogoDePinceisDoMotor.salvarForma(bitmap).also { bitmap.recycle() }
            },
        )
        prontos.forEach { CatalogoDePinceisDoMotor.salvar(it) }
        return Importacao.Ok(prontos)
    }

    private const val LIMITE_DA_CONFIGURACAO_DO_PROCREATE = 4 * 1024 * 1024
    private const val LIMITE_DA_IMAGEM_DO_PROCREATE = 40 * 1024 * 1024
    private const val LIMITE_DE_PINCEIS_DO_PROCREATE = 200

    /** PNG do Procreate (pode ter 4096 px) reduzido para ~512 px já na decodificação. */
    private fun decodificarReduzida(bytes: ByteArray): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
        var amostra = 1
        while (minOf(limites.outWidth, limites.outHeight) / (amostra * 2) >= 512) amostra *= 2
        return runCatching {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = amostra })
        }.getOrNull()
    }

    private fun importarDoProcreate(
        context: Context,
        lido: LidoDoProcreate,
        formasCruas: Map<String, Bitmap>,
        granuladosCrus: Map<String, Bitmap>,
    ): Importacao {
        val plistDaOrdem = lido.ordem?.let { PlistBinarioDaApple.ler(it) ?: lerPlistDeTexto(it) }
        val ordem = PincelDoProcreate.ordemDoBrushset(plistDaOrdem)
        val pastas = PincelDoProcreate.pastasDosPinceis(lido.configuracoes.keys, ordem).take(LIMITE_DE_PINCEIS_DO_PROCREATE)
        val prontos = mutableListOf<PincelDoMotor>()
        val semEquivalente = linkedSetOf<String>()
        val relatorio = StringBuilder()
        var internos = 0
        pastas.forEachIndexed { indice, pasta ->
            val bytes = lido.configuracoes[pasta + PincelDoProcreate.ARQUIVO_DAS_CONFIGURACOES]
                ?: lido.configuracoes.entries.firstOrNull { it.key.equals(pasta + PincelDoProcreate.ARQUIVO_DAS_CONFIGURACOES, ignoreCase = true) }?.value
                ?: return@forEachIndexed
            val silica = PlistBinarioDaApple.raizDoArquivoChaveado(PlistBinarioDaApple.ler(bytes)) ?: return@forEachIndexed
            val forma = formasCruas[pasta]
            val granulado = granuladosCrus[pasta]
            val leitura = PincelDoProcreate.ler(
                silica,
                nomeReserva = "Pincel do Procreate ${indice + 1}",
                temFormaPropria = forma != null,
                temGranuladoProprio = granulado != null,
            )
            relatorio.append("# ").append(leitura.pincel.nome).append(" (").append(pasta.ifEmpty { "raiz" }).append(")\n")
            leitura.relatorio.forEach { relatorio.append(it).append('\n') }
            relatorio.append('\n')
            var pincel = leitura.pincel.copy(id = CatalogoDePinceisDoMotor.novoId())
            if (forma != null) {
                val mascara = CatalogoDePinceisDoMotor.mascaraDaImagem(forma, inverter = leitura.formaInvertida)
                val nome = CatalogoDePinceisDoMotor.salvarForma(mascara).also { mascara.recycle() }
                pincel = if (nome != null) pincel.copy(arquivoDaForma = nome) else pincel.copy(forma = com.chromebrothers.navegador.regras.FormaDoCarimbo.REDONDA)
            }
            if (granulado != null) {
                val mascara = CatalogoDePinceisDoMotor.mascaraDaImagem(granulado, inverter = leitura.granuladoInvertido)
                val nome = CatalogoDePinceisDoMotor.salvarForma(mascara).also { mascara.recycle() }
                pincel = if (nome != null) pincel.copy(arquivoDoGranulado = nome) else pincel.copy(granulado = com.chromebrothers.navegador.regras.FonteDoGranulado.PAPEL)
            }
            if (leitura.granuladoInterno) internos++
            semEquivalente += leitura.semEquivalente
            CatalogoDePinceisDoMotor.salvar(pincel)
            prontos += pincel
        }
        // Relatório para calibrar o mapeamento com arquivos reais (adb pull).
        runCatching {
            java.io.File(context.filesDir, "pinceis").apply { mkdirs() }
                .resolve("ultima-importacao-procreate.txt").writeText(relatorio.toString())
        }
        if (prontos.isEmpty()) return Importacao.Falhou("Não consegui ler os pincéis desse arquivo do Procreate.")
        val avisos = mutableListOf<String>()
        if (semEquivalente.isNotEmpty()) avisos += "Sem equivalente aqui (entrou sem): " + semEquivalente.joinToString(", ") + "."
        if (internos > 0) avisos += "Granulado da biblioteca interna do Procreate trocado pelo nosso papel."
        return Importacao.Ok(prontos, avisos.joinToString(" ").ifBlank { null })
    }

    /** `brushset.plist` às vezes vem em XML: só precisamos da lista `brushes`. */
    private fun lerPlistDeTexto(bytes: ByteArray): Any? {
        val texto = runCatching { bytes.toString(Charsets.UTF_8) }.getOrNull() ?: return null
        val bloco = Regex("<key>brushes</key>\\s*<array>(.*?)</array>", RegexOption.DOT_MATCHES_ALL).find(texto)?.groupValues?.get(1)
            ?: return null
        val nomes = Regex("<string>(.*?)</string>").findAll(bloco).map { it.groupValues[1].trim() }.toList()
        return mapOf("brushes" to nomes)
    }

    /** Lê a entrada inteira se couber em [limite]; maior que isso é ignorada (null). */
    private fun lerAte(entrada: InputStream, limite: Int): ByteArray? {
        val saida = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val n = entrada.read(buffer)
            if (n < 0) break
            total += n
            if (total > limite) return null
            saida.write(buffer, 0, n)
        }
        return saida.toByteArray()
    }
}
