package com.chromebrothers.navegador.regras

import java.net.URI

class ListaDeBloqueioDeAnuncio(
    private val bloqueios: Set<String>,
    private val bloqueiosDeTerceiros: Set<String>,
    private val excecoes: Set<String>,
    private val excecoesDeTerceiros: Set<String>,
) {
    fun excecaoCobre(host: String, terceiro: Boolean): Boolean {
        if (cobre(host, excecoes)) return true
        return terceiro && cobre(host, excecoesDeTerceiros)
    }

    fun bloqueioCobre(host: String, terceiro: Boolean): Boolean {
        if (cobre(host, bloqueios)) return true
        return terceiro && cobre(host, bloqueiosDeTerceiros)
    }

    private fun cobre(host: String, dominios: Set<String>): Boolean {
        var atual = host
        while (atual.isNotEmpty()) {
            if (dominios.contains(atual)) return true
            val ponto = atual.indexOf('.')
            if (ponto <= 0 || ponto == atual.lastIndex) break
            atual = atual.substring(ponto + 1)
        }
        return false
    }

    fun dominiosDeBloqueio(): List<String> = bloqueios.sorted()

    fun dominiosDeBloqueioDeTerceiros(): List<String> = bloqueiosDeTerceiros.sorted()

    fun dominiosDeExcecao(): List<String> = excecoes.sorted()

    fun dominiosDeExcecaoDeTerceiros(): List<String> = excecoesDeTerceiros.sorted()
}

object TextoDaListaDeBloqueioDeAnuncio {
    fun ler(texto: String): ListaDeBloqueioDeAnuncio {
        val bloqueios = HashSet<String>()
        val bloqueiosDeTerceiros = HashSet<String>()
        val excecoes = HashSet<String>()
        val excecoesDeTerceiros = HashSet<String>()
        texto.lineSequence().forEach { bruta ->
            val linha = bruta.trim()
            if (linha.isEmpty() || linha.startsWith("!") || linha.startsWith("[")) return@forEach
            if (linha.contains("##") || linha.contains("#@#") || linha.contains("#?#")) return@forEach
            val excecao = linha.startsWith("@@")
            val corpo = if (excecao) linha.removePrefix("@@") else linha
            if (!corpo.startsWith("||")) return@forEach
            val resto = corpo.removePrefix("||")
            val marca = resto.indexOf('^')
            if (marca <= 0) return@forEach
            val dominio = resto.substring(0, marca).lowercase()
            if (!dominioValido(dominio)) return@forEach
            val depois = resto.substring(marca + 1)
            val soTerceiro = when {
                depois.isEmpty() -> false
                depois == "\$third-party" -> true
                else -> return@forEach
            }
            val destino = when {
                excecao && soTerceiro -> excecoesDeTerceiros
                excecao -> excecoes
                soTerceiro -> bloqueiosDeTerceiros
                else -> bloqueios
            }
            destino.add(dominio)
        }
        return ListaDeBloqueioDeAnuncio(bloqueios, bloqueiosDeTerceiros, excecoes, excecoesDeTerceiros)
    }

    private fun dominioValido(dominio: String): Boolean {
        return dominio.contains('.') &&
            !dominio.contains('/') &&
            !dominio.contains('*') &&
            !dominio.contains(' ') &&
            dominio.none { it.isUpperCase() }
    }
}

object DecisaoDeBloqueioDeAnuncio {
    fun deveBloquear(
        habilitado: Boolean,
        ehQuadroPrincipal: Boolean,
        urlDoPedido: String,
        urlDaPagina: String,
        lista: ListaDeBloqueioDeAnuncio,
    ): Boolean {
        if (!habilitado || ehQuadroPrincipal) return false
        if (!urlDoPedido.startsWith("http://") && !urlDoPedido.startsWith("https://")) return false
        val host = hostDe(urlDoPedido)
        if (host.isEmpty()) return false
        val terceiro = !mesmoSite(host, hostDe(urlDaPagina))
        if (lista.excecaoCobre(host, terceiro)) return false
        return lista.bloqueioCobre(host, terceiro)
    }

    fun urlsDoMesmoSite(urlA: String, urlB: String): Boolean {
        return mesmoSite(hostDe(urlA), hostDe(urlB))
    }

    fun baseRegistravel(host: String): String {
        if (host.isEmpty() || host.contains(':')) return host
        if (host.all { it.isDigit() || it == '.' }) return host
        val partes = host.split('.').filter { it.isNotEmpty() }
        if (partes.size <= 2) return partes.joinToString(".")
        return partes.takeLast(2).joinToString(".")
    }

    private fun mesmoSite(hostPedido: String, hostPagina: String): Boolean {
        val pedido = baseRegistravel(hostPedido)
        val pagina = baseRegistravel(hostPagina)
        return pedido.isNotEmpty() && pedido == pagina
    }

    private fun hostDe(url: String): String {
        return try {
            URI(url).host?.lowercase()?.trim('.') ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}
