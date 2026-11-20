package com.chromebrothers.navegador.regras

import java.io.ByteArrayOutputStream

object TempoDeExibicaoDaAmostraParaOArquivo {
    fun sequencia(temposEmMicrossegundos: List<Long>): List<Long> {
        return temposEmMicrossegundos.map { it.coerceAtLeast(0) }
    }
}

object PreparacaoDaAmostraDeVideoAvcParaOMuxer {
    private val codigoDeInicio = byteArrayOf(0, 0, 0, 1)

    fun tamanhoDoComprimento(configuracao: ByteArray): Int {
        if (configuracao.size > 4 && configuracao[0] == 1.toByte()) {
            return (configuracao[4].toInt() and 0x03) + 1
        }
        return 4
    }

    fun inicializacao(configuracao: ByteArray, segundo: ByteArray?): List<ByteArray>? {
        if (configuracao.isNotEmpty() && configuracao[0] == 1.toByte()) {
            return daConfiguracao(configuracao)
        }
        val unidades = extrair(configuracao) + (segundo?.let { extrair(it) } ?: emptyList())
        if (unidades.size < 2) return null
        return listOf(comInicio(unidades[0]), comInicio(unidades[1]))
    }

    fun amostra(dados: ByteArray, tamanhoDoComprimento: Int): ByteArray {
        if (comecaComCodigoDeInicio(dados) || tamanhoDoComprimento !in 1..4) return dados
        val saida = ByteArrayOutputStream(dados.size + 4)
        var indice = 0
        while (indice + tamanhoDoComprimento <= dados.size) {
            var tamanho = 0
            for (casa in 0 until tamanhoDoComprimento) {
                tamanho = (tamanho shl 8) or (dados[indice + casa].toInt() and 0xFF)
            }
            indice += tamanhoDoComprimento
            if (tamanho <= 0 || indice + tamanho > dados.size) return dados
            saida.write(codigoDeInicio)
            saida.write(dados, indice, tamanho)
            indice += tamanho
        }
        val convertido = saida.toByteArray()
        return if (convertido.isEmpty()) dados else convertido
    }

    private fun daConfiguracao(configuracao: ByteArray): List<ByteArray>? {
        if (configuracao.size < 8) return null
        var indice = 5
        val quantidadeDeSps = configuracao[indice].toInt() and 0x1F
        indice += 1
        if (quantidadeDeSps < 1) return null
        val sps = lerUnidade(configuracao, indice) ?: return null
        indice = sps.second
        if (indice >= configuracao.size) return null
        val quantidadeDePps = configuracao[indice].toInt() and 0xFF
        indice += 1
        if (quantidadeDePps < 1) return null
        val pps = lerUnidade(configuracao, indice) ?: return null
        return listOf(comInicio(sps.first), comInicio(pps.first))
    }

    private fun lerUnidade(dados: ByteArray, inicio: Int): Pair<ByteArray, Int>? {
        if (inicio + 2 > dados.size) return null
        val tamanho = ((dados[inicio].toInt() and 0xFF) shl 8) or (dados[inicio + 1].toInt() and 0xFF)
        val posicao = inicio + 2
        if (tamanho <= 0 || posicao + tamanho > dados.size) return null
        return dados.copyOfRange(posicao, posicao + tamanho) to (posicao + tamanho)
    }

    private fun extrair(dados: ByteArray): List<ByteArray> {
        if (!comecaComCodigoDeInicio(dados)) return emptyList()
        val unidades = mutableListOf<ByteArray>()
        var inicio = 0
        while (inicio < dados.size) {
            val codigo = indiceDoCodigo(dados, inicio) ?: break
            val conteudo = codigo + if (dados[codigo + 2] == 1.toByte()) 3 else 4
            val proximo = indiceDoCodigo(dados, conteudo) ?: dados.size
            if (conteudo < proximo) unidades.add(dados.copyOfRange(conteudo, proximo))
            inicio = proximo
        }
        return unidades
    }

    private fun indiceDoCodigo(dados: ByteArray, desde: Int): Int? {
        var indice = desde
        while (indice + 3 < dados.size) {
            if (dados[indice] == 0.toByte() && dados[indice + 1] == 0.toByte()) {
                if (dados[indice + 2] == 1.toByte()) return indice
                if (indice + 3 < dados.size && dados[indice + 2] == 0.toByte() && dados[indice + 3] == 1.toByte()) {
                    return indice
                }
            }
            indice += 1
        }
        return null
    }

    private fun comecaComCodigoDeInicio(dados: ByteArray): Boolean {
        if (dados.size >= 4 && dados[0] == 0.toByte() && dados[1] == 0.toByte() && dados[2] == 0.toByte() && dados[3] == 1.toByte()) {
            return true
        }
        return dados.size >= 3 && dados[0] == 0.toByte() && dados[1] == 0.toByte() && dados[2] == 1.toByte()
    }

    private fun comInicio(unidade: ByteArray): ByteArray {
        val saida = ByteArray(codigoDeInicio.size + unidade.size)
        codigoDeInicio.copyInto(saida)
        unidade.copyInto(saida, codigoDeInicio.size)
        return saida
    }
}
