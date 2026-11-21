package com.chromebrothers.navegador.regras

import kotlin.math.ln

/**
 * Uma URL no histórico, no estilo do Chrome: quantas vezes foi visitada, quantas vezes
 * foi digitada/escolhida na barra e quando foi a última visita.
 */
data class EntradaDoHistoricoDoNavegador(
    val endereco: String,
    val titulo: String,
    val visitas: Int,
    val digitadas: Int,
    val ultimaVisitaMs: Long,
) {
    fun comoVisita(): VisitaDePaginaNoNavegador = VisitaDePaginaNoNavegador(titulo, endereco)
}

object RegrasDoHistoricoCompletoDoNavegador {
    /** O Chrome guarda 90 dias de histórico. */
    const val DIAS_DE_RETENCAO = 90
    const val RETENCAO_MS = DIAS_DE_RETENCAO * 24L * 60L * 60L * 1000L
    const val LIMITE_DE_ENTRADAS = 20_000
    private const val TAMANHO_MAXIMO_DA_URL = 2_048

    fun deveGuardar(endereco: String): Boolean {
        val url = endereco.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) return false
        return url.length <= TAMANHO_MAXIMO_DA_URL
    }

    fun registrarVisita(
        existente: EntradaDoHistoricoDoNavegador?,
        endereco: String,
        titulo: String,
        digitada: Boolean,
        agoraMs: Long,
    ): EntradaDoHistoricoDoNavegador {
        val url = endereco.trim()
        val nome = titulo.trim().ifBlank { existente?.titulo?.takeIf { it.isNotBlank() } ?: url }
        return EntradaDoHistoricoDoNavegador(
            endereco = url,
            titulo = nome,
            visitas = (existente?.visitas ?: 0) + 1,
            digitadas = (existente?.digitadas ?: 0) + if (digitada) 1 else 0,
            ultimaVisitaMs = agoraMs,
        )
    }

    fun expirada(entrada: EntradaDoHistoricoDoNavegador, agoraMs: Long): Boolean =
        agoraMs - entrada.ultimaVisitaMs > RETENCAO_MS

    /** Mantém só o que está dentro da retenção e no limite, mais recentes primeiro. */
    fun podar(
        entradas: Collection<EntradaDoHistoricoDoNavegador>,
        agoraMs: Long,
        limite: Int = LIMITE_DE_ENTRADAS,
    ): List<EntradaDoHistoricoDoNavegador> = entradas
        .filterNot { expirada(it, agoraMs) }
        .sortedByDescending { it.ultimaVisitaMs }
        .take(limite)

    /** O histórico antigo só tinha a ordem: o primeiro vira "agora", cada seguinte 1 min antes. */
    fun migrarDoHistoricoAntigo(
        visitas: List<VisitaDePaginaNoNavegador>,
        agoraMs: Long,
    ): List<EntradaDoHistoricoDoNavegador> = visitas
        .filter { deveGuardar(it.endereco) }
        .distinctBy { it.endereco }
        .mapIndexed { indice, visita ->
            EntradaDoHistoricoDoNavegador(
                endereco = visita.endereco.trim(),
                titulo = visita.titulo,
                visitas = 1,
                digitadas = 0,
                ultimaVisitaMs = agoraMs - indice * 60_000L,
            )
        }
}

/**
 * Ranqueamento das páginas do histórico para a barra de endereço, inspirado no
 * HistoryQuickProvider do Chrome: o texto precisa casar (todos os termos, de preferência
 * no começo de palavras) e o peso vem de frequência + digitação + recência.
 */
object RanqueamentoDoHistoricoNaBarraDeEndereco {
    private const val DIA_MS = 24L * 60L * 60L * 1000L
    private val separadores = Regex("[^\\p{L}\\p{N}]+")

    fun frecencia(entrada: EntradaDoHistoricoDoNavegador, agoraMs: Long): Double {
        val dias = (agoraMs - entrada.ultimaVisitaMs).coerceAtLeast(0L).toDouble() / DIA_MS
        val recencia = when {
            dias < 1 -> 1.0
            dias < 4 -> 0.85
            dias < 14 -> 0.65
            dias < 31 -> 0.45
            else -> 0.3
        }
        val uso = 1.0 + ln(1.0 + entrada.visitas) + 2.0 * ln(1.0 + entrada.digitadas)
        return uso * recencia
    }

    /** Qualidade do casamento do texto com a página (0 = não casa). Todos os termos precisam casar. */
    fun casamento(consulta: String, entrada: EntradaDoHistoricoDoNavegador): Int {
        val termos = consulta.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (termos.isEmpty()) return 0
        val host = IconeDaPaginaNaBarraDeEndereco.hospedeiro(entrada.endereco)
        val semEsquema = enderecoSemEsquema(entrada.endereco)
        val titulo = entrada.titulo.lowercase()
        var total = 0
        for (termo in termos) {
            val curto = termo.length < 3
            val pontos = when {
                termos.size == 1 && semEsquema.startsWith(termo) -> 400
                host.startsWith(termo) -> 380
                comecaPalavra(host, termo) -> 300
                comecaPalavra(titulo, termo) -> 220
                comecaPalavra(semEsquema, termo) -> 160
                !curto && titulo.contains(termo) -> 90
                !curto && semEsquema.contains(termo) -> 60
                else -> return 0
            }
            total += pontos
        }
        return total / termos.size
    }

    fun pontuar(consulta: String, entrada: EntradaDoHistoricoDoNavegador, agoraMs: Long): Double {
        val base = casamento(consulta, entrada)
        if (base <= 0) return 0.0
        return base * frecencia(entrada, agoraMs)
    }

    fun melhores(
        consulta: String,
        entradas: List<EntradaDoHistoricoDoNavegador>,
        agoraMs: Long,
        limite: Int,
    ): List<EntradaDoHistoricoDoNavegador> {
        if (consulta.isBlank() || limite <= 0) return emptyList()
        return entradas
            .mapNotNull { entrada ->
                val pontos = pontuar(consulta, entrada, agoraMs)
                if (pontos <= 0.0) null else entrada to pontos
            }
            .sortedWith(
                compareByDescending<Pair<EntradaDoHistoricoDoNavegador, Double>> { it.second }
                    .thenByDescending { it.first.ultimaVisitaMs },
            )
            .take(limite)
            .map { it.first }
    }

    /**
     * Autocompletar dentro da barra (o "youtube.com" que aparece selecionado quando se digita
     * "you"). Devolve o texto completo (o digitado + o resto) ou null.
     * Só completa sites que a pessoa já digitou ou visitou mais de uma vez, como o Chrome.
     */
    fun autocompletarEmLinha(
        digitado: String,
        entradas: List<EntradaDoHistoricoDoNavegador>,
        agoraMs: Long,
    ): String? {
        if (digitado.isEmpty() || digitado.any { it.isWhitespace() }) return null
        val consulta = digitado.lowercase()
        if (consulta.startsWith("http:") || consulta.startsWith("https:")) return null
        val semWww = consulta.removePrefix("www.")
        if (semWww.isEmpty()) return null
        val prefixoWww = consulta.length - semWww.length

        // 1) Completar o site (host), somando o uso de todas as páginas dele.
        if (!semWww.contains('/')) {
            data class Uso(var visitas: Int = 0, var digitadas: Int = 0, var peso: Double = 0.0)
            val porHost = linkedMapOf<String, Uso>()
            entradas.forEach { entrada ->
                val host = IconeDaPaginaNaBarraDeEndereco.hospedeiro(entrada.endereco)
                if (host.length <= semWww.length || !host.startsWith(semWww)) return@forEach
                val uso = porHost.getOrPut(host) { Uso() }
                uso.visitas += entrada.visitas
                uso.digitadas += entrada.digitadas
                uso.peso += frecencia(entrada, agoraMs)
            }
            val escolhido = porHost.entries
                .filter { it.value.digitadas > 0 || it.value.visitas >= 2 }
                .maxWithOrNull(compareBy<Map.Entry<String, Uso>> { it.value.peso }.thenBy { -it.key.length })
            if (escolhido != null) return digitado + escolhido.key.substring(semWww.length)
        }

        // 2) Já digitando o caminho (ex.: "github.com/fo"): completa a página inteira.
        // Só com a barra digitada: o domínio inteiro ("higgsfield.ai") abre o site, não a página
        // mais visitada dele (higgsfield.ai/supercomputer), como no Chrome.
        if (!semWww.contains('/')) return null
        val pagina = entradas
            .filter { it.digitadas > 0 || it.visitas >= 2 }
            .mapNotNull { entrada ->
                val alvo = enderecoSemEsquema(entrada.endereco).removeSuffix("/")
                if (alvo.length > semWww.length && alvo.startsWith(semWww)) entrada to alvo else null
            }
            .maxByOrNull { frecencia(it.first, agoraMs) }
            ?: return null
        val resto = pagina.second.substring(consulta.length - prefixoWww)
        return digitado + resto
    }

    /** "https://www.Exemplo.com/a" → "exemplo.com/a". */
    fun enderecoSemEsquema(endereco: String): String {
        val baixo = endereco.trim().lowercase()
        val semEsquema = baixo.substringAfter("://", baixo)
        return semEsquema.removePrefix("www.")
    }

    private fun comecaPalavra(texto: String, termo: String): Boolean {
        if (texto.startsWith(termo)) return true
        var indice = texto.indexOf(termo)
        while (indice > 0) {
            if (separadores.matches(texto[indice - 1].toString())) return true
            indice = texto.indexOf(termo, indice + 1)
        }
        return false
    }
}
