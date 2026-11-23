package com.chromebrothers.navegador

import android.content.Context
import com.chromebrothers.navegador.regras.AtalhoDaPaginaInicial
import com.chromebrothers.navegador.regras.BancoDeNotasDoCampoDoNavegador
import com.chromebrothers.navegador.regras.BancoDeVinculosDoCampoNoSite
import com.chromebrothers.navegador.regras.NotaDoCampoDoNavegador
import com.chromebrothers.navegador.regras.VinculoDoCampoNoSite
import com.chromebrothers.navegador.regras.IncrementosDeDeslocamentoDoPlayerDeVideo
import com.chromebrothers.navegador.regras.ComportamentoDoTecladoPorSiteDoNavegador
import com.chromebrothers.navegador.regras.ModoDeApresentacaoDoSite
import com.chromebrothers.navegador.regras.ModoDoTecladoSobreAPaginaDoNavegador
import com.chromebrothers.navegador.regras.MotorDeBuscaDaBarraDeEndereco
import com.chromebrothers.navegador.regras.TextoDoHistoricoDePaginasVisitadas
import com.chromebrothers.navegador.regras.VisitaDePaginaNoNavegador
import org.json.JSONArray
import org.json.JSONObject

class PreferenciasVisuaisEDeNavegacaoDoChromebrothers(context: Context) {
    private val prefs = context.getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE)

    init {
        if (prefs.getInt(CHAVE_VERSAO_DO_MODO, 1) < 2) {
            val editor = prefs.edit().putInt(CHAVE_VERSAO_DO_MODO, 2)
            if (prefs.getString(CHAVE_MODO, "tablet") == "tablet") {
                editor.putString(CHAVE_MODO, "computador")
            }
            editor.apply()
        }
    }

    var aparenciaEscura: Boolean
        get() = prefs.getString(CHAVE_APARENCIA, "clara") == "escura"
        set(valor) {
            prefs.edit().putString(CHAVE_APARENCIA, if (valor) "escura" else "clara").apply()
        }

    var motor: MotorDeBuscaDaBarraDeEndereco
        get() = MotorDeBuscaDaBarraDeEndereco.peloId(prefs.getString(CHAVE_MOTOR, "google").orEmpty())
        set(valor) {
            prefs.edit().putString(CHAVE_MOTOR, valor.id).apply()
        }

    var incrementoDeDeslocamentoDoVideo: Int
        get() = IncrementosDeDeslocamentoDoPlayerDeVideo.ajustar(
            prefs.getInt(CHAVE_INCREMENTO_DO_VIDEO, IncrementosDeDeslocamentoDoPlayerDeVideo.PADRAO),
        )
        set(valor) {
            prefs.edit()
                .putInt(CHAVE_INCREMENTO_DO_VIDEO, IncrementosDeDeslocamentoDoPlayerDeVideo.ajustar(valor))
                .apply()
        }

    var modoDeSite: ModoDeApresentacaoDoSite
        get() = ModoDeApresentacaoDoSite.peloId(prefs.getString(CHAVE_MODO, "computador").orEmpty())
        set(valor) {
            prefs.edit().putString(CHAVE_MODO, ModoDeApresentacaoDoSite.idDe(valor)).apply()
        }

    var capturarMidiaParaDownload: Boolean
        get() = prefs.getBoolean(CHAVE_CAPTURAR_MIDIA, false)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_CAPTURAR_MIDIA, valor).apply()
        }

    var bloquearAnuncios: Boolean
        get() = prefs.getBoolean(CHAVE_BLOQUEAR_ANUNCIOS, true)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_BLOQUEAR_ANUNCIOS, valor).apply()
        }

    /** Pré-carrega Visualizador + MCP da coleção invisíveis ao abrir o navegador. */
    var preCarregarPaineisDaColecao: Boolean
        get() = prefs.getBoolean(CHAVE_PRE_CARREGAR_PAINEIS_DA_COLECAO, true)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_PRE_CARREGAR_PAINEIS_DA_COLECAO, valor).apply()
        }

    /** Deslizar da borda direita abre o chat; deslizar da junção página/chat para a direita recolhe. */
    var abrirEFecharChatPorGestoDeBorda: Boolean
        get() = prefs.getBoolean(CHAVE_ABRIR_E_FECHAR_CHAT_POR_GESTO_DE_BORDA, true)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_ABRIR_E_FECHAR_CHAT_POR_GESTO_DE_BORDA, valor).apply()
        }

    var moverSelecaoCriandoNovaCamada: Boolean
        get() = prefs.getBoolean(CHAVE_MOVER_SELECAO_CRIANDO_NOVA_CAMADA, true)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_MOVER_SELECAO_CRIANDO_NOVA_CAMADA, valor).apply()
        }

    var instanteDaListaDeBloqueioDeAnuncio: Long
        get() = prefs.getLong(CHAVE_INSTANTE_DA_LISTA_DE_BLOQUEIO, 0L)
        set(valor) {
            prefs.edit().putLong(CHAVE_INSTANTE_DA_LISTA_DE_BLOQUEIO, valor).apply()
        }

    fun lerAtalhos(): List<AtalhoDaPaginaInicial> {
        val bruto = prefs.getString(CHAVE_ATALHOS, "[]") ?: "[]"
        return try {
            val array = JSONArray(bruto)
            buildList {
                for (indice in 0 until array.length()) {
                    val item = array.getJSONObject(indice)
                    add(AtalhoDaPaginaInicial(item.getString("titulo"), item.getString("endereco")))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun gravarAtalhos(atalhos: List<AtalhoDaPaginaInicial>) {
        val array = JSONArray()
        atalhos.forEach { atalho ->
            array.put(JSONObject().put("titulo", atalho.titulo).put("endereco", atalho.endereco))
        }
        prefs.edit().putString(CHAVE_ATALHOS, array.toString()).apply()
    }

    fun lerNotasDoCampoDeTexto(): List<NotaDoCampoDoNavegador> {
        return BancoDeNotasDoCampoDoNavegador.ler(prefs.getString(CHAVE_NOTAS_DO_CAMPO, "").orEmpty())
    }

    fun gravarNotasDoCampoDeTexto(notas: List<NotaDoCampoDoNavegador>) {
        prefs.edit().putString(CHAVE_NOTAS_DO_CAMPO, BancoDeNotasDoCampoDoNavegador.gravar(notas)).apply()
    }

    fun lerVinculosDoCampoDeTexto(): List<VinculoDoCampoNoSite> {
        return BancoDeVinculosDoCampoNoSite.ler(prefs.getString(CHAVE_VINCULOS_DO_CAMPO, "").orEmpty())
    }

    fun gravarVinculosDoCampoDeTexto(vinculos: List<VinculoDoCampoNoSite>) {
        prefs.edit().putString(CHAVE_VINCULOS_DO_CAMPO, BancoDeVinculosDoCampoNoSite.gravar(vinculos)).apply()
    }

    fun lerHistoricoDePaginas(): List<VisitaDePaginaNoNavegador> {
        return TextoDoHistoricoDePaginasVisitadas.ler(prefs.getString(CHAVE_HISTORICO_DE_PAGINAS, "").orEmpty())
    }

    fun gravarHistoricoDePaginas(visitas: List<VisitaDePaginaNoNavegador>) {
        prefs.edit().putString(CHAVE_HISTORICO_DE_PAGINAS, TextoDoHistoricoDePaginasVisitadas.gravar(visitas)).apply()
    }

    /** O histórico agora fica em [HistoricoDoNavegadorChromebrothers]; a lista antiga some após migrar. */
    fun apagarHistoricoAntigoDePaginas() {
        prefs.edit().remove(CHAVE_HISTORICO_DE_PAGINAS).commit()
    }

    /** Modo imersivo no navegador todo: esconde as barras do sistema (voltam ao deslizar da borda). */
    var modoImersivo: Boolean
        get() = prefs.getBoolean(CHAVE_MODO_IMERSIVO, false)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_MODO_IMERSIVO, valor).apply()
        }

    /**
     * Manter o Higgsfield pronto: ao abrir o app carrega (e aquece) uma página do site escondida, que
     * aparece pronta ao abrir o site numa aba e volta a ser guardada ao fechar a aba ou sair do site.
     * Custa memória e processamento em segundo plano; vem desligado.
     */
    var manterHiggsfieldPronto: Boolean
        get() = prefs.getBoolean(CHAVE_MANTER_HIGGSFIELD_PRONTO, false)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_MANTER_HIGGSFIELD_PRONTO, valor).apply()
        }

    /**
     * Acessibilidade das páginas no motor (o que leitores de tela como o TalkBack usam). Desligar
     * economiza processamento em páginas grandes quando há serviço de acessibilidade ativo.
     */
    var acessibilidadeDasPaginas: Boolean
        get() = prefs.getBoolean(CHAVE_ACESSIBILIDADE_DAS_PAGINAS, true)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_ACESSIBILIDADE_DAS_PAGINAS, valor).apply()
        }

    /** Reabrir as abas da última vez ao abrir o navegador (como o Chrome). */
    var restaurarAbasAoAbrir: Boolean
        get() = prefs.getBoolean(CHAVE_RESTAURAR_ABAS_AO_ABRIR, true)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_RESTAURAR_ABAS_AO_ABRIR, valor).apply()
        }

    /** Pedir sugestões de pesquisa ao motor de busca enquanto se digita na barra. */
    var sugestoesDePesquisaOnline: Boolean
        get() = prefs.getBoolean(CHAVE_SUGESTOES_DE_PESQUISA_ONLINE, true)
        set(valor) {
            prefs.edit().putBoolean(CHAVE_SUGESTOES_DE_PESQUISA_ONLINE, valor).apply()
        }

    fun lerModosDoTecladoPorSite(): Map<String, ModoDoTecladoSobreAPaginaDoNavegador> {
        return ComportamentoDoTecladoPorSiteDoNavegador.ler(
            prefs.getString(CHAVE_MODOS_DO_TECLADO_POR_SITE, "").orEmpty(),
        )
    }

    fun gravarModosDoTecladoPorSite(mapa: Map<String, ModoDoTecladoSobreAPaginaDoNavegador>) {
        prefs.edit()
            .putString(CHAVE_MODOS_DO_TECLADO_POR_SITE, ComportamentoDoTecladoPorSiteDoNavegador.gravar(mapa))
            .apply()
    }

    fun modoDoTecladoParaEndereco(endereco: String): ModoDoTecladoSobreAPaginaDoNavegador {
        val host = ComportamentoDoTecladoPorSiteDoNavegador.hospedeiro(endereco)
        return ComportamentoDoTecladoPorSiteDoNavegador.modoPara(lerModosDoTecladoPorSite(), host)
    }

    fun gravarModoDoTecladoParaEndereco(endereco: String, modo: ModoDoTecladoSobreAPaginaDoNavegador) {
        val host = ComportamentoDoTecladoPorSiteDoNavegador.hospedeiro(endereco)
        if (host.isBlank()) return
        gravarModosDoTecladoPorSite(
            ComportamentoDoTecladoPorSiteDoNavegador.guardar(lerModosDoTecladoPorSite(), host, modo),
        )
    }

    fun larguraDoCampoDeTexto(): Int = prefs.getInt(CHAVE_LARGURA_DO_CAMPO, 0)

    fun gravarLarguraDoCampoDeTexto(px: Int) {
        prefs.edit().putInt(CHAVE_LARGURA_DO_CAMPO, px).apply()
    }

    fun alturaDoCampoDeTexto(): Int = prefs.getInt(CHAVE_ALTURA_DO_CAMPO, 0)

    fun gravarAlturaDoCampoDeTexto(px: Int) {
        prefs.edit().putInt(CHAVE_ALTURA_DO_CAMPO, px).apply()
    }

    /** "gaveta" (esmaga a página como o chat) ou "faixa" (embaixo). */
    var modoDoGravadorDeRoteiro: String
        get() = prefs.getString(CHAVE_MODO_DO_GRAVADOR_DE_ROTEIRO, "gaveta").orEmpty().ifBlank { "gaveta" }
        set(valor) {
            val limpo = when (valor.trim().lowercase()) {
                "faixa" -> "faixa"
                else -> "gaveta"
            }
            prefs.edit().putString(CHAVE_MODO_DO_GRAVADOR_DE_ROTEIRO, limpo).apply()
        }

    /** Modelo do modo chat (consultar/planejar). Vazio = usa o mesmo do agente. */
    var modeloDoChatNoModoConversa: String
        get() = prefs.getString("modelo_do_chat_no_modo_conversa", null).orEmpty().ifBlank { modeloDoChat }
        set(valor) {
            prefs.edit().putString("modelo_do_chat_no_modo_conversa", valor.trim()).apply()
        }

    var modeloDoChat: String
        get() {
            val padrao = com.chromebrothers.navegador.regras.PedidoDeChatParaAOpenRouter.MODELO_PADRAO
            val antigo = com.chromebrothers.navegador.regras.PedidoDeChatParaAOpenRouter.MODELO_PADRAO_ANTIGO
            val salvo = prefs.getString(CHAVE_MODELO_DO_CHAT, null).orEmpty()
            if (salvo.isBlank()) return padrao
            if (salvo == antigo && !prefs.getBoolean(CHAVE_MODELO_DO_CHAT_MIGRADO, false)) {
                prefs.edit().putString(CHAVE_MODELO_DO_CHAT, padrao).putBoolean(CHAVE_MODELO_DO_CHAT_MIGRADO, true).apply()
                return padrao
            }
            return salvo
        }
        set(valor) {
            val modelo = valor.trim().ifBlank { com.chromebrothers.navegador.regras.PedidoDeChatParaAOpenRouter.MODELO_PADRAO }
            prefs.edit()
                .putString(CHAVE_MODELO_DO_CHAT, modelo)
                .putBoolean(CHAVE_MODELO_DO_CHAT_MIGRADO, true)
                .apply()
        }

    fun estadoDaPaginaInicial(): String {
        val atalhos = JSONArray()
        lerAtalhos().forEach { atalho ->
            atalhos.put(JSONObject().put("titulo", atalho.titulo).put("endereco", atalho.endereco))
        }
        return JSONObject()
            .put("escura", aparenciaEscura)
            .put("motor", motor.id)
            .put("modo", ModoDeApresentacaoDoSite.idDe(modoDeSite))
            .put("atalhos", atalhos)
            .toString()
    }

    companion object {
        private const val ARQUIVO = "preferencias_do_navegador_chromebrothers"
        private const val CHAVE_APARENCIA = "aparencia"
        private const val CHAVE_MOTOR = "motor"
        private const val CHAVE_MODO = "modo_de_site"
        private const val CHAVE_VERSAO_DO_MODO = "versao_do_modo_de_site"
        private const val CHAVE_ATALHOS = "atalhos"
        private const val CHAVE_INCREMENTO_DO_VIDEO = "incremento_de_deslocamento_do_video"
        private const val CHAVE_CAPTURAR_MIDIA = "capturar_midia_para_download"
        private const val CHAVE_BLOQUEAR_ANUNCIOS = "bloquear_anuncios"
        private const val CHAVE_PRE_CARREGAR_PAINEIS_DA_COLECAO = "pre_carregar_paineis_da_colecao"
        private const val CHAVE_ABRIR_E_FECHAR_CHAT_POR_GESTO_DE_BORDA = "abrir_e_fechar_chat_por_gesto_de_borda"
        private const val CHAVE_MOVER_SELECAO_CRIANDO_NOVA_CAMADA = "mover_selecao_criando_nova_camada"
        private const val CHAVE_INSTANTE_DA_LISTA_DE_BLOQUEIO = "instante_da_lista_de_bloqueio_de_anuncio"
        private const val CHAVE_NOTAS_DO_CAMPO = "notas_do_campo_de_texto"
        private const val CHAVE_VINCULOS_DO_CAMPO = "vinculos_do_campo_de_texto"
        private const val CHAVE_LARGURA_DO_CAMPO = "largura_do_campo_de_texto"
        private const val CHAVE_ALTURA_DO_CAMPO = "altura_do_campo_de_texto"
        private const val CHAVE_MODELO_DO_CHAT = "modelo_do_chat_da_openrouter"
        private const val CHAVE_MODELO_DO_CHAT_MIGRADO = "modelo_do_chat_da_openrouter_migrado"
        private const val CHAVE_MODO_DO_GRAVADOR_DE_ROTEIRO = "modo_do_gravador_de_roteiro"
        private const val CHAVE_HISTORICO_DE_PAGINAS = "historico_de_paginas_visitadas"
        private const val CHAVE_MODOS_DO_TECLADO_POR_SITE = "modos_do_teclado_por_site"
        private const val CHAVE_RESTAURAR_ABAS_AO_ABRIR = "restaurar_abas_ao_abrir"
        private const val CHAVE_MANTER_HIGGSFIELD_PRONTO = "manter_higgsfield_pronto"
        private const val CHAVE_ACESSIBILIDADE_DAS_PAGINAS = "acessibilidade_das_paginas"
        private const val CHAVE_MODO_IMERSIVO = "modo_imersivo"
        private const val CHAVE_SUGESTOES_DE_PESQUISA_ONLINE = "sugestoes_de_pesquisa_online"
    }
}
