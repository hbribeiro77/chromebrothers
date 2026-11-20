package com.chromebrothers.navegador

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import com.chromebrothers.navegador.regras.FerramentasDePinceisDoChat
import com.chromebrothers.navegador.regras.FonteDoGranulado
import com.chromebrothers.navegador.regras.FormaDoCarimbo
import com.chromebrothers.navegador.regras.ParteDeConteudoDoChat
import com.chromebrothers.navegador.regras.PincelDoMotor
import com.chromebrothers.navegador.regras.TextoJsonDoChat

/**
 * Executa as ferramentas de pincéis do chat. Roda fora da thread principal; [aoFim] volta na principal com
 * o texto para o modelo e, no teste, a imagem da amostra (que o modelo vê).
 */
class ExecucaoDasFerramentasDePinceisDoChat(
    private val activity: Activity,
    /** Seleciona o pincel no editor de imagem; false se o editor não estiver aberto. */
    private val usarNoEditor: (String) -> Boolean,
    /** Última imagem anexada no chat (para virar ponta/textura). */
    private val ultimaImagemAnexada: () -> ParteDeConteudoDoChat?,
) {
    private val regra = FerramentasDePinceisDoChat

    fun executar(nome: String, argumentos: String, aoFim: (String, ParteDeConteudoDoChat?) -> Unit) {
        CatalogoDePinceisDoMotor.iniciar(activity)
        // usar_pincel mexe no editor (thread principal); a imagem anexada também é lida antes de sair dela.
        if (nome == regra.USAR) {
            aoFim(usar(argumentos), null)
            return
        }
        val anexada = if (nome == regra.IMAGEM_COMO_PONTA) ultimaImagemAnexada() else null
        Thread {
            val (texto, imagem) = runCatching {
                when (nome) {
                    regra.LISTAR -> regra.textoDaLista(CatalogoDePinceisDoMotor.todos(), CatalogoDePinceisDoMotor::ehPredefinido) to null
                    regra.LER -> ler(argumentos) to null
                    regra.CRIAR -> criar(argumentos) to null
                    regra.AJUSTAR -> ajustar(argumentos) to null
                    regra.TESTAR -> testar(argumentos)
                    regra.IMAGEM_COMO_PONTA -> imagemComoPonta(argumentos, anexada) to null
                    else -> "Ferramenta de pincel desconhecida." to null
                }
            }.getOrElse { erro -> "Não deu certo: ${erro.message ?: erro.javaClass.simpleName}." to null }
            activity.runOnUiThread { aoFim(texto, imagem) }
        }.start()
    }

    private fun id(argumentos: String): String = TextoJsonDoChat.valorDaChave(argumentos, "id")?.trim().orEmpty()

    private fun pincel(id: String): PincelDoMotor? = if (id.isBlank()) null else CatalogoDePinceisDoMotor.porId(id)

    private fun ler(argumentos: String): String {
        val p = pincel(id(argumentos)) ?: return "Não achei o pincel «${id(argumentos)}». Use listar_pinceis."
        return regra.textoDoPincel(p, CatalogoDePinceisDoMotor.ehPredefinido(p.id))
    }

    private fun criar(argumentos: String): String {
        val baseId = TextoJsonDoChat.valorDaChave(argumentos, "base")?.trim().orEmpty()
        val base = if (baseId.isBlank()) null else pincel(baseId)
        if (baseId.isNotBlank() && base == null) return "Não achei o pincel base «$baseId». Use listar_pinceis."
        val novoId = CatalogoDePinceisDoMotor.novoId()
        val inicio = (base ?: PincelDoMotor(id = novoId, nome = "Pincel")).copy(id = novoId)
        val aplicado = regra.aplicarParametros(inicio, argumentos)
        CatalogoDePinceisDoMotor.salvar(aplicado.pincel)
        return "Pincel criado: id=${aplicado.pincel.id}, nome=${aplicado.pincel.nome}." + avisos(aplicado.avisos) +
            " Agora rode testar_pincel(id=${aplicado.pincel.id})."
    }

    private fun ajustar(argumentos: String): String {
        val atual = pincel(id(argumentos)) ?: return "Não achei o pincel «${id(argumentos)}». Use listar_pinceis."
        val copia = CatalogoDePinceisDoMotor.ehPredefinido(atual.id)
        val base = if (copia) atual.copy(id = CatalogoDePinceisDoMotor.novoId(), nome = atual.nome + " (meu)") else atual
        val aplicado = regra.aplicarParametros(base, argumentos)
        CatalogoDePinceisDoMotor.salvar(aplicado.pincel)
        val prefixo = if (copia) "O pincel vem com o app; criei a cópia id=${aplicado.pincel.id} (${aplicado.pincel.nome}). " else ""
        return prefixo + "Ajustado: ${aplicado.pincel.id}." + avisos(aplicado.avisos) + " Rode testar_pincel para ver."
    }

    private fun testar(argumentos: String): Pair<String, ParteDeConteudoDoChat?> {
        val p = pincel(id(argumentos)) ?: return "Não achei o pincel «${id(argumentos)}». Use listar_pinceis." to null
        val cor = TextoJsonDoChat.valorDaChave(argumentos, "cor")?.trim()?.let { runCatching { Color.parseColor(it) }.getOrNull() }
            ?: Color.BLACK
        val amostra = AmostraDoPincelParaOChat.desenhar(p, cor)
        val preparada = CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador.preparar(activity, amostra)
        amostra.recycle()
        return if (preparada == null) {
            "Não consegui desenhar a amostra." to null
        } else {
            regra.relatoDoTeste(p) to preparada.parte
        }
    }

    private fun usar(argumentos: String): String {
        val p = pincel(id(argumentos)) ?: return "Não achei o pincel «${id(argumentos)}». Use listar_pinceis."
        return if (usarNoEditor(p.id)) {
            "Pincel «${p.nome}» selecionado no editor."
        } else {
            "O editor de imagem não está aberto. O pincel «${p.nome}» já está na lista de pincéis do editor (Meus pincéis)."
        }
    }

    private fun imagemComoPonta(argumentos: String, anexada: ParteDeConteudoDoChat?): String {
        if (anexada == null || anexada.dados.isBlank()) return "Não há imagem anexada no chat. Peça ao usuário para anexar (ou gere uma e anexe)."
        val bytes = runCatching { Base64.decode(anexada.dados, Base64.DEFAULT) }.getOrNull() ?: return "Não consegui ler a imagem anexada."
        val imagem = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return "A imagem anexada não abriu."
        val inverter = Regex("\"inverter\"\\s*:\\s*true").containsMatchIn(argumentos)
        val mascara = CatalogoDePinceisDoMotor.mascaraDaImagem(imagem, inverter)
        imagem.recycle()
        val nome = CatalogoDePinceisDoMotor.salvarForma(mascara).also { mascara.recycle() }
            ?: return "Não consegui gravar a ponta."
        val comoGranulado = TextoJsonDoChat.valorDaChave(argumentos, "como")?.trim()?.lowercase() == "granulado"
        val alvo = pincel(id(argumentos))
            ?: return "Criei a ${if (comoGranulado) "textura" else "ponta"} «$nome». Passe id em usar_imagem_como_ponta para aplicar num pincel."
        val copia = CatalogoDePinceisDoMotor.ehPredefinido(alvo.id)
        val base = if (copia) alvo.copy(id = CatalogoDePinceisDoMotor.novoId(), nome = alvo.nome + " (meu)") else alvo
        val novo = if (comoGranulado) {
            base.copy(granulado = FonteDoGranulado.IMAGEM, arquivoDoGranulado = nome)
        } else {
            base.copy(forma = FormaDoCarimbo.IMAGEM, arquivoDaForma = nome)
        }
        CatalogoDePinceisDoMotor.salvar(novo)
        return "Apliquei a ${if (comoGranulado) "textura" else "ponta"} no pincel ${novo.id} (${novo.nome}). Rode testar_pincel para ver."
    }

    private fun avisos(lista: List<String>): String = if (lista.isEmpty()) "" else " Avisos: " + lista.joinToString(" ")
}
