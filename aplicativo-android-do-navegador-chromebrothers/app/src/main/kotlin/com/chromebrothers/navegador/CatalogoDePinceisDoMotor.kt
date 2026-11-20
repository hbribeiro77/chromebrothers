package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.chromebrothers.navegador.regras.PinceisPredefinidosDoMotor
import com.chromebrothers.navegador.regras.PincelDoMotor
import com.chromebrothers.navegador.regras.PincelDoMotorEmJson
import java.io.File
import java.util.UUID

/**
 * Pincéis do motor: os que vêm no app + "Meus pincéis" (um JSON por linha em `pinceis/meus-pinceis.txt`).
 * As máscaras das formas de imagem ficam em `pinceis/formas/` com nome único e **nunca são sobrescritas**:
 * traços antigos continuam achando a forma com que foram feitos, mesmo depois de o pincel ser editado.
 */
object CatalogoDePinceisDoMotor {
    private var pasta: File? = null
    private val meus = mutableListOf<PincelDoMotor>()

    fun iniciar(context: Context) {
        synchronized(this) {
            if (pasta != null) return
            pasta = File(context.applicationContext.filesDir, "pinceis")
            carregar()
        }
        // Pontas prontas: desenha em segundo plano as que faltam (a lista de pincéis não trava na 1ª vez).
        Thread {
            com.chromebrothers.navegador.regras.PontasProntasDoMotor.TODAS.forEach { arquivoDaForma(it.arquivo) }
        }.start()
    }

    fun meusPinceis(): List<PincelDoMotor> = synchronized(this) { meus.toList() }

    fun todos(): List<PincelDoMotor> = PinceisPredefinidosDoMotor.TODOS + meusPinceis()

    fun porId(id: String): PincelDoMotor? =
        PinceisPredefinidosDoMotor.porId(id) ?: meusPinceis().firstOrNull { it.id == id }

    fun ehPredefinido(id: String): Boolean = PinceisPredefinidosDoMotor.porId(id) != null

    fun novoId(): String = "meu-" + UUID.randomUUID().toString().replace("-", "").take(8)

    fun salvar(pincel: PincelDoMotor) {
        if (ehPredefinido(pincel.id)) return
        synchronized(this) {
            val indice = meus.indexOfFirst { it.id == pincel.id }
            if (indice >= 0) meus[indice] = pincel else meus += pincel
            gravar()
        }
    }

    /** Tira da lista; a forma fica no disco (traços já feitos ainda a usam). */
    fun apagar(id: String) {
        synchronized(this) {
            if (meus.removeAll { it.id == id }) gravar()
        }
    }

    /** Guarda a máscara (branco com alpha) com nome novo e devolve o nome do arquivo. */
    fun salvarForma(mascara: Bitmap): String? {
        val pastaDasFormas = pastaDasFormas() ?: return null
        return runCatching {
            pastaDasFormas.mkdirs()
            val nome = "forma-" + UUID.randomUUID().toString().replace("-", "").take(12) + ".png"
            File(pastaDasFormas, nome).outputStream().use { mascara.compress(Bitmap.CompressFormat.PNG, 100, it) }
            nome
        }.getOrNull()
    }

    fun arquivoDaForma(nome: String): File? {
        if (nome.isBlank() || nome.contains('/') || nome.contains('\\') || nome.startsWith(".")) return null
        val arquivo = pastaDasFormas()?.let { File(it, nome) } ?: return null
        if (arquivo.isFile) return arquivo
        // Ponta pronta (desenhada pelo app): gera na primeira vez que alguém usa.
        val ponta = com.chromebrothers.navegador.regras.PontasProntasDoMotor.porArquivo(nome) ?: return null
        return synchronized(this) {
            if (!arquivo.isFile) gravarPontaPronta(ponta, arquivo)
            arquivo.takeIf { it.isFile }
        }
    }

    private fun gravarPontaPronta(ponta: com.chromebrothers.navegador.regras.PontaPronta, arquivo: File) {
        runCatching {
            val regra = com.chromebrothers.navegador.regras.PontasProntasDoMotor
            val alfas = regra.alfas(ponta)
            val pixels = IntArray(alfas.size) { Color.argb(alfas[it], 255, 255, 255) }
            val mascara = Bitmap.createBitmap(pixels, regra.LADO, regra.LADO, Bitmap.Config.ARGB_8888)
            arquivo.parentFile?.mkdirs()
            val temporario = File(arquivo.parentFile, arquivo.name + ".tmp")
            temporario.outputStream().use { mascara.compress(Bitmap.CompressFormat.PNG, 100, it) }
            mascara.recycle()
            temporario.renameTo(arquivo)
        }
    }

    /**
     * Imagem qualquer → máscara de forma 256×256: recorte quadrado do centro, luminosidade vira opacidade
     * (branco = tinta, como no Procreate); [inverter] troca (preto = tinta).
     */
    fun mascaraDaImagem(original: Bitmap, inverter: Boolean): Bitmap {
        val lado = minOf(original.width, original.height).coerceAtLeast(1)
        val recorte = Bitmap.createBitmap(original, (original.width - lado) / 2, (original.height - lado) / 2, lado, lado)
        val reduzida = Bitmap.createScaledBitmap(recorte, LADO_DA_MASCARA, LADO_DA_MASCARA, true)
        if (recorte !== original && recorte !== reduzida) recorte.recycle()
        val pixels = IntArray(LADO_DA_MASCARA * LADO_DA_MASCARA)
        reduzida.getPixels(pixels, 0, LADO_DA_MASCARA, 0, 0, LADO_DA_MASCARA, LADO_DA_MASCARA)
        if (reduzida !== original) reduzida.recycle()
        for (i in pixels.indices) {
            val c = pixels[i]
            val luz = (Color.red(c) * 299 + Color.green(c) * 587 + Color.blue(c) * 114) / 1000
            val tinta = if (inverter) 255 - luz else luz
            pixels[i] = Color.argb(tinta * Color.alpha(c) / 255, 255, 255, 255)
        }
        return Bitmap.createBitmap(pixels, LADO_DA_MASCARA, LADO_DA_MASCARA, Bitmap.Config.ARGB_8888)
    }

    /** Inverte uma máscara já guardada (gera arquivo novo). */
    fun inverterForma(nome: String): String? {
        val arquivo = arquivoDaForma(nome) ?: return null
        val atual = BitmapFactory.decodeFile(arquivo.absolutePath) ?: return null
        val pixels = IntArray(atual.width * atual.height)
        atual.getPixels(pixels, 0, atual.width, 0, 0, atual.width, atual.height)
        for (i in pixels.indices) pixels[i] = Color.argb(255 - Color.alpha(pixels[i]), 255, 255, 255)
        val invertida = Bitmap.createBitmap(pixels, atual.width, atual.height, Bitmap.Config.ARGB_8888)
        atual.recycle()
        return salvarForma(invertida).also { invertida.recycle() }
    }

    private fun pastaDasFormas(): File? = pasta?.let { File(it, "formas") }

    private fun carregar() {
        val arquivo = pasta?.let { File(it, ARQUIVO) } ?: return
        if (!arquivo.isFile) return
        meus.clear()
        runCatching { arquivo.readLines() }.getOrDefault(emptyList()).forEach { linha ->
            PincelDoMotorEmJson.deJson(linha)?.takeIf { !ehPredefinido(it.id) }?.let { meus += it }
        }
    }

    private fun gravar() {
        val base = pasta ?: return
        runCatching {
            base.mkdirs()
            val temporario = File(base, "$ARQUIVO.tmp")
            temporario.writeText(meus.joinToString("\n") { PincelDoMotorEmJson.paraJson(it) })
            val destino = File(base, ARQUIVO)
            if (!temporario.renameTo(destino)) {
                destino.delete()
                temporario.renameTo(destino)
            }
        }
    }

    private const val ARQUIVO = "meus-pinceis.txt"
    private const val LADO_DA_MASCARA = 256
}
