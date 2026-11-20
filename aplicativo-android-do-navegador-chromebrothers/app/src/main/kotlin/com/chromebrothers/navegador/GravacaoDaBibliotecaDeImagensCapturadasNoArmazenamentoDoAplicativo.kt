package com.chromebrothers.navegador

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.chromebrothers.navegador.regras.BibliotecaDeImagensCapturadasDoNavegador
import android.net.Uri
import com.chromebrothers.navegador.regras.ItemDaBibliotecaDeImagensCapturadas
import com.chromebrothers.navegador.regras.OrigemDaMidiaNaBibliotecaDoNavegador
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo(
    private val pasta: File,
    private val preferencias: SharedPreferences?,
    private val pastaDoUltimoQuadroCopiado: File?,
) {
    constructor(contexto: Context) : this(
        File(contexto.filesDir, "biblioteca-de-imagens-capturadas"),
        contexto.getSharedPreferences("biblioteca_de_imagens_capturadas", Context.MODE_PRIVATE),
        File(contexto.cacheDir, "quadros"),
    )

    fun carregar(): BibliotecaDeImagensCapturadasDoNavegador {
        val arquivo = File(pasta, "itens-da-biblioteca-de-imagens.json")
        if (!arquivo.isFile) return BibliotecaDeImagensCapturadasDoNavegador()
        return try {
            val lista = mutableListOf<ItemDaBibliotecaDeImagensCapturadas>()
            val array = JSONArray(arquivo.readText())
            for (indice in 0 until array.length()) {
                val objeto = array.getJSONObject(indice)
                val id = objeto.optString("id")
                val nome = objeto.optString("nomeDoArquivo")
                if (id.isBlank() || nome.isBlank()) continue
                lista += ItemDaBibliotecaDeImagensCapturadas(
                    id = id,
                    instante = objeto.optLong("instante"),
                    nomeDoArquivo = nome,
                    naGaleria = objeto.optBoolean("naGaleria"),
                    favorito = objeto.optBoolean("favorito"),
                    foraDoHistorico = objeto.optBoolean("foraDoHistorico"),
                    uriDaGaleria = objeto.optString("uriDaGaleria").ifBlank { null },
                    nomeExibido = objeto.optString("nomeExibido").ifBlank { null },
                    origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(objeto.optString("origem")),
                    modelo = objeto.optString("modelo"),
                    prompt = objeto.optString("prompt"),
                    refs = referenciasDeTextoNoArrayJson(objeto.optJSONArray("refs")),
                    caminhoLocal = objeto.optString("caminhoLocal"),
                    exportado = objeto.optBoolean("exportado", false),
                    idDaReceita = objeto.optString("idDaReceita"),
                )
            }
            BibliotecaDeImagensCapturadasDoNavegador(lista)
        } catch (_: Exception) {
            BibliotecaDeImagensCapturadasDoNavegador()
        }
    }

    fun gravar(biblioteca: BibliotecaDeImagensCapturadasDoNavegador) {
        pasta.mkdirs()
        val array = JSONArray()
        biblioteca.listar().forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("instante", item.instante)
                    .put("nomeDoArquivo", item.nomeDoArquivo)
                    .put("naGaleria", item.naGaleria)
                    .put("favorito", item.favorito)
                    .put("foraDoHistorico", item.foraDoHistorico)
                    .put("uriDaGaleria", item.uriDaGaleria.orEmpty())
                    .put("nomeExibido", item.nomeExibido.orEmpty())
                    .put("origem", item.origem)
                    .put("modelo", item.modelo)
                    .put("prompt", item.prompt)
                    .put("refs", JSONArray(item.refs))
                    .put("caminhoLocal", item.caminhoLocal)
                    .put("exportado", item.exportado)
                    .put("idDaReceita", item.idDaReceita),
            )
        }
        File(pasta, "itens-da-biblioteca-de-imagens.json").writeText(array.toString())
    }

    fun guardarQuadro(
        biblioteca: BibliotecaDeImagensCapturadasDoNavegador,
        bytes: ByteArray,
        naGaleria: Boolean,
    ): ItemDaBibliotecaDeImagensCapturadas? {
        val origem = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        val largura = origem.width
        val reduzida = if (largura <= LARGURA_DO_QUADRO) {
            origem
        } else {
            val altura = (origem.height.toLong() * LARGURA_DO_QUADRO / largura).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(origem, LARGURA_DO_QUADRO, altura, true)
        }
        val criada = reduzida !== origem
        val id = UUID.randomUUID().toString()
        val nome = "quadro-$id.jpg"
        pasta.mkdirs()
        File(pasta, nome).outputStream().use { saida ->
            reduzida.compress(Bitmap.CompressFormat.JPEG, 90, saida)
        }
        if (criada && !reduzida.isRecycled) reduzida.recycle()
        if (!origem.isRecycled) origem.recycle()
        val item = biblioteca.registrar(
            id = id,
            instante = System.currentTimeMillis(),
            nomeDoArquivo = nome,
            naGaleria = naGaleria,
        )
        gravar(biblioteca)
        return item
    }

    /**
     * Imagem final do editor: grava no formato e tamanho escolhidos (sem a redução para
     * [LARGURA_DO_QUADRO] nem a recodificação em JPEG do [guardarQuadro]). Devolve o item e os bytes.
     */
    fun guardarImagemExportada(
        biblioteca: BibliotecaDeImagensCapturadasDoNavegador,
        imagem: Bitmap,
        formato: com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato,
    ): Pair<ItemDaBibliotecaDeImagensCapturadas, ByteArray>? {
        val regra = com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao
        val saida = java.io.ByteArrayOutputStream()
        val ok = if (formato == com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.Formato.PNG) {
            imagem.compress(Bitmap.CompressFormat.PNG, 100, saida)
        } else {
            imagem.compress(Bitmap.CompressFormat.JPEG, 92, saida)
        }
        if (!ok) return null
        val bytes = saida.toByteArray()
        val id = UUID.randomUUID().toString()
        val nome = "quadro-$id.${regra.extensao(formato)}"
        pasta.mkdirs()
        File(pasta, nome).writeBytes(bytes)
        val item = biblioteca.registrar(
            id = id,
            instante = System.currentTimeMillis(),
            nomeDoArquivo = nome,
            naGaleria = false,
        )
        gravar(biblioteca)
        return item to bytes
    }

    fun importarUltimoQuadroCopiadoSeAindaExistir(
        biblioteca: BibliotecaDeImagensCapturadasDoNavegador,
    ): BibliotecaDeImagensCapturadasDoNavegador {
        val prefs = preferencias ?: return biblioteca
        if (prefs.getBoolean(CHAVE_IMPORTACAO, false)) return biblioteca
        prefs.edit().putBoolean(CHAVE_IMPORTACAO, true).apply()
        val arquivo = pastaDoUltimoQuadroCopiado?.let { File(it, "chromebrothers-quadro-copiado.png") }
        if (arquivo == null || !arquivo.isFile || arquivo.length() <= 0L) return biblioteca
        guardarQuadro(biblioteca, arquivo.readBytes(), naGaleria = false)
        return biblioteca
    }

    fun lembrarGaleria(
        biblioteca: BibliotecaDeImagensCapturadasDoNavegador,
        id: String,
        uri: String,
        nome: String,
    ): ItemDaBibliotecaDeImagensCapturadas? {
        val item = biblioteca.vincularGaleria(id, uri, nome) ?: return null
        gravar(biblioteca)
        return item
    }

    fun copiarItem(
        contexto: Context,
        biblioteca: BibliotecaDeImagensCapturadasDoNavegador,
        item: ItemDaBibliotecaDeImagensCapturadas,
    ): ItemDaBibliotecaDeImagensCapturadas {
        val existente = item.uriDaGaleria
        if (item.naGaleria && !existente.isNullOrBlank()) {
            GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.copiarUri(
                contexto,
                Uri.parse(existente),
                item.nomeExibido ?: "imagem.jpg",
            )
            return item
        }
        val bytes = ler(item.nomeDoArquivo) ?: throw java.io.IOException("A imagem não está mais no aplicativo.")
        GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia.copiarParaAreaDeTransferencia(
            contexto,
            bytes,
            com.chromebrothers.navegador.regras.TamanhoDaExportacaoDaComposicao.mimePeloNome(item.nomeDoArquivo),
        )
        return item
    }

    fun arquivo(nome: String): File? {
        if (nome.isBlank() || nome.contains('/') || nome.contains('\\')) return null
        val arquivo = File(pasta, nome)
        return if (arquivo.isFile) arquivo else null
    }

    fun ler(nome: String): ByteArray? = arquivo(nome)?.readBytes()

    fun apagar(nome: String) {
        arquivo(nome)?.delete()
    }

    private fun referenciasDeTextoNoArrayJson(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val textos = mutableListOf<String>()
        for (indice in 0 until array.length()) {
            val texto = array.optString(indice)
            if (texto.isNotBlank()) textos += texto
        }
        return textos
    }

    companion object {
        const val LARGURA_DO_QUADRO = 1920
        private const val CHAVE_IMPORTACAO = "ultimo_quadro_copiado_importado"
    }
}
