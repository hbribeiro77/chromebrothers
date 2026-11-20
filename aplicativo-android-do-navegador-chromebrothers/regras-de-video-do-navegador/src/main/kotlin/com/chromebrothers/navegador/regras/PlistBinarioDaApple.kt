package com.chromebrothers.navegador.regras

/**
 * Leitor de "binary plist" (`bplist00`), o formato de configuração da Apple usado no `Brush.archive`
 * do Procreate. Devolve: `Map<String, Any?>`, `List<Any?>`, `String`, `Long`, `Double`, `Boolean`,
 * `ByteArray`, [Uid] ou null. Arquivo malformado → null (nunca lança).
 */
object PlistBinarioDaApple {
    /** Referência do NSKeyedArchiver (índice em `$objects`). */
    data class Uid(val indice: Int)

    private const val LIMITE_DE_OBJETOS = 200_000
    private const val PROFUNDIDADE_MAXIMA = 64

    fun ler(bytes: ByteArray): Any? = runCatching { Leitor(bytes).raiz() }.getOrNull()

    fun ehPlistBinario(bytes: ByteArray): Boolean =
        bytes.size >= 40 && String(bytes, 0, 8, Charsets.US_ASCII) == "bplist00"

    private class Leitor(private val b: ByteArray) {
        private val tamanhoDoOffset: Int
        private val tamanhoDaRef: Int
        private val quantidade: Int
        private val topo: Int
        private val tabela: Int

        init {
            require(ehPlistBinario(b))
            val t = b.size - 32
            tamanhoDoOffset = b[t + 6].toInt() and 0xFF
            tamanhoDaRef = b[t + 7].toInt() and 0xFF
            quantidade = inteiro(t + 8, 8).toInt()
            topo = inteiro(t + 16, 8).toInt()
            tabela = inteiro(t + 24, 8).toInt()
            require(tamanhoDoOffset in 1..8 && tamanhoDaRef in 1..8)
            require(quantidade in 1..LIMITE_DE_OBJETOS && topo in 0 until quantidade)
            require(tabela >= 8 && tabela.toLong() + quantidade.toLong() * tamanhoDoOffset <= t)
        }

        fun raiz(): Any? = objeto(topo, 0)

        private fun inteiro(pos: Int, n: Int): Long {
            require(pos >= 0 && pos + n <= b.size)
            var v = 0L
            for (i in 0 until n) v = (v shl 8) or (b[pos + i].toLong() and 0xFF)
            return v
        }

        private fun offset(ref: Int): Int {
            require(ref in 0 until quantidade)
            return inteiro(tabela + ref * tamanhoDoOffset, tamanhoDoOffset).toInt()
        }

        /** Contagem do objeto: nibble baixo, ou 0xF + inteiro em seguida. Devolve (contagem, início dos dados). */
        private fun contagem(pos: Int, nibble: Int): Pair<Int, Int> {
            if (nibble != 0xF) return nibble to pos + 1
            val marca = b[pos + 1].toInt() and 0xFF
            require(marca shr 4 == 0x1)
            val n = 1 shl (marca and 0xF)
            val valor = inteiro(pos + 2, n)
            require(valor in 0..Int.MAX_VALUE.toLong())
            return valor.toInt() to pos + 2 + n
        }

        private fun objeto(ref: Int, profundidade: Int): Any? {
            require(profundidade < PROFUNDIDADE_MAXIMA)
            val pos = offset(ref)
            require(pos in 8 until b.size)
            val marca = b[pos].toInt() and 0xFF
            val tipo = marca shr 4
            val nibble = marca and 0xF
            return when (tipo) {
                0x0 -> when (marca) {
                    0x08 -> false
                    0x09 -> true
                    else -> null
                }
                0x1 -> {
                    val n = 1 shl nibble
                    require(n <= 8)
                    val v = inteiro(pos + 1, n)
                    // 1, 2 e 4 bytes são sem sinal; 8 bytes é com sinal (Long já é).
                    v
                }
                0x2 -> when (1 shl nibble) {
                    4 -> java.lang.Float.intBitsToFloat(inteiro(pos + 1, 4).toInt()).toDouble()
                    8 -> java.lang.Double.longBitsToDouble(inteiro(pos + 1, 8))
                    else -> error("real")
                }
                0x3 -> java.lang.Double.longBitsToDouble(inteiro(pos + 1, 8))
                0x4 -> {
                    val (n, ini) = contagem(pos, nibble)
                    require(ini + n <= b.size)
                    b.copyOfRange(ini, ini + n)
                }
                0x5 -> {
                    val (n, ini) = contagem(pos, nibble)
                    require(ini + n <= b.size)
                    String(b, ini, n, Charsets.US_ASCII)
                }
                0x6 -> {
                    val (n, ini) = contagem(pos, nibble)
                    require(ini + n * 2 <= b.size)
                    String(b, ini, n * 2, Charsets.UTF_16BE)
                }
                0x8 -> Uid(inteiro(pos + 1, nibble + 1).toInt())
                0xA, 0xC -> {
                    val (n, ini) = contagem(pos, nibble)
                    require(n <= quantidade)
                    List(n) { i -> objeto(inteiro(ini + i * tamanhoDaRef, tamanhoDaRef).toInt(), profundidade + 1) }
                }
                0xD -> {
                    val (n, ini) = contagem(pos, nibble)
                    require(n <= quantidade)
                    val mapa = LinkedHashMap<String, Any?>(n * 2)
                    for (i in 0 until n) {
                        val chave = objeto(inteiro(ini + i * tamanhoDaRef, tamanhoDaRef).toInt(), profundidade + 1)
                        val valor = objeto(inteiro(ini + (n + i) * tamanhoDaRef, tamanhoDaRef).toInt(), profundidade + 1)
                        mapa[chave.toString()] = valor
                    }
                    mapa
                }
                else -> null
            }
        }
    }

    /**
     * Desfaz o NSKeyedArchiver: segue `$top.root` e troca cada [Uid] pelo objeto de `$objects`
     * (`$null` vira null; NSDictionary/NSArray/NSString viram mapa/lista/texto). Sem `$objects`, null.
     */
    fun raizDoArquivoChaveado(plist: Any?): Map<String, Any?>? {
        val mapa = plist as? Map<*, *> ?: return null
        val objetos = mapa["\$objects"] as? List<*> ?: return null
        val topo = mapa["\$top"] as? Map<*, *> ?: return null
        val raiz = topo["root"] as? Uid ?: topo.values.firstOrNull { it is Uid } as? Uid ?: return null
        @Suppress("UNCHECKED_CAST")
        return resolver(raiz, objetos, 0) as? Map<String, Any?>
    }

    private fun resolver(valor: Any?, objetos: List<*>, profundidade: Int): Any? {
        if (profundidade > 12) return null
        return when (valor) {
            is Uid -> {
                val alvo = objetos.getOrNull(valor.indice)
                if (alvo == "\$null") null else resolver(alvo, objetos, profundidade + 1)
            }
            is Map<*, *> -> {
                val chaves = valor["NS.keys"] as? List<*>
                val valores = valor["NS.objects"] as? List<*>
                when {
                    chaves != null && valores != null -> {
                        val saida = LinkedHashMap<String, Any?>()
                        chaves.zip(valores).forEach { (k, v) ->
                            saida[resolver(k, objetos, profundidade + 1).toString()] = resolver(v, objetos, profundidade + 1)
                        }
                        saida
                    }
                    valores != null -> valores.map { resolver(it, objetos, profundidade + 1) }
                    valor["NS.string"] != null -> resolver(valor["NS.string"], objetos, profundidade + 1)
                    valor["NS.bytes"] != null -> resolver(valor["NS.bytes"], objetos, profundidade + 1)
                    else -> {
                        val saida = LinkedHashMap<String, Any?>()
                        valor.forEach { (k, v) ->
                            val chave = k.toString()
                            if (chave == "\$class") return@forEach
                            saida[chave] = resolver(v, objetos, profundidade + 1)
                        }
                        saida
                    }
                }
            }
            is List<*> -> valor.map { resolver(it, objetos, profundidade + 1) }
            else -> valor
        }
    }
}
