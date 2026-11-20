package com.chromebrothers.navegador.regras

data class MarcaDoElementoParaORoteiroDeAutomacao(
    val id: String = "",
    val name: String = "",
    val ariaLabel: String = "",
    val placeholder: String = "",
    val texto: String = "",
    val tag: String = "",
    val indiceNaTag: Int = 0,
    val type: String = "",
    val role: String = "",
    val href: String = "",
    val testId: String = "",
    val dataQa: String = "",
    val classes: String = "",
    val formId: String = "",
    val inNav: Boolean = false,
    val inHeader: Boolean = false,
    val labelTexto: String = "",
    val pai: MarcaDoElementoParaORoteiroDeAutomacao? = null,
)

data class CandidataDoElementoParaORoteiro(
    val rotulo: String,
    val seletor: String,
    val texto: String,
    val matches: Int? = null,
) {
    fun comMatches(n: Int): CandidataDoElementoParaORoteiro {
        val sufixo = when {
            n <= 0 -> " · 0 na página"
            n == 1 -> " · única"
            else -> " · $n na página"
        }
        val base = rotulo.substringBefore(" · ")
        return copy(rotulo = base + sufixo, matches = n)
    }
}

object CandidatasDoElementoParaORoteiroDeAutomacao {
    const val LIMITE_DE_CANDIDATAS = 12
    private const val LIMITE_TEXTO = 80

    fun lerMarca(json: String): MarcaDoElementoParaORoteiroDeAutomacao? {
        if (json.isBlank()) return null
        val tag = TextoJsonDoChat.valorDaChave(json, "tag")?.trim().orEmpty()
        val texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty()
        val id = TextoJsonDoChat.valorDaChave(json, "id")?.trim().orEmpty()
        if (tag.isEmpty() && id.isEmpty() && texto.isEmpty()) return null
        val paiJson = TextoJsonDoChat.objetoDaChave(json, "pai")
        val pai = paiJson?.let { lerMarcaPlana(it) }
        return lerMarcaPlana(json).copy(pai = pai)
    }

    private fun lerMarcaPlana(json: String): MarcaDoElementoParaORoteiroDeAutomacao =
        MarcaDoElementoParaORoteiroDeAutomacao(
            id = TextoJsonDoChat.valorDaChave(json, "id")?.trim().orEmpty(),
            name = TextoJsonDoChat.valorDaChave(json, "name")?.trim().orEmpty(),
            ariaLabel = TextoJsonDoChat.valorDaChave(json, "aria")?.trim().orEmpty()
                .ifBlank { TextoJsonDoChat.valorDaChave(json, "ariaLabel")?.trim().orEmpty() },
            placeholder = TextoJsonDoChat.valorDaChave(json, "placeholder")?.trim().orEmpty(),
            texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty().take(LIMITE_TEXTO),
            tag = TextoJsonDoChat.valorDaChave(json, "tag")?.trim()?.lowercase().orEmpty(),
            indiceNaTag = (TextoJsonDoChat.inteiroDaChave(json, "indice") ?: 0).coerceAtLeast(0),
            type = TextoJsonDoChat.valorDaChave(json, "type")?.trim()?.lowercase().orEmpty(),
            role = TextoJsonDoChat.valorDaChave(json, "role")?.trim()?.lowercase().orEmpty(),
            href = TextoJsonDoChat.valorDaChave(json, "href")?.trim().orEmpty(),
            testId = TextoJsonDoChat.valorDaChave(json, "testid")?.trim().orEmpty()
                .ifBlank { TextoJsonDoChat.valorDaChave(json, "testId")?.trim().orEmpty() },
            dataQa = TextoJsonDoChat.valorDaChave(json, "dataqa")?.trim().orEmpty()
                .ifBlank { TextoJsonDoChat.valorDaChave(json, "dataQa")?.trim().orEmpty() },
            classes = TextoJsonDoChat.valorDaChave(json, "classes")?.trim().orEmpty(),
            formId = TextoJsonDoChat.valorDaChave(json, "formId")?.trim().orEmpty(),
            inNav = TextoJsonDoChat.valorDaChave(json, "inNav")?.equals("true", true) == true ||
                TextoJsonDoChat.inteiroDaChave(json, "inNav") == 1,
            inHeader = TextoJsonDoChat.valorDaChave(json, "inHeader")?.equals("true", true) == true ||
                TextoJsonDoChat.inteiroDaChave(json, "inHeader") == 1,
            labelTexto = TextoJsonDoChat.valorDaChave(json, "label")?.trim().orEmpty().take(LIMITE_TEXTO),
            pai = null,
        )

    fun ranquear(marca: MarcaDoElementoParaORoteiroDeAutomacao): List<CandidataDoElementoParaORoteiro> {
        val saida = mutableListOf<CandidataDoElementoParaORoteiro>()
        fun add(rotulo: String, seletor: String = "", texto: String = "") {
            if (saida.size >= LIMITE_DE_CANDIDATAS) return
            if (seletor.isBlank() && texto.isBlank()) return
            if (saida.any { it.seletor == seletor && it.texto == texto }) return
            saida += CandidataDoElementoParaORoteiro(rotulo, seletor, texto)
        }

        fun addDaMarca(m: MarcaDoElementoParaORoteiroDeAutomacao, prefixo: String) {
            if (m.testId.isNotEmpty()) {
                add(
                    "${prefixo}data-testid",
                    seletor = """[data-testid="${escaparAttr(m.testId)}"]""",
                )
            }
            if (m.dataQa.isNotEmpty()) {
                add(
                    "${prefixo}data-qa",
                    seletor = """[data-qa="${escaparAttr(m.dataQa)}"]""",
                )
            }
            val id = m.id
            if (id.isNotEmpty() && idServe(id)) {
                add("${prefixo}id #$id", seletor = "#$id")
                if (m.formId.isNotEmpty() && idServe(m.formId)) {
                    add(
                        "${prefixo}form #${m.formId} #$id",
                        seletor = """form#${m.formId} #$id""",
                    )
                }
            }
            if (m.name.isNotEmpty()) {
                val nameSel = """[name="${escaparAttr(m.name)}"]"""
                add("${prefixo}name", seletor = nameSel)
                if (m.formId.isNotEmpty() && idServe(m.formId)) {
                    add(
                        "${prefixo}form #${m.formId} name",
                        seletor = """form#${m.formId} $nameSel""",
                    )
                }
            }
            if (m.type.isNotEmpty() && (m.tag == "input" || m.tag == "button")) {
                add(
                    "${prefixo}${m.tag}[type=${m.type}]",
                    seletor = """${m.tag}[type="${escaparAttr(m.type)}"]""",
                )
                if (m.formId.isNotEmpty() && idServe(m.formId) && m.type == "submit") {
                    add(
                        "${prefixo}form submit",
                        seletor = """form#${m.formId} ${m.tag}[type="submit"]""",
                    )
                }
            }
            if (m.ariaLabel.isNotEmpty()) {
                add(
                    "${prefixo}aria-label",
                    seletor = """[aria-label="${escaparAttr(m.ariaLabel)}"]""",
                )
            }
            if (m.placeholder.isNotEmpty()) {
                add(
                    "${prefixo}placeholder",
                    seletor = """[placeholder="${escaparAttr(m.placeholder)}"]""",
                )
            }
            if (m.role.isNotEmpty()) {
                add(
                    "${prefixo}role=${m.role}",
                    seletor = """[role="${escaparAttr(m.role)}"]""",
                )
            }
            if (m.href.isNotEmpty()) {
                val trecho = trechoHref(m.href)
                if (trecho.isNotEmpty()) {
                    add(
                        "${prefixo}href…",
                        seletor = """a[href*="${escaparAttr(trecho)}"]""",
                    )
                }
            }
            if (m.inNav && m.tag == "a" && m.texto.isNotEmpty()) {
                add("${prefixo}nav + texto", seletor = "nav a", texto = m.texto.take(LIMITE_TEXTO))
            }
            if (m.inHeader && (m.tag == "a" || m.tag == "button") && m.texto.isNotEmpty()) {
                add(
                    "${prefixo}header + texto",
                    seletor = "header ${m.tag}",
                    texto = m.texto.take(LIMITE_TEXTO),
                )
            }
            val classe = classeEstavel(m.classes)
            if (classe != null && m.tag.isNotEmpty()) {
                add(
                    "${prefixo}.${classe}",
                    seletor = "${m.tag}.${escaparAttr(classe)}",
                )
            }
            if (m.texto.isNotEmpty()) {
                add(
                    "${prefixo}texto «${m.texto.take(36)}»",
                    texto = m.texto.take(LIMITE_TEXTO),
                )
            }
            if (m.labelTexto.isNotEmpty() && m.texto.isEmpty()) {
                add(
                    "${prefixo}label «${m.labelTexto.take(36)}»",
                    texto = m.labelTexto.take(LIMITE_TEXTO),
                )
            }
            if (m.tag.isNotEmpty()) {
                val nth = m.indiceNaTag + 1
                add(
                    "${prefixo}${m.tag}:nth-of-type($nth)",
                    seletor = "${m.tag}:nth-of-type($nth)",
                )
            }
        }

        addDaMarca(marca, "")
        val pai = marca.pai
        if (pai != null) {
            addDaMarca(pai, "pai · ")
        }
        return saida
    }

    private fun idServe(id: String): Boolean =
        id.isNotEmpty() && id.all { it.isLetterOrDigit() || it == '-' || it == '_' || it == ':' }

    private fun trechoHref(href: String): String {
        val limpo = href.substringBefore('#').substringBefore('?').trim()
        if (limpo.isEmpty() || limpo == "/" || limpo == "#") return ""
        val ultimo = limpo.trimEnd('/').substringAfterLast('/').ifBlank { limpo.takeLast(40) }
        return ultimo.take(60)
    }

    private fun classeEstavel(classes: String): String? {
        if (classes.isBlank()) return null
        val tokens = classes.split(Regex("\\s+")).map { it.trim() }.filter { it.isNotEmpty() }
        val preferidasEditor = setOf("ProseMirror", "ql-editor", "CodeMirror", "cm-content")
        tokens.firstOrNull { it in preferidasEditor }?.let { return it }
        val lixo = setOf(
            "active", "selected", "open", "show", "hide", "hidden", "disabled",
            "flex", "grid", "row", "col", "container", "wrapper", "btn", "button",
            "link", "text", "icon", "svg", "fa", "material-icons",
        )
        return tokens.firstOrNull { c ->
            c.length in 3..40 &&
                c[0].isLetter() &&
                c.all { it.isLetterOrDigit() || it == '-' || it == '_' } &&
                c.lowercase() !in lixo &&
                !c.matches(Regex("(?i)^(js|is|has|ng|v|css)-.*")) &&
                !c.matches(Regex("(?i)^(p|m|mt|mb|ml|mr|px|py|pt|pb|w|h|min|max|gap|text|bg|border|rounded|shadow)-.*")) &&
                !c.matches(Regex("(?i)^[a-z]{1,3}-\\d+$"))
        }
    }

    private fun escaparAttr(valor: String): String =
        valor.replace("\\", "\\\\").replace("\"", "\\\"")
}
