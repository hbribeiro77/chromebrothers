# Task 1 review package (no git — full file snapshots)



===== FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\OrigemDaMidiaNaBibliotecaDoNavegador.kt =====

package com.chromebrothers.navegador.regras

import java.util.Locale

object OrigemDaMidiaNaBibliotecaDoNavegador {
    const val HIGGSFIELD = "higgsfield"
    const val SJINN = "sjinn"
    const val TOPVIEW = "topview"
    const val PAGINA = "pagina"
    const val DESCONHECIDO = "desconhecido"

    private val conhecidas = setOf(HIGGSFIELD, SJINN, TOPVIEW, PAGINA)

    fun parse(bruto: String?): String {
        val normalizada = bruto?.trim()?.lowercase(Locale.ROOT).orEmpty()
        return if (normalizada in conhecidas) normalizada else DESCONHECIDO
    }

    fun rotulo(origem: String): String = when (parse(origem)) {
        HIGGSFIELD -> "Higgsfield"
        SJINN -> "Sjinn"
        TOPVIEW -> "Topview"
        PAGINA -> "Página"
        else -> "Desconhecido"
    }
}



===== FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\FiltroPorOrigemNaBibliotecaDeMidia.kt =====

package com.chromebrothers.navegador.regras

object FiltroPorOrigemNaBibliotecaDeMidia {
    fun filtrarVideos(
        itens: List<ItemDaBibliotecaDeVideos>,
        origemOuVazio: String,
    ): List<ItemDaBibliotecaDeVideos> {
        if (semFiltro(origemOuVazio)) return itens
        val origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(origemOuVazio)
        return itens.filter { OrigemDaMidiaNaBibliotecaDoNavegador.parse(it.origem) == origem }
    }

    fun filtrarImagens(
        itens: List<ItemDaBibliotecaDeImagensCapturadas>,
        origemOuVazio: String,
    ): List<ItemDaBibliotecaDeImagensCapturadas> {
        if (semFiltro(origemOuVazio)) return itens
        val origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(origemOuVazio)
        return itens.filter { OrigemDaMidiaNaBibliotecaDoNavegador.parse(it.origem) == origem }
    }

    private fun semFiltro(origemOuVazio: String): Boolean {
        val texto = origemOuVazio.trim()
        return texto.isEmpty() || texto.equals("todos", ignoreCase = true)
    }
}



===== FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\BibliotecaDeVideosDoNavegador.kt =====

package com.chromebrothers.navegador.regras

data class ItemDaBibliotecaDeVideos(
    val chave: String,
    val titulo: String,
    val enderecoParaAbrir: String,
    val abreNoPlayerDeArquivo: Boolean,
    val favorito: Boolean,
    val instante: Long,
    val nomeDaMiniatura: String,
    val foraDoHistorico: Boolean = false,
    val origem: String = OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO,
    val modelo: String = "",
    val prompt: String = "",
    val refs: List<String> = emptyList(),
    val caminhoLocal: String = "",
    val exportado: Boolean = false,
)

data class ResultadoDaRemocaoDoHistoricoDeVideos(
    val apagouOItem: Boolean,
    val nomeDaMiniatura: String,
)

class BibliotecaDeVideosDoNavegador(
    inicial: List<ItemDaBibliotecaDeVideos> = emptyList(),
    private val limiteDeRecentes: Int = 40,
) {
    private val itens = inicial.toMutableList()

    init {
        val vistos = mutableSetOf<String>()
        val unicos = mutableListOf<ItemDaBibliotecaDeVideos>()
        itens.forEach { item ->
            if (item.chave in vistos) return@forEach
            vistos += item.chave
            unicos += item
        }
        itens.clear()
        itens.addAll(unicos)
        aparar()
    }

    fun listar(): List<ItemDaBibliotecaDeVideos> = itens.toList()

    fun recentes(): List<ItemDaBibliotecaDeVideos> {
        return itens.filter { !it.foraDoHistorico }.take(limiteDeRecentes)
    }

    fun favoritos(): List<ItemDaBibliotecaDeVideos> = itens.filter { it.favorito }

    fun buscar(chave: String): ItemDaBibliotecaDeVideos? = itens.find { it.chave == chave }

    fun substituirPor(novos: List<ItemDaBibliotecaDeVideos>) {
        itens.clear()
        itens.addAll(novos)
        val vistos = mutableSetOf<String>()
        val unicos = mutableListOf<ItemDaBibliotecaDeVideos>()
        itens.forEach { item ->
            if (item.chave in vistos) return@forEach
            vistos += item.chave
            unicos += item
        }
        itens.clear()
        itens.addAll(unicos)
        aparar()
    }

    fun registrar(
        chave: String,
        titulo: String,
        enderecoParaAbrir: String,
        abreNoPlayerDeArquivo: Boolean,
        instante: Long,
        origem: String? = null,
        modelo: String? = null,
        prompt: String? = null,
        refs: List<String>? = null,
        caminhoLocal: String? = null,
    ): ItemDaBibliotecaDeVideos {
        val anterior = itens.find { it.chave == chave }
        itens.removeAll { it.chave == chave }
        val item = ItemDaBibliotecaDeVideos(
            chave = chave,
            titulo = titulo.trim().ifBlank { anterior?.titulo.orEmpty() }.ifBlank { enderecoParaAbrir },
            enderecoParaAbrir = enderecoParaAbrir,
            abreNoPlayerDeArquivo = abreNoPlayerDeArquivo,
            favorito = anterior?.favorito ?: false,
            instante = instante,
            nomeDaMiniatura = anterior?.nomeDaMiniatura.orEmpty(),
            foraDoHistorico = false,
            origem = origem?.let { OrigemDaMidiaNaBibliotecaDoNavegador.parse(it) }
                ?: anterior?.origem
                ?: OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO,
            modelo = modelo ?: anterior?.modelo.orEmpty(),
            prompt = prompt ?: anterior?.prompt.orEmpty(),
            refs = refs?.toList() ?: anterior?.refs.orEmpty(),
            caminhoLocal = caminhoLocal ?: anterior?.caminhoLocal.orEmpty(),
            exportado = anterior?.exportado ?: false,
        )
        itens.add(0, item)
        aparar()
        return itens.first { it.chave == chave }
    }

    fun definirFavorito(chave: String, favorito: Boolean): ItemDaBibliotecaDeVideos? {
        val indice = itens.indexOfFirst { it.chave == chave }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(favorito = favorito)
        aparar()
        return itens.find { it.chave == chave }
    }

    fun removerDoHistorico(chave: String): ResultadoDaRemocaoDoHistoricoDeVideos? {
        val indice = itens.indexOfFirst { it.chave == chave }
        if (indice < 0) return null
        val item = itens[indice]
        if (item.favorito) {
            itens[indice] = item.copy(foraDoHistorico = true)
            return ResultadoDaRemocaoDoHistoricoDeVideos(
                apagouOItem = false,
                nomeDaMiniatura = item.nomeDaMiniatura,
            )
        }
        itens.removeAt(indice)
        return ResultadoDaRemocaoDoHistoricoDeVideos(
            apagouOItem = true,
            nomeDaMiniatura = item.nomeDaMiniatura,
        )
    }

    fun definirMiniatura(chave: String, nomeDaMiniatura: String): ItemDaBibliotecaDeVideos? {
        val indice = itens.indexOfFirst { it.chave == chave }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(nomeDaMiniatura = nomeDaMiniatura)
        return itens[indice]
    }

    fun marcarExportado(chave: String): ItemDaBibliotecaDeVideos? {
        val indice = itens.indexOfFirst { it.chave == chave }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(exportado = true)
        return itens[indice]
    }

    private fun aparar() {
        val noHistorico = itens.filter { !it.foraDoHistorico }.take(limiteDeRecentes).map { it.chave }.toSet()
        itens.removeAll { !it.favorito && it.chave !in noHistorico }
    }
}



===== FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\BibliotecaDeImagensCapturadasDoNavegador.kt =====

package com.chromebrothers.navegador.regras

data class ItemDaBibliotecaDeImagensCapturadas(
    val id: String,
    val instante: Long,
    val nomeDoArquivo: String,
    val naGaleria: Boolean,
    val favorito: Boolean,
    val foraDoHistorico: Boolean = false,
    val uriDaGaleria: String? = null,
    val nomeExibido: String? = null,
    val origem: String = OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO,
    val modelo: String = "",
    val prompt: String = "",
    val refs: List<String> = emptyList(),
    val caminhoLocal: String = "",
    val exportado: Boolean = false,
)

data class ResultadoDaRemocaoDoHistoricoDeImagens(
    val apagouOItem: Boolean,
    val nomeDoArquivo: String,
)

class BibliotecaDeImagensCapturadasDoNavegador(
    inicial: List<ItemDaBibliotecaDeImagensCapturadas> = emptyList(),
    private val limiteDoHistorico: Int = 40,
) {
    private val itens = inicial.toMutableList()

    init {
        val vistos = mutableSetOf<String>()
        val unicos = mutableListOf<ItemDaBibliotecaDeImagensCapturadas>()
        itens.forEach { item ->
            if (item.id in vistos) return@forEach
            vistos += item.id
            unicos += item
        }
        itens.clear()
        itens.addAll(unicos)
        aparar()
    }

    fun listar(): List<ItemDaBibliotecaDeImagensCapturadas> = itens.toList()

    fun historico(): List<ItemDaBibliotecaDeImagensCapturadas> {
        return itens.filter { !it.foraDoHistorico }.take(limiteDoHistorico)
    }

    fun favoritos(): List<ItemDaBibliotecaDeImagensCapturadas> = itens.filter { it.favorito }

    fun copiadasSemBaixar(): List<ItemDaBibliotecaDeImagensCapturadas> = itens.filter { !it.naGaleria }

    fun buscar(id: String): ItemDaBibliotecaDeImagensCapturadas? = itens.find { it.id == id }

    fun substituirPor(novos: List<ItemDaBibliotecaDeImagensCapturadas>) {
        itens.clear()
        itens.addAll(novos)
        val vistos = mutableSetOf<String>()
        val unicos = mutableListOf<ItemDaBibliotecaDeImagensCapturadas>()
        itens.forEach { item ->
            if (item.id in vistos) return@forEach
            vistos += item.id
            unicos += item
        }
        itens.clear()
        itens.addAll(unicos)
        aparar()
    }

    fun registrar(
        id: String,
        instante: Long,
        nomeDoArquivo: String,
        naGaleria: Boolean,
        origem: String? = null,
        modelo: String? = null,
        prompt: String? = null,
        refs: List<String>? = null,
        caminhoLocal: String? = null,
    ): ItemDaBibliotecaDeImagensCapturadas {
        val anterior = itens.find { it.id == id }
        itens.removeAll { it.id == id }
        val item = ItemDaBibliotecaDeImagensCapturadas(
            id = id,
            instante = instante,
            nomeDoArquivo = nomeDoArquivo,
            naGaleria = naGaleria,
            favorito = anterior?.favorito ?: false,
            foraDoHistorico = false,
            uriDaGaleria = anterior?.uriDaGaleria,
            nomeExibido = anterior?.nomeExibido,
            origem = origem?.let { OrigemDaMidiaNaBibliotecaDoNavegador.parse(it) }
                ?: anterior?.origem
                ?: OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO,
            modelo = modelo ?: anterior?.modelo.orEmpty(),
            prompt = prompt ?: anterior?.prompt.orEmpty(),
            refs = refs?.toList() ?: anterior?.refs.orEmpty(),
            caminhoLocal = caminhoLocal ?: anterior?.caminhoLocal.orEmpty(),
            exportado = anterior?.exportado ?: false,
        )
        itens.add(0, item)
        aparar()
        return itens.first { it.id == id }
    }

    fun definirFavorito(id: String, favorito: Boolean): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(favorito = favorito)
        aparar()
        return itens.find { it.id == id }
    }

    fun marcarComoBaixada(id: String): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(naGaleria = true)
        return itens[indice]
    }

    fun vincularGaleria(id: String, uri: String, nome: String): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0 || uri.isBlank() || nome.isBlank()) return null
        itens[indice] = itens[indice].copy(naGaleria = true, uriDaGaleria = uri, nomeExibido = nome)
        return itens[indice]
    }

    fun marcarExportado(id: String): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        itens[indice] = itens[indice].copy(exportado = true)
        return itens[indice]
    }

    fun renomear(id: String, nome: String): ItemDaBibliotecaDeImagensCapturadas? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0 || nome.isBlank()) return null
        val item = itens[indice]
        if (item.uriDaGaleria.isNullOrBlank()) return null
        itens[indice] = item.copy(nomeExibido = nome)
        return itens[indice]
    }

    fun removerDoHistorico(id: String): ResultadoDaRemocaoDoHistoricoDeImagens? {
        val indice = itens.indexOfFirst { it.id == id }
        if (indice < 0) return null
        val item = itens[indice]
        if (item.favorito) {
            itens[indice] = item.copy(foraDoHistorico = true)
            return ResultadoDaRemocaoDoHistoricoDeImagens(
                apagouOItem = false,
                nomeDoArquivo = item.nomeDoArquivo,
            )
        }
        itens.removeAt(indice)
        return ResultadoDaRemocaoDoHistoricoDeImagens(
            apagouOItem = true,
            nomeDoArquivo = item.nomeDoArquivo,
        )
    }

    private fun aparar() {
        val noHistorico = itens.filter { !it.foraDoHistorico }.take(limiteDoHistorico).map { it.id }.toSet()
        itens.removeAll { !it.favorito && it.id !in noHistorico }
    }
}



===== FILE: regras-de-video-do-navegador\src\test\kotlin\com\chromebrothers\navegador\regras\TesteDaOrigemEFiltroNaBibliotecaDeMidia.kt =====

package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaOrigemEFiltroNaBibliotecaDeMidia {
    @Test
    fun parseOrigemVaziaViraDesconhecido() {
        assertEquals("desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.parse(null))
        assertEquals("desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.parse(""))
        assertEquals("topview", OrigemDaMidiaNaBibliotecaDoNavegador.parse("Topview"))
        assertEquals("desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.parse("outra-coisa"))
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD, "higgsfield")
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.SJINN, "sjinn")
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.TOPVIEW, "topview")
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.PAGINA, "pagina")
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO, "desconhecido")
    }

    @Test
    fun rotuloDaOrigem() {
        assertEquals("Higgsfield", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("higgsfield"))
        assertEquals("Sjinn", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("sjinn"))
        assertEquals("Topview", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("TOPVIEW"))
        assertEquals("Página", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("pagina"))
        assertEquals("Desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("nao-existe"))
        assertEquals("Desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo(""))
    }

    @Test
    fun filtroPorOrigem() {
        val a = ItemDaBibliotecaDeVideos("1", "t", "file://x", true, false, 1L, "", origem = "sjinn")
        val b = a.copy(chave = "2", origem = "topview")
        assertEquals(listOf(a), FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), "sjinn"))
        assertEquals(2, FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), "todos").size)
        assertEquals(listOf(a, b), FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), ""))
        assertEquals(listOf(a, b), FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), "   "))
    }

    @Test
    fun filtroDeImagensPorOrigem() {
        val higgsfield = ItemDaBibliotecaDeImagensCapturadas(
            id = "1",
            instante = 1L,
            nomeDoArquivo = "a.jpg",
            naGaleria = false,
            favorito = false,
            origem = "higgsfield",
        )
        val pagina = higgsfield.copy(id = "2", origem = "pagina")
        val itens = listOf(higgsfield, pagina)
        assertEquals(listOf(higgsfield), FiltroPorOrigemNaBibliotecaDeMidia.filtrarImagens(itens, "higgsfield"))
        assertEquals(itens, FiltroPorOrigemNaBibliotecaDeMidia.filtrarImagens(itens, "todos"))
        assertEquals(itens, FiltroPorOrigemNaBibliotecaDeMidia.filtrarImagens(itens, ""))
    }

    @Test
    fun registrarGuardaPromptEOrigem() {
        val bib = BibliotecaDeVideosDoNavegador()
        bib.registrar("sjinn-1", "curto", "file://a", true, 9L, origem = "sjinn", modelo = "wan3", prompt = "onda grande", refs = listOf("r1.jpg"), caminhoLocal = "/a.mp4")
        val item = bib.buscar("sjinn-1")!!
        assertEquals("sjinn", item.origem)
        assertEquals("onda grande", item.prompt)
        assertEquals(listOf("r1.jpg"), item.refs)
        assertEquals("wan3", item.modelo)
        assertEquals("/a.mp4", item.caminhoLocal)
        assertFalse(item.exportado)
    }

    @Test
    fun registrarSemMetadadosNasceDesconhecidoEVazio() {
        val video = ItemDaBibliotecaDeVideos("1", "t", "file://x", true, false, 1L, "")
        assertEquals("desconhecido", video.origem)
        assertEquals("", video.modelo)
        assertEquals("", video.prompt)
        assertEquals(emptyList(), video.refs)
        assertEquals("", video.caminhoLocal)
        assertFalse(video.exportado)
        val registrado = BibliotecaDeVideosDoNavegador().registrar("k", "t", "file://x", false, 1L)
        assertEquals("desconhecido", registrado.origem)
        assertEquals("", registrado.prompt)
        assertFalse(registrado.exportado)
    }

    @Test
    fun registrarDeNovoSemArgumentosNovosPreservaMetadadosEOrigemExplicitaAtualiza() {
        val bib = BibliotecaDeVideosDoNavegador()
        bib.registrar(
            "k",
            "titulo",
            "file://a",
            true,
            1L,
            origem = "sjinn",
            prompt = "onda",
            refs = listOf("r1.jpg"),
            caminhoLocal = "/a.mp4",
        )
        val deNovo = bib.registrar("k", "titulo novo", "file://b", false, 2L)
        assertEquals("sjinn", deNovo.origem)
        assertEquals("onda", deNovo.prompt)
        assertEquals(listOf("r1.jpg"), deNovo.refs)
        assertEquals("/a.mp4", deNovo.caminhoLocal)
        val atualizado = bib.registrar("k", "titulo novo", "file://b", false, 3L, origem = "Topview")
        assertEquals("topview", atualizado.origem)
        assertEquals("onda", atualizado.prompt)
        assertEquals(listOf("r1.jpg"), atualizado.refs)
        assertEquals("/a.mp4", atualizado.caminhoLocal)
    }

    @Test
    fun registrarImagemDeNovoSemArgumentosNovosPreservaMetadadosEOrigemExplicitaAtualiza() {
        val imagens = BibliotecaDeImagensCapturadasDoNavegador()
        imagens.registrar(
            "img-1",
            1L,
            "a.jpg",
            false,
            origem = "higgsfield",
            prompt = "gato",
            refs = listOf("r.png"),
            caminhoLocal = "/a.jpg",
        )
        val deNovo = imagens.registrar("img-1", 2L, "b.jpg", true)
        assertEquals("higgsfield", deNovo.origem)
        assertEquals("gato", deNovo.prompt)
        assertEquals(listOf("r.png"), deNovo.refs)
        assertEquals("/a.jpg", deNovo.caminhoLocal)
        val atualizado = imagens.registrar("img-1", 3L, "b.jpg", true, origem = "pagina")
        assertEquals("pagina", atualizado.origem)
        assertEquals("gato", atualizado.prompt)
        assertEquals(listOf("r.png"), atualizado.refs)
        assertEquals("/a.jpg", atualizado.caminhoLocal)
    }

    @Test
    fun registrarDeNovoMantemFavoritoMiniaturaEExportado() {
        val bib = BibliotecaDeVideosDoNavegador()
        bib.registrar("k", "antigo", "file://a", true, 1L, origem = "sjinn", prompt = "antes", caminhoLocal = "/a.mp4")
        bib.definirFavorito("k", true)
        bib.definirMiniatura("k", "mini.jpg")
        bib.marcarExportado("k")
        val deNovo = bib.registrar(
            "k",
            "novo",
            "file://b",
            true,
            2L,
            origem = "Topview",
            modelo = "wan",
            prompt = "depois",
            refs = listOf("n.jpg"),
            caminhoLocal = "/b.mp4",
        )
        assertEquals("novo", deNovo.titulo)
        assertTrue(deNovo.favorito)
        assertEquals("mini.jpg", deNovo.nomeDaMiniatura)
        assertTrue(deNovo.exportado)
        assertEquals("topview", deNovo.origem)
        assertEquals("depois", deNovo.prompt)
        assertEquals(listOf("n.jpg"), deNovo.refs)
        assertEquals("/b.mp4", deNovo.caminhoLocal)
    }

    @Test
    fun favoritoMiniaturaERemocaoNaoApagamMetadadosDoVideo() {
        val bib = BibliotecaDeVideosDoNavegador()
        bib.registrar(
            "k",
            "t",
            "file://a",
            true,
            1L,
            origem = "pagina",
            modelo = "m",
            prompt = "oi",
            refs = listOf("r"),
            caminhoLocal = "/a",
        )
        bib.definirFavorito("k", true)
        bib.definirMiniatura("k", "m.jpg")
        val favorito = bib.buscar("k")!!
        assertEquals("pagina", favorito.origem)
        assertEquals("oi", favorito.prompt)
        assertEquals(listOf("r"), favorito.refs)
        assertEquals("m.jpg", favorito.nomeDaMiniatura)
        val remocao = bib.removerDoHistorico("k")
        assertFalse(remocao!!.apagouOItem)
        val fora = bib.buscar("k")!!
        assertTrue(fora.foraDoHistorico)
        assertEquals("oi", fora.prompt)
        assertEquals("/a", fora.caminhoLocal)
        assertEquals("m", fora.modelo)
    }

    @Test
    fun marcarExportadoNoVideo() {
        val videos = BibliotecaDeVideosDoNavegador()
        videos.registrar("v", "t", "file://v", true, 1L, caminhoLocal = "/v.mp4")
        assertFalse(videos.buscar("v")!!.exportado)
        assertNull(videos.marcarExportado("nao-existe"))
        val marcado = videos.marcarExportado("v")
        assertTrue(marcado!!.exportado)
        assertEquals("/v.mp4", marcado.caminhoLocal)
        assertEquals("desconhecido", marcado.origem)
        assertTrue(videos.buscar("v")!!.exportado)
    }

    @Test
    fun registrarImagemGuardaPromptEOrigemEMarcarExportado() {
        val imagens = BibliotecaDeImagensCapturadasDoNavegador()
        val criada = imagens.registrar(
            "img-1",
            3L,
            "a.jpg",
            false,
            origem = "Higgsfield",
            modelo = "flux",
            prompt = "gato",
            refs = listOf("r.png"),
            caminhoLocal = "/a.jpg",
        )
        assertEquals("higgsfield", criada.origem)
        assertEquals("flux", criada.modelo)
        assertEquals("gato", criada.prompt)
        assertEquals(listOf("r.png"), criada.refs)
        assertEquals("/a.jpg", criada.caminhoLocal)
        assertFalse(criada.exportado)
        val solta = ItemDaBibliotecaDeImagensCapturadas("z", 1L, "z.jpg", false, false)
        assertEquals("desconhecido", solta.origem)
        assertEquals("", solta.prompt)
        assertEquals(emptyList(), solta.refs)
        assertEquals("", solta.caminhoLocal)
        assertFalse(solta.exportado)
        assertNull(imagens.marcarExportado("nao"))
        val marcada = imagens.marcarExportado("img-1")
        assertTrue(marcada!!.exportado)
        assertEquals("gato", marcada.prompt)
        imagens.definirFavorito("img-1", true)
        val favorita = imagens.buscar("img-1")!!
        assertTrue(favorita.favorito)
        assertTrue(favorita.exportado)
        assertEquals("higgsfield", favorita.origem)
        assertEquals(listOf("r.png"), favorita.refs)
    }
}

