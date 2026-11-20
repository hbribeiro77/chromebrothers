package com.chromebrothers.navegador

import android.app.Dialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.chromebrothers.navegador.regras.ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina
import com.chromebrothers.navegador.regras.ExtracaoDeTextoDeImagemViaIaDoNavegador
import com.chromebrothers.navegador.regras.FonteDoEnderecoDaImagemDoToqueLongo
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

enum class DestinoDaImagemDaPagina { COPIAR, SALVAR, EDITAR, ACRESCENTAR, EXTRAIR_TEXTO }

class CopiaDeImagemDaPaginaParaABibliotecaDoNavegador(
    private val activity: AppCompatActivity,
    private val aoReceber: (ByteArray, DestinoDaImagemDaPagina) -> Unit,
    private val temChave: () -> Boolean,
    private val aoColocarNaColecao: (String) -> Unit,
) {
    fun perguntarPorEndereco(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String) {
        perguntar(vista, endereco)
    }

    /** Baixa bytes da imagem da página (HTTP/data/blob/file) para arraste ou outros usos. */
    fun baixarBytes(
        vista: SuperficieDaPaginaNoMotorDoFirefox,
        endereco: String,
        aoReceber: (ByteArray?) -> Unit,
    ) {
        when (ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(endereco)) {
            FonteDoEnderecoDaImagemDoToqueLongo.DATA -> {
                val bytes = bytesDeDataUrl(endereco)?.takeIf { ehImagem(it) }
                aoReceber(bytes)
            }
            FonteDoEnderecoDaImagemDoToqueLongo.BLOB ->
                pedirBytesPelaPagina(vista, endereco, aoReceber)
            FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL -> {
                Thread {
                    val bytes = bytesDeArquivoLocal(endereco)?.takeIf { ehImagem(it) }
                    activity.runOnUiThread {
                        if (activity.isFinishing) return@runOnUiThread
                        aoReceber(bytes)
                    }
                }.start()
            }
            FonteDoEnderecoDaImagemDoToqueLongo.HTTP -> {
                val agente = vista.settings.userAgentString.orEmpty()
                val pagina = vista.url
                Thread {
                    val bytes = baixarPelaRede(endereco, agente, pagina, null)?.takeIf { ehImagem(it) }
                    activity.runOnUiThread {
                        if (activity.isFinishing) return@runOnUiThread
                        if (bytes != null) aoReceber(bytes)
                        else pedirBytesPelaPagina(vista, endereco, aoReceber)
                    }
                }.start()
            }
            FonteDoEnderecoDaImagemDoToqueLongo.INSUPORTADO -> aoReceber(null)
        }
    }

    private fun perguntar(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String) {
        val dialogo = Dialog(activity)
        val raiz = activity.layoutInflater.inflate(
            R.layout.layout_caixa_de_opcoes_da_imagem_tocada_na_pagina_do_navegador,
            null,
        )
        fun escolher(destino: DestinoDaImagemDaPagina) {
            dialogo.dismiss()
            baixar(vista, endereco, destino)
        }
        listOf(
            R.id.botao_copiar_imagem_da_pagina_no_toque_longo,
            R.id.botao_salvar_imagem_da_pagina_no_toque_longo,
            R.id.botao_editar_imagem_da_pagina_no_toque_longo,
            R.id.botao_colocar_imagem_na_colecao_no_toque_longo,
            R.id.botao_acrescentar_imagem_a_ultima_edicao,
            R.id.botao_extrair_texto_da_imagem_da_pagina_no_toque_longo,
            R.id.botao_cancelar_opcoes_da_imagem_da_pagina,
        ).forEach { id ->
            raiz.findViewById<Button>(id).apply {
                backgroundTintList = null
                stateListAnimator = null
            }
        }
        raiz.findViewById<View>(R.id.botao_copiar_imagem_da_pagina_no_toque_longo)
            .setOnClickListener { escolher(DestinoDaImagemDaPagina.COPIAR) }
        raiz.findViewById<View>(R.id.botao_salvar_imagem_da_pagina_no_toque_longo)
            .setOnClickListener { escolher(DestinoDaImagemDaPagina.SALVAR) }
        raiz.findViewById<View>(R.id.botao_editar_imagem_da_pagina_no_toque_longo)
            .setOnClickListener { escolher(DestinoDaImagemDaPagina.EDITAR) }
        raiz.findViewById<View>(R.id.botao_colocar_imagem_na_colecao_no_toque_longo)
            .setOnClickListener {
                dialogo.dismiss()
                aoColocarNaColecao(endereco)
            }
        raiz.findViewById<View>(R.id.botao_acrescentar_imagem_a_ultima_edicao)
            .setOnClickListener { escolher(DestinoDaImagemDaPagina.ACRESCENTAR) }
        raiz.findViewById<View>(R.id.botao_extrair_texto_da_imagem_da_pagina_no_toque_longo).apply {
            visibility = if (ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(temChave())) View.VISIBLE else View.GONE
            setOnClickListener { escolher(DestinoDaImagemDaPagina.EXTRAIR_TEXTO) }
        }
        raiz.findViewById<View>(R.id.botao_cancelar_opcoes_da_imagem_da_pagina)
            .setOnClickListener { dialogo.dismiss() }
        dialogo.setContentView(raiz)
        dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogo.show()
        val largura = (420 * activity.resources.displayMetrics.density).toInt()
        dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun baixar(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String, destino: DestinoDaImagemDaPagina) {
        when (ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.classificar(endereco)) {
            FonteDoEnderecoDaImagemDoToqueLongo.DATA -> {
                val bytes = bytesDeDataUrl(endereco)
                if (bytes == null || !ehImagem(bytes)) falhar() else aoReceber(bytes, destino)
            }
            FonteDoEnderecoDaImagemDoToqueLongo.BLOB -> pedirPelaPagina(vista, endereco, destino)
            FonteDoEnderecoDaImagemDoToqueLongo.ARQUIVO_LOCAL -> {
                Thread {
                    val bytes = bytesDeArquivoLocal(endereco)?.takeIf { ehImagem(it) }
                    activity.runOnUiThread {
                        if (activity.isFinishing) return@runOnUiThread
                        if (bytes != null) aoReceber(bytes, destino) else falhar()
                    }
                }.start()
            }
            FonteDoEnderecoDaImagemDoToqueLongo.HTTP -> {
                val agente = vista.settings.userAgentString.orEmpty()
                val pagina = vista.url
                Thread {
                    val bytes = baixarPelaRede(endereco, agente, pagina, null)?.takeIf { ehImagem(it) }
                    activity.runOnUiThread {
                        if (activity.isFinishing) return@runOnUiThread
                        if (bytes != null) aoReceber(bytes, destino)
                        else pedirPelaPagina(vista, endereco, destino)
                    }
                }.start()
            }
            FonteDoEnderecoDaImagemDoToqueLongo.INSUPORTADO -> falhar()
        }
    }

    private fun pedirPelaPagina(vista: SuperficieDaPaginaNoMotorDoFirefox, endereco: String, destino: DestinoDaImagemDaPagina) {
        pedirBytesPelaPagina(vista, endereco) { bytes ->
            if (bytes != null) aoReceber(bytes, destino) else falhar()
        }
    }

    private fun pedirBytesPelaPagina(
        vista: SuperficieDaPaginaNoMotorDoFirefox,
        endereco: String,
        aoReceber: (ByteArray?) -> Unit,
    ) {
        val citado = JSONObject.quote(endereco)
        val script = """
            (function(){
              var url = $citado;
              function desenhar(img) {
                try {
                  var w = img.naturalWidth || img.width;
                  var h = img.naturalHeight || img.height;
                  if (!w || !h) return "";
                  var tela = document.createElement("canvas");
                  tela.width = w;
                  tela.height = h;
                  tela.getContext("2d").drawImage(img, 0, 0);
                  return tela.toDataURL("image/png");
                } catch (e) { return ""; }
              }
              var lista = document.querySelectorAll("img");
              for (var i = 0; i < lista.length; i++) {
                var img = lista[i];
                if (img.currentSrc === url || img.src === url) {
                  var pronto = desenhar(img);
                  if (pronto) return pronto;
                }
              }
              return "";
            })()
        """.trimIndent()
        vista.evaluateJavascript(script) { bruto ->
            if (activity.isFinishing) return@evaluateJavascript
            val texto = textoDoJavascript(bruto)
            val bytes = texto?.let { bytesDeDataUrl(it) }
            aoReceber(bytes?.takeIf { ehImagem(it) })
        }
    }

    private fun falhar() {
        android.widget.Toast.makeText(
            activity,
            R.string.imagem_da_pagina_indisponivel,
            android.widget.Toast.LENGTH_LONG,
        ).show()
    }

    companion object {
        private const val LIMITE_DE_BYTES = 20 * 1024 * 1024

        fun bytesDeDataUrl(data: String): ByteArray? {
            val virgula = data.indexOf(',')
            if (!data.startsWith("data:") || virgula < 0) return null
            val cabeca = data.substring(0, virgula)
            val corpo = data.substring(virgula + 1)
            return try {
                if (cabeca.contains(";base64", ignoreCase = true)) {
                    Base64.decode(corpo, Base64.DEFAULT)
                } else {
                    java.net.URLDecoder.decode(corpo, "UTF-8").toByteArray(Charsets.UTF_8)
                }
            } catch (_: Exception) {
                null
            }
        }

        fun bytesDeArquivoLocal(endereco: String): ByteArray? {
            val caminho = ClassificacaoDoEnderecoDaImagemDoToqueLongoNaPagina.caminhoDoArquivoLocal(endereco)
                ?: return null
            val arquivo = File(caminho)
            if (!arquivo.isFile) return null
            if (arquivo.length() <= 0L || arquivo.length() > LIMITE_DE_BYTES) return null
            return runCatching { arquivo.readBytes() }.getOrNull()?.takeIf { it.isNotEmpty() }
        }

        fun mimeDaImagem(bytes: ByteArray): String {
            if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) return "image/jpeg"
            if (bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte()) return "image/png"
            if (bytes.size >= 6 && bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte()) return "image/gif"
            if (bytes.size >= 12 && bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte()) return "image/webp"
            return "image/jpeg"
        }

        fun ehImagem(bytes: ByteArray): Boolean {
            if (bytes.isEmpty()) return false
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return false
            bitmap.recycle()
            return true
        }

        private fun baixarPelaRede(
            endereco: String,
            agente: String,
            pagina: String?,
            cookie: String?,
        ): ByteArray? {
            val conexao = runCatching { URL(endereco).openConnection() as HttpURLConnection }.getOrNull()
                ?: return null
            return try {
                conexao.instanceFollowRedirects = true
                conexao.connectTimeout = 15000
                conexao.readTimeout = 20000
                if (agente.isNotBlank()) conexao.setRequestProperty("User-Agent", agente)
                if (!pagina.isNullOrBlank()) conexao.setRequestProperty("Referer", pagina)
                if (!cookie.isNullOrBlank()) conexao.setRequestProperty("Cookie", cookie)
                conexao.setRequestProperty("Accept", "image/avif,image/webp,image/*,*/*;q=0.8")
                conexao.connect()
                if (conexao.responseCode !in 200..299) return null
                val anunciado = conexao.contentLengthLong
                if (anunciado > LIMITE_DE_BYTES) return null
                conexao.inputStream.use { entrada ->
                    val saida = ByteArrayOutputStream()
                    val bloco = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val lido = entrada.read(bloco)
                        if (lido < 0) break
                        total += lido
                        if (total > LIMITE_DE_BYTES) return null
                        saida.write(bloco, 0, lido)
                    }
                    saida.toByteArray().takeIf { it.isNotEmpty() }
                }
            } catch (_: Exception) {
                null
            } finally {
                conexao.disconnect()
            }
        }

        private fun textoDoJavascript(bruto: String?): String? {
            if (bruto.isNullOrBlank() || bruto == "null") return null
            return try {
                JSONArray("[$bruto]").getString(0).takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        }
    }
}
