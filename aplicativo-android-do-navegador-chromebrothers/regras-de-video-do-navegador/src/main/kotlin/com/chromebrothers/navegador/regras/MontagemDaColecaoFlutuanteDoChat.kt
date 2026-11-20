package com.chromebrothers.navegador.regras

import java.net.URI

enum class TipoDoItemDaColecaoFlutuante { VIDEO, IMAGEM }

enum class TamanhoDoItemDaColecaoFlutuante { PEQUENO, MEDIO, GRANDE }

data class ItemDaColecaoFlutuante(
    val tipo: TipoDoItemDaColecaoFlutuante,
    val endereco: String,
    val titulo: String,
    val nota: String,
    val tamanho: TamanhoDoItemDaColecaoFlutuante = TamanhoDoItemDaColecaoFlutuante.PEQUENO,
)

data class AjusteDaOrdemDaColecao(
    val endereco: String,
    val tamanho: TamanhoDoItemDaColecaoFlutuante?,
)

sealed class ResultadoDaListaDaColecao {
    data class Pronta(val itens: List<ItemDaColecaoFlutuante>) : ResultadoDaListaDaColecao()
    data class Recusada(val mensagem: String) : ResultadoDaListaDaColecao()
}

data class ResultadoDoAcrescimoDaColecao(
    val itens: List<ItemDaColecaoFlutuante>,
    val acrescentados: Int,
    val jaEstavam: Int,
    val cortados: Int,
    val mensagem: String,
)

sealed class ResultadoDaOrdemDaColecao {
    data class Pronta(val ajustes: List<AjusteDaOrdemDaColecao>) : ResultadoDaOrdemDaColecao()
    data class Recusada(val mensagem: String) : ResultadoDaOrdemDaColecao()
}

data class ResultadoDaOrganizacaoDaColecao(
    val itens: List<ItemDaColecaoFlutuante>,
    val mensagem: String,
)

sealed class ResultadoDosEnderecosParaTirarDaColecao {
    data class Prontos(val enderecos: List<String>) : ResultadoDosEnderecosParaTirarDaColecao()
    data class Recusada(val mensagem: String) : ResultadoDosEnderecosParaTirarDaColecao()
}

data class ResultadoDaRemocaoDaColecao(
    val itens: List<ItemDaColecaoFlutuante>,
    val mensagem: String,
)

object MontagemDaColecaoFlutuanteDoChat {
    const val LIMITE = 40
    const val LIMITE_DA_LEITURA = 80
    const val LIMITE_DO_TITULO = 120
    const val LIMITE_DA_NOTA = 500
    const val LIMITE_DO_ENDERECO = 2000

    fun ler(argumentos: String): ResultadoDaListaDaColecao {
        val objetos = TextoJsonDoChat.objetosDaChave(argumentos, "itens")?.take(LIMITE_DA_LEITURA)
            ?: return ResultadoDaListaDaColecao.Recusada("Faltou a lista.")
        val itens = LinkedHashMap<String, ItemDaColecaoFlutuante>()
        objetos.forEach { objeto ->
            val item = itemDe(objeto) ?: return@forEach
            if (item.endereco !in itens) itens[item.endereco] = item
        }
        if (itens.isEmpty()) return ResultadoDaListaDaColecao.Recusada("Nenhum item válido.")
        return ResultadoDaListaDaColecao.Pronta(itens.values.toList())
    }

    fun acrescentar(
        atuais: List<ItemDaColecaoFlutuante>,
        novos: List<ItemDaColecaoFlutuante>,
    ): ResultadoDoAcrescimoDaColecao {
        val itens = atuais.toMutableList()
        var acrescentados = 0
        var jaEstavam = 0
        var cortados = 0
        novos.forEach { item ->
            when {
                itens.any { it.endereco == item.endereco } -> jaEstavam++
                itens.size >= LIMITE -> cortados++
                else -> {
                    itens += item
                    acrescentados++
                }
            }
        }
        val resultado = ResultadoDoAcrescimoDaColecao(itens, acrescentados, jaEstavam, cortados, "")
        return resultado.copy(mensagem = mensagem(resultado))
    }

    fun pergunta(itens: List<ItemDaColecaoFlutuante>): String {
        return "Montar a coleção?\n${textoDasLinhas(itens.map { "- ${rotuloDoItem(it)}" })}"
    }

    fun lerOrdem(argumentos: String): ResultadoDaOrdemDaColecao {
        val objetos = TextoJsonDoChat.objetosDaChave(argumentos, "ordem")?.take(LIMITE_DA_LEITURA)
            ?: return ResultadoDaOrdemDaColecao.Recusada("Faltou a ordem.")
        if (objetos.isEmpty()) return ResultadoDaOrdemDaColecao.Recusada("Faltou a ordem.")
        val ajustes = LinkedHashMap<String, AjusteDaOrdemDaColecao>()
        objetos.forEach { objeto ->
            val endereco = TextoJsonDoChat.valorDaChave(objeto, "endereco")?.trim().orEmpty()
            if (!enderecoHttp(endereco) || endereco in ajustes) return@forEach
            ajustes[endereco] = AjusteDaOrdemDaColecao(endereco, tamanhoDe(TextoJsonDoChat.valorDaChave(objeto, "tamanho")))
        }
        if (ajustes.isEmpty()) return ResultadoDaOrdemDaColecao.Recusada("Nenhum endereço válido.")
        return ResultadoDaOrdemDaColecao.Pronta(ajustes.values.toList())
    }

    fun organizar(
        atuais: List<ItemDaColecaoFlutuante>,
        ajustes: List<AjusteDaOrdemDaColecao>,
    ): ResultadoDaOrganizacaoDaColecao {
        val porEndereco = atuais.associateBy { it.endereco }
        val usados = linkedSetOf<String>()
        val frente = mutableListOf<ItemDaColecaoFlutuante>()
        ajustes.forEach { ajuste ->
            val atual = porEndereco[ajuste.endereco] ?: return@forEach
            if (ajuste.endereco in usados) return@forEach
            usados += ajuste.endereco
            frente += atual.copy(tamanho = ajuste.tamanho ?: atual.tamanho)
        }
        if (frente.isEmpty()) {
            return ResultadoDaOrganizacaoDaColecao(atuais, "Nenhum desses endereços está na coleção.")
        }
        val itens = frente + atuais.filter { it.endereco !in usados }
        return ResultadoDaOrganizacaoDaColecao(itens, "Reorganizei a coleção. Agora são ${itens.size}.")
    }

    fun perguntaDaOrdem(ajustes: List<AjusteDaOrdemDaColecao>): String {
        val linhas = ajustes.map { ajuste ->
            val tamanho = when (ajuste.tamanho) {
                TamanhoDoItemDaColecaoFlutuante.PEQUENO -> "pequeno "
                TamanhoDoItemDaColecaoFlutuante.MEDIO -> "médio "
                TamanhoDoItemDaColecaoFlutuante.GRANDE -> "grande "
                null -> ""
            }
            "- $tamanho${ajuste.endereco}"
        }
        return "Reorganizar a coleção? O que não estiver na lista fica no fim.\n${textoDasLinhas(linhas)}"
    }

    fun lerEnderecosParaTirar(argumentos: String): ResultadoDosEnderecosParaTirarDaColecao {
        val textos = TextoJsonDoChat.textosDaChave(argumentos, "enderecos")?.take(LIMITE_DA_LEITURA)
            ?: return ResultadoDosEnderecosParaTirarDaColecao.Recusada("Faltou o endereço.")
        if (textos.isEmpty()) return ResultadoDosEnderecosParaTirarDaColecao.Recusada("Faltou o endereço.")
        val enderecos = LinkedHashSet<String>()
        textos.forEach { bruto ->
            val endereco = bruto.trim()
            if (enderecoHttp(endereco)) enderecos += endereco
        }
        if (enderecos.isEmpty()) return ResultadoDosEnderecosParaTirarDaColecao.Recusada("Nenhum endereço válido.")
        return ResultadoDosEnderecosParaTirarDaColecao.Prontos(enderecos.toList())
    }

    fun tirar(atuais: List<ItemDaColecaoFlutuante>, enderecos: List<String>): ResultadoDaRemocaoDaColecao {
        val pedidos = enderecos.toSet()
        val itens = atuais.filter { it.endereco !in pedidos }
        val removidos = atuais.size - itens.size
        if (removidos == 0) {
            return ResultadoDaRemocaoDaColecao(atuais, "Nenhum desses itens está na coleção.")
        }
        val palavra = if (removidos == 1) "item" else "itens"
        val fim = if (itens.isEmpty()) "A coleção ficou vazia." else "Agora são ${itens.size}."
        return ResultadoDaRemocaoDaColecao(itens, "Tirei $removidos $palavra. $fim")
    }

    fun perguntaDeTirar(enderecos: List<String>): String {
        return "Tirar da coleção?\n${textoDasLinhas(enderecos.map { "- $it" })}"
    }

    fun descrever(itens: List<ItemDaColecaoFlutuante>): String {
        if (itens.isEmpty()) return "A coleção está vazia."
        val linhas = itens.joinToString("\n") { item ->
            "- ${rotulo(item.tipo)} ${nomeDoTamanho(item.tamanho)} ${item.endereco} — ${item.titulo}"
        }
        return "Coleção agora:\n$linhas"
    }

    fun tituloCurto(endereco: String, reserva: String): String {
        val caminho = try {
            URI(endereco).path
        } catch (_: Exception) {
            null
        }
        val nome = caminho?.substringAfterLast('/')?.trim().orEmpty()
        return nome.take(80).ifBlank { reserva }
    }

    fun tamanhoDe(texto: String?): TamanhoDoItemDaColecaoFlutuante? = when (texto?.trim()?.lowercase()) {
        "pequeno", "pequena" -> TamanhoDoItemDaColecaoFlutuante.PEQUENO
        "medio", "médio", "media", "média" -> TamanhoDoItemDaColecaoFlutuante.MEDIO
        "grande" -> TamanhoDoItemDaColecaoFlutuante.GRANDE
        else -> null
    }

    private fun mensagem(resultado: ResultadoDoAcrescimoDaColecao): String {
        if (resultado.acrescentados == 0) {
            return if (resultado.cortados > 0) {
                "A coleção chegou ao limite."
            } else {
                "Esses itens já estavam na coleção."
            }
        }
        val palavra = if (resultado.acrescentados == 1) "item" else "itens"
        val partes = mutableListOf(
            "Coloquei ${resultado.acrescentados} $palavra na coleção. Agora são ${resultado.itens.size}.",
        )
        if (resultado.jaEstavam > 0) {
            val verbo = if (resultado.jaEstavam == 1) "já estava" else "já estavam"
            partes += "${resultado.jaEstavam} $verbo na coleção."
        }
        if (resultado.cortados > 0) {
            partes += "Deixei ${resultado.cortados} de fora porque a coleção cabe $LIMITE."
        }
        return partes.joinToString(" ")
    }

    private fun itemDe(objeto: String): ItemDaColecaoFlutuante? {
        val tipo = when (TextoJsonDoChat.valorDaChave(objeto, "tipo")?.trim()?.lowercase()) {
            "video", "vídeo" -> TipoDoItemDaColecaoFlutuante.VIDEO
            "imagem", "image" -> TipoDoItemDaColecaoFlutuante.IMAGEM
            else -> return null
        }
        val endereco = TextoJsonDoChat.valorDaChave(objeto, "endereco")?.trim().orEmpty()
        if (!enderecoPermitido(endereco, tipo)) return null
        val titulo = TextoJsonDoChat.valorDaChave(objeto, "titulo")
            ?.trim()
            ?.replace('\n', ' ')
            ?.take(LIMITE_DO_TITULO)
            .orEmpty()
            .ifBlank { "Sem título" }
        val nota = TextoJsonDoChat.valorDaChave(objeto, "nota")?.trim()?.take(LIMITE_DA_NOTA).orEmpty()
        val tamanho = tamanhoDe(TextoJsonDoChat.valorDaChave(objeto, "tamanho"))
            ?: TamanhoDoItemDaColecaoFlutuante.PEQUENO
        return ItemDaColecaoFlutuante(tipo, endereco, titulo, nota, tamanho)
    }

    private fun enderecoPermitido(endereco: String, tipo: TipoDoItemDaColecaoFlutuante): Boolean {
        if (endereco.length > LIMITE_DO_ENDERECO) return false
        if (endereco.startsWith("http://") || endereco.startsWith("https://")) return true
        return tipo == TipoDoItemDaColecaoFlutuante.IMAGEM && endereco.startsWith("file:")
    }

    private fun enderecoHttp(endereco: String): Boolean {
        if (endereco.length > LIMITE_DO_ENDERECO) return false
        return endereco.startsWith("http://") ||
            endereco.startsWith("https://") ||
            endereco.startsWith("file:")
    }

    private fun textoDasLinhas(linhas: List<String>): String {
        val visiveis = linhas.take(6).joinToString("\n")
        val resto = linhas.size - minOf(linhas.size, 6)
        val fim = if (resto > 0) "\ne mais $resto." else ""
        return visiveis + fim
    }

    private fun rotuloDoItem(item: ItemDaColecaoFlutuante): String {
        val tamanho = when (item.tamanho) {
            TamanhoDoItemDaColecaoFlutuante.PEQUENO -> ""
            TamanhoDoItemDaColecaoFlutuante.MEDIO -> " médio"
            TamanhoDoItemDaColecaoFlutuante.GRANDE -> " grande"
        }
        return "${rotulo(item.tipo)}$tamanho: ${item.titulo}"
    }

    private fun nomeDoTamanho(tamanho: TamanhoDoItemDaColecaoFlutuante): String = when (tamanho) {
        TamanhoDoItemDaColecaoFlutuante.PEQUENO -> "pequeno"
        TamanhoDoItemDaColecaoFlutuante.MEDIO -> "medio"
        TamanhoDoItemDaColecaoFlutuante.GRANDE -> "grande"
    }

    private fun rotulo(tipo: TipoDoItemDaColecaoFlutuante): String = when (tipo) {
        TipoDoItemDaColecaoFlutuante.VIDEO -> "vídeo"
        TipoDoItemDaColecaoFlutuante.IMAGEM -> "imagem"
    }
}
