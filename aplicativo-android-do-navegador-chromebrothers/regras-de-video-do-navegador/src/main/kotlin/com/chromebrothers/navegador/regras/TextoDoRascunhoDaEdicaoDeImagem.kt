package com.chromebrothers.navegador.regras

enum class FerramentaGravadaDoRascunhoDaEdicao { VERMELHO, BRANCO, BORRACHA, RECORTE }

data class ImagemDoRascunhoDaEdicao(
    val id: Int,
    val arquivo: String,
    val esquerda: Float,
    val topo: Float,
    val largura: Float,
    val altura: Float,
)

data class TextoDoRascunhoDaEdicao(
    val id: Int,
    val texto: String,
    val tamanho: Float,
    val cor: Int,
    val esquerda: Float,
    val topo: Float,
    val largura: Float,
    val altura: Float,
)

data class PontoDoTracoDoRascunhoDaEdicao(
    val x: Float,
    val y: Float,
    val largura: Float,
    val alpha: Int = 255,
)

data class CamadaVaziaDoRascunhoDaEdicao(
    val id: Int,
    val esquerda: Float,
    val topo: Float,
    val largura: Float,
    val altura: Float,
)

data class TracoDoRascunhoDaEdicao(
    val cor: Int,
    val borracha: Boolean,
    val pontos: List<PontoDoTracoDoRascunhoDaEdicao>,
    val idDaPeca: Int = -1,
    val coordenadasDoQuadro: Boolean = false,
    val ponta: PontaDoTracoDaComposicao = PontaDoTracoDaComposicao.CANETA,
    /** Pincel do motor de carimbos (vazio = traço comum) e a semente que torna o desenho repetível. */
    val pincel: String = "",
    val semente: Long = 0L,
)

data class EspelhoGravadoDaPecaDaComposicao(
    val id: Int,
    val horizontal: Boolean,
    val vertical: Boolean,
)

data class DocumentoDoRascunhoDaEdicaoDeImagem(
    val largura: Int,
    val altura: Int,
    val proximoId: Int,
    val ferramenta: FerramentaGravadaDoRascunhoDaEdicao,
    val imagens: List<ImagemDoRascunhoDaEdicao>,
    val textos: List<TextoDoRascunhoDaEdicao>,
    val tracos: List<TracoDoRascunhoDaEdicao>,
    val corDaFolha: Int = PaletaDoEditorDeImagem.corDaFolhaInicial,
    val angulos: Map<Int, Float> = emptyMap(),
    val espelhos: List<EspelhoGravadoDaPecaDaComposicao> = emptyList(),
    val ordem: List<Int> = emptyList(),
    val camadasVazias: List<CamadaVaziaDoRascunhoDaEdicao> = emptyList(),
    /** Opacidade por camada (só as < 1; ausente = 1). */
    val opacidades: Map<Int, Float> = emptyMap(),
    /** Camadas escondidas (olho fechado no painel de camadas). */
    val ocultas: Set<Int> = emptySet(),
    /** Camadas com a transparência bloqueada (só pintam onde já tem pixel). */
    val bloqueadas: Set<Int> = emptySet(),
    /** Cópia (JSON) de cada versão de pincel do motor usada, por chave: o traço não muda se o pincel for editado. */
    val pinceis: Map<String, String> = emptyMap(),
) {
    fun arquivoDaPrimeiraImagem(): String? {
        val nome = imagens.firstOrNull()?.arquivo ?: return null
        val seguro = nome.isNotEmpty() &&
            nome.none { it == '/' || it == '\\' || it == '\u0000' } &&
            !nome.contains("..")
        return nome.takeIf { seguro }
    }
}

object IdentificadorDaPastaDoRascunhoDaEdicao {
    fun aceito(nome: String): Boolean {
        if (nome.length !in 1..32 || !nome[0].isDigit() || nome.endsWith('-')) return false
        return nome.all { it.isDigit() || it == '-' } && !nome.contains("--")
    }
}

object TextoDoRascunhoDaEdicaoDeImagem {
    fun escrever(documento: DocumentoDoRascunhoDaEdicaoDeImagem): String {
        val linhas = mutableListOf<String>()
        linhas += "v1"
        linhas += "moldura ${documento.largura} ${documento.altura}"
        linhas += "proximo ${documento.proximoId}"
        linhas += "ferramenta ${documento.ferramenta.name}"
        linhas += "folha ${documento.corDaFolha}"
        documento.angulos.forEach { (id, angulo) ->
            linhas += "giro $id ${numero(angulo)}"
        }
        documento.espelhos.forEach { espelho ->
            if (!espelho.horizontal && !espelho.vertical) return@forEach
            linhas += "espelho ${espelho.id} ${if (espelho.horizontal) 1 else 0} ${if (espelho.vertical) 1 else 0}"
        }
        if (documento.ordem.isNotEmpty()) linhas += "ordem ${documento.ordem.joinToString(" ")}"
        documento.opacidades.forEach { (id, valor) ->
            if (valor < 0.999f) linhas += "opacidade $id ${numero(valor.coerceIn(0f, 1f))}"
        }
        documento.ocultas.sorted().forEach { id -> linhas += "oculta $id" }
        documento.bloqueadas.sorted().forEach { id -> linhas += "bloqueada $id" }
        documento.camadasVazias.forEach { camada ->
            linhas += "camada ${camada.id} ${numero(camada.esquerda)} ${numero(camada.topo)} ${numero(camada.largura)} ${numero(camada.altura)}"
        }
        documento.imagens.forEach { imagem ->
            linhas += "imagem ${imagem.id} ${imagem.arquivo} ${numero(imagem.esquerda)} ${numero(imagem.topo)} ${numero(imagem.largura)} ${numero(imagem.altura)}"
        }
        documento.textos.forEach { texto ->
            linhas += "texto ${texto.id} ${numero(texto.tamanho)} ${texto.cor} ${numero(texto.esquerda)} ${numero(texto.topo)} ${numero(texto.largura)} ${numero(texto.altura)} ${escapar(texto.texto)}"
        }
        documento.pinceis.forEach { (chave, json) ->
            if (json.isNotBlank() && '\n' !in json) linhas += "pinceldef $chave $json"
        }
        documento.tracos.forEach { traco ->
            val dono = if (traco.idDaPeca >= 0) "${traco.idDaPeca} " else ""
            val ponta = if (traco.ponta == PontaDoTracoDaComposicao.CANETA) "" else " ${traco.ponta.name.lowercase()}"
            val quadro = if (traco.coordenadasDoQuadro) " quadro" else ""
            // Campos opcionais "chave=valor": o leitor separa antes de ler as posições fixas.
            val motor = if (traco.pincel.isBlank()) "" else " pincel=${traco.pincel} semente=${traco.semente}"
            linhas += "traco $dono${traco.cor} ${if (traco.borracha) 1 else 0}$ponta$quadro$motor"
            traco.pontos.forEach { ponto ->
                val claro = if (ponto.alpha == 255) "" else " ${ponto.alpha}"
                linhas += "p ${numero(ponto.x)} ${numero(ponto.y)} ${numero(ponto.largura)}$claro"
            }
        }
        return linhas.joinToString("\n")
    }

    fun ler(texto: String): DocumentoDoRascunhoDaEdicaoDeImagem? {
        val linhas = texto.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (linhas.firstOrNull() != "v1") return null
        var largura = 0
        var altura = 0
        var proximoId = 1
        var ferramenta = FerramentaGravadaDoRascunhoDaEdicao.VERMELHO
        var corDaFolha = PaletaDoEditorDeImagem.corDaFolhaInicial
        val angulos = mutableMapOf<Int, Float>()
        val espelhos = mutableListOf<EspelhoGravadoDaPecaDaComposicao>()
        val ordem = mutableListOf<Int>()
        val opacidades = mutableMapOf<Int, Float>()
        val ocultas = mutableSetOf<Int>()
        val bloqueadas = mutableSetOf<Int>()
        val imagens = mutableListOf<ImagemDoRascunhoDaEdicao>()
        val textos = mutableListOf<TextoDoRascunhoDaEdicao>()
        val camadasVazias = mutableListOf<CamadaVaziaDoRascunhoDaEdicao>()
        val tracos = mutableListOf<TracoDoRascunhoDaEdicao>()
        val pinceis = mutableMapOf<String, String>()
        var tracoAberto: MutableList<PontoDoTracoDoRascunhoDaEdicao>? = null
        var corDoTraco = 0
        var borracha = false
        var idDaPecaDoTraco = -1
        var coordenadasDoQuadro = false
        var pontaDoTraco = PontaDoTracoDaComposicao.CANETA
        var pincelDoTraco = ""
        var sementeDoTraco = 0L
        fun fecharTraco() {
            val pontos = tracoAberto ?: return
            tracos += TracoDoRascunhoDaEdicao(
                corDoTraco,
                borracha,
                pontos.toList(),
                idDaPecaDoTraco,
                coordenadasDoQuadro,
                pontaDoTraco,
                pincelDoTraco,
                sementeDoTraco,
            )
            tracoAberto = null
        }
        for (linha in linhas.drop(1)) {
            val partes = linha.split(' ')
            when (partes.firstOrNull()) {
                "moldura" -> {
                    largura = partes.getOrNull(1)?.toIntOrNull() ?: return null
                    altura = partes.getOrNull(2)?.toIntOrNull() ?: return null
                }
                "proximo" -> proximoId = partes.getOrNull(1)?.toIntOrNull() ?: return null
                "ferramenta" -> {
                    ferramenta = runCatching {
                        FerramentaGravadaDoRascunhoDaEdicao.valueOf(partes.getOrNull(1).orEmpty())
                    }.getOrNull() ?: return null
                }
                "folha" -> corDaFolha = partes.getOrNull(1)?.toIntOrNull() ?: return null
                "giro" -> {
                    val id = partes.getOrNull(1)?.toIntOrNull() ?: return null
                    angulos[id] = partes.getOrNull(2)?.toFloatOrNull() ?: return null
                }
                "espelho" -> {
                    val id = partes.getOrNull(1)?.toIntOrNull() ?: return null
                    val horizontal = partes.getOrNull(2)?.toIntOrNull() ?: return null
                    val vertical = partes.getOrNull(3)?.toIntOrNull() ?: return null
                    espelhos += EspelhoGravadoDaPecaDaComposicao(id, horizontal == 1, vertical == 1)
                }
                "ordem" -> {
                    ordem.clear()
                    partes.drop(1).forEach { token ->
                        ordem += token.toIntOrNull() ?: return null
                    }
                }
                "opacidade" -> {
                    val id = partes.getOrNull(1)?.toIntOrNull() ?: return null
                    opacidades[id] = (partes.getOrNull(2)?.toFloatOrNull() ?: return null).coerceIn(0f, 1f)
                }
                "oculta" -> ocultas += partes.getOrNull(1)?.toIntOrNull() ?: return null
                "bloqueada" -> bloqueadas += partes.getOrNull(1)?.toIntOrNull() ?: return null
                "pinceldef" -> {
                    val chave = partes.getOrNull(1).orEmpty()
                    val json = linha.substringAfter(' ').substringAfter(' ', "")
                    if (chave.isNotEmpty() && json.isNotEmpty()) pinceis[chave] = json
                }
                "camada" -> {
                    fecharTraco()
                    if (partes.size < 6) return null
                    camadasVazias += CamadaVaziaDoRascunhoDaEdicao(
                        id = partes[1].toIntOrNull() ?: return null,
                        esquerda = partes[2].toFloatOrNull() ?: return null,
                        topo = partes[3].toFloatOrNull() ?: return null,
                        largura = partes[4].toFloatOrNull() ?: return null,
                        altura = partes[5].toFloatOrNull() ?: return null,
                    )
                }
                "imagem" -> {
                    fecharTraco()
                    if (partes.size < 7) return null
                    imagens += ImagemDoRascunhoDaEdicao(
                        id = partes[1].toIntOrNull() ?: return null,
                        arquivo = partes[2],
                        esquerda = partes[3].toFloatOrNull() ?: return null,
                        topo = partes[4].toFloatOrNull() ?: return null,
                        largura = partes[5].toFloatOrNull() ?: return null,
                        altura = partes[6].toFloatOrNull() ?: return null,
                    )
                }
                "texto" -> {
                    fecharTraco()
                    if (partes.size < 8) return null
                    textos += TextoDoRascunhoDaEdicao(
                        id = partes[1].toIntOrNull() ?: return null,
                        tamanho = partes[2].toFloatOrNull() ?: return null,
                        cor = partes[3].toIntOrNull() ?: return null,
                        esquerda = partes[4].toFloatOrNull() ?: return null,
                        topo = partes[5].toFloatOrNull() ?: return null,
                        largura = partes[6].toFloatOrNull() ?: return null,
                        altura = partes[7].toFloatOrNull() ?: return null,
                        texto = desescapar(partes.drop(8).joinToString(" ")),
                    )
                }
                "traco" -> {
                    fecharTraco()
                    val extras = partes.filter { '=' in it }.associate { it.substringBefore('=') to it.substringAfter('=') }
                    pincelDoTraco = extras["pincel"].orEmpty().filter { it.isLetterOrDigit() || it == '-' || it == '~' }
                    sementeDoTraco = extras["semente"]?.toLongOrNull() ?: 0L
                    val fixas = partes.filterNot { '=' in it }
                    val marcaQuadro = fixas.lastOrNull() == "quadro"
                    var dados = if (marcaQuadro) fixas.dropLast(1) else fixas
                    coordenadasDoQuadro = marcaQuadro
                    pontaDoTraco = when (dados.lastOrNull()) {
                        "marcador" -> PontaDoTracoDaComposicao.MARCADOR
                        "nanquim" -> PontaDoTracoDaComposicao.NANQUIM
                        "lapis" -> PontaDoTracoDaComposicao.LAPIS
                        "seta" -> PontaDoTracoDaComposicao.SETA
                        else -> PontaDoTracoDaComposicao.CANETA
                    }
                    if (pontaDoTraco != PontaDoTracoDaComposicao.CANETA) dados = dados.dropLast(1)
                    if (dados.size >= 4) {
                        idDaPecaDoTraco = dados[1].toIntOrNull() ?: return null
                        corDoTraco = dados[2].toIntOrNull() ?: return null
                        borracha = dados[3] == "1"
                    } else {
                        idDaPecaDoTraco = -1
                        corDoTraco = dados.getOrNull(1)?.toIntOrNull() ?: return null
                        borracha = dados.getOrNull(2) == "1"
                    }
                    tracoAberto = mutableListOf()
                }
                "p" -> {
                    val pontos = tracoAberto ?: return null
                    if (partes.size < 4) return null
                    pontos += PontoDoTracoDoRascunhoDaEdicao(
                        x = partes[1].toFloatOrNull() ?: return null,
                        y = partes[2].toFloatOrNull() ?: return null,
                        largura = partes[3].toFloatOrNull() ?: return null,
                        alpha = partes.getOrNull(4)?.toIntOrNull() ?: 255,
                    )
                }
                else -> return null
            }
        }
        fecharTraco()
        if (largura <= 0 || altura <= 0) return null
        return DocumentoDoRascunhoDaEdicaoDeImagem(
            largura = largura,
            altura = altura,
            proximoId = proximoId,
            ferramenta = ferramenta,
            imagens = imagens,
            textos = textos,
            tracos = tracos,
            corDaFolha = corDaFolha,
            angulos = angulos,
            espelhos = espelhos,
            ordem = ordem,
            camadasVazias = camadasVazias,
            opacidades = opacidades,
            ocultas = ocultas,
            bloqueadas = bloqueadas,
            pinceis = pinceis,
        )
    }

    private fun numero(valor: Float): String = String.format(java.util.Locale.US, "%.4f", valor)

    private fun escapar(texto: String): String {
        return texto.replace("\\", "\\\\").replace("\n", "\\n").replace(" ", "\\s")
    }

    private fun desescapar(texto: String): String {
        val saida = StringBuilder()
        var escapando = false
        for (caractere in texto) {
            if (escapando) {
                saida.append(
                    when (caractere) {
                        'n' -> '\n'
                        's' -> ' '
                        else -> caractere
                    },
                )
                escapando = false
            } else if (caractere == '\\') {
                escapando = true
            } else {
                saida.append(caractere)
            }
        }
        return saida.toString()
    }
}
