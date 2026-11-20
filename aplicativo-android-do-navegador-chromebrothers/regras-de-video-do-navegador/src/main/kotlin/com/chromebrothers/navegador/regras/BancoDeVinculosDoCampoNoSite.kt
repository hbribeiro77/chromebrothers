package com.chromebrothers.navegador.regras

data class MarcaDoElementoVinculado(
    val id: String,
    val nome: String,
    val rotulo: String,
    val tag: String,
    val papel: String,
)

data class VinculoDoCampoNoSite(
    val hospedeiro: String,
    val campo: MarcaDoElementoVinculado?,
    val envio: MarcaDoElementoVinculado?,
    val endereco: String = "",
)

object BancoDeVinculosDoCampoNoSite {
    fun hospedeiro(endereco: String): String {
        val semEsquema = endereco.substringAfter("://", endereco)
        return semEsquema.substringBefore("/").substringBefore("?").substringBefore("#").lowercase()
    }

    fun doSite(vinculos: List<VinculoDoCampoNoSite>, hospedeiro: String): VinculoDoCampoNoSite? {
        return vinculos.find { it.hospedeiro == hospedeiro }
    }

    fun guardarCampo(
        vinculos: List<VinculoDoCampoNoSite>,
        hospedeiro: String,
        campo: MarcaDoElementoVinculado,
    ): List<VinculoDoCampoNoSite> = colocar(vinculos, hospedeiro, campo = campo, envio = null, trocaEnvio = false)

    fun guardarEnvio(
        vinculos: List<VinculoDoCampoNoSite>,
        hospedeiro: String,
        envio: MarcaDoElementoVinculado,
    ): List<VinculoDoCampoNoSite> = colocar(vinculos, hospedeiro, campo = null, envio = envio, trocaEnvio = true)

    fun removerCampo(vinculos: List<VinculoDoCampoNoSite>, hospedeiro: String): List<VinculoDoCampoNoSite> {
        return vinculos.mapNotNull { vinculo ->
            if (vinculo.hospedeiro != hospedeiro) return@mapNotNull vinculo
            if (vinculo.envio == null) null else vinculo.copy(campo = null)
        }
    }

    fun removerEnvio(vinculos: List<VinculoDoCampoNoSite>, hospedeiro: String): List<VinculoDoCampoNoSite> {
        return vinculos.mapNotNull { vinculo ->
            if (vinculo.hospedeiro != hospedeiro) return@mapNotNull vinculo
            if (vinculo.campo == null) null else vinculo.copy(envio = null)
        }
    }

    fun guardarEndereco(
        vinculos: List<VinculoDoCampoNoSite>,
        hospedeiro: String,
        endereco: String,
    ): List<VinculoDoCampoNoSite> {
        if (hospedeiro.isBlank() || endereco.isBlank()) return vinculos
        val indice = vinculos.indexOfFirst { it.hospedeiro == hospedeiro }
        if (indice < 0 || vinculos[indice].endereco == endereco) return vinculos
        return vinculos.mapIndexed { posicao, vinculo ->
            if (posicao == indice) vinculo.copy(endereco = endereco) else vinculo
        }
    }

    fun enderecoParaAbrir(vinculo: VinculoDoCampoNoSite): String {
        val salvo = vinculo.endereco.trim()
        if (salvo.startsWith("https://") || salvo.startsWith("http://")) return salvo
        return "https://${vinculo.hospedeiro}/"
    }

    fun gravar(vinculos: List<VinculoDoCampoNoSite>): String {
        return vinculos.joinToString("\n") { vinculo ->
            listOf(vinculo.hospedeiro).plus(campos(vinculo.campo)).plus(campos(vinculo.envio)).plus(vinculo.endereco)
                .joinToString("\t") { escapar(it) }
        }
    }

    fun ler(bruto: String): List<VinculoDoCampoNoSite> {
        if (bruto.isBlank()) return emptyList()
        return bruto.lineSequence().mapNotNull { linha ->
            val partes = linha.split('\t').map { desescapar(it) }
            if (partes.size < 11 || partes[0].isBlank()) return@mapNotNull null
            VinculoDoCampoNoSite(
                partes[0],
                marca(partes.subList(1, 6)),
                marca(partes.subList(6, 11)),
                endereco = partes.getOrNull(11).orEmpty(),
            )
        }.toList()
    }

    private fun colocar(
        vinculos: List<VinculoDoCampoNoSite>,
        hospedeiro: String,
        campo: MarcaDoElementoVinculado?,
        envio: MarcaDoElementoVinculado?,
        trocaEnvio: Boolean,
    ): List<VinculoDoCampoNoSite> {
        if (hospedeiro.isBlank()) return vinculos
        val atual = vinculos.find { it.hospedeiro == hospedeiro }
        val novo = VinculoDoCampoNoSite(
            hospedeiro,
            campo = if (trocaEnvio) atual?.campo else campo,
            envio = if (trocaEnvio) envio else atual?.envio,
            endereco = atual?.endereco.orEmpty(),
        )
        if (novo.campo == null && novo.envio == null) return vinculos.filter { it.hospedeiro != hospedeiro }
        return listOf(novo) + vinculos.filter { it.hospedeiro != hospedeiro }
    }

    private fun campos(marca: MarcaDoElementoVinculado?): List<String> {
        return listOf(marca?.id.orEmpty(), marca?.nome.orEmpty(), marca?.rotulo.orEmpty(), marca?.tag.orEmpty(), marca?.papel.orEmpty())
    }

    private fun marca(partes: List<String>): MarcaDoElementoVinculado? {
        if (partes.all { it.isEmpty() }) return null
        return MarcaDoElementoVinculado(partes[0], partes[1], partes[2], partes[3], partes[4])
    }

    private fun escapar(texto: String) = buildString {
        texto.forEach { caractere ->
            when (caractere) {
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(caractere)
            }
        }
    }

    private fun desescapar(texto: String) = buildString {
        var indice = 0
        while (indice < texto.length) {
            val caractere = texto[indice]
            if (caractere == '\\' && indice + 1 < texto.length) {
                when (texto[indice + 1]) {
                    'n' -> append('\n')
                    'r' -> append('\r')
                    't' -> append('\t')
                    '\\' -> append('\\')
                    else -> append(texto[indice + 1])
                }
                indice += 2
            } else {
                append(caractere)
                indice += 1
            }
        }
    }
}
