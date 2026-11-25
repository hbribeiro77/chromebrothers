package com.chromebrothers.navegador

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.PermissionDelegate
import org.mozilla.geckoview.GeckoSession.PermissionDelegate.ContentPermission
import org.mozilla.geckoview.GeckoSession.PermissionDelegate.MediaSource

/** A atividade que sabe abrir o pedido de permissão do Android. */
interface PedidorDePermissoesDoAndroid {
    fun pedirPermissoesDoAndroid(permissoes: List<String>, aoResponder: (Map<String, Boolean>) -> Unit)
}

/**
 * Permissões dos sites. O motor grava para sempre o que devolvemos, então "agora não" responde
 * PERGUNTAR (não NEGAR) e só é lembrado até a próxima página. Página escondida nunca pergunta.
 */
class PermissoesDaPaginaNoMotorDoFirefox(
    private val superficie: SuperficieDaPaginaNoMotorDoFirefox,
) : PermissionDelegate {
    private val recusadasNestaPagina = mutableSetOf<String>()
    private val abertas = mutableSetOf<CaixaDoPedidoDaPagina>()

    fun aoComecarPagina() = recusadasNestaPagina.clear()

    fun fecharTudo() {
        abertas.toList().forEach { it.fechar() }
        abertas.clear()
    }

    private fun perguntar(activity: Activity, titulo: String, mensagem: String, aoResponder: (Boolean) -> Unit) {
        lateinit var caixa: CaixaDoPedidoDaPagina
        fun responder(permitiu: Boolean) {
            abertas.remove(caixa)
            aoResponder(permitiu)
        }
        caixa = CaixaDoPedidoDaPagina(
            activity,
            titulo,
            mensagem,
            textoConfirmar = "Permitir",
            textoCancelar = "Agora não",
            fecharAoTocarFora = false,
            aoConfirmar = { responder(true) },
            aoCancelar = { responder(false) },
        )
        abertas.add(caixa)
        caixa.mostrar()
    }

    override fun onContentPermissionRequest(
        session: GeckoSession,
        perm: ContentPermission,
    ): GeckoResult<Int> {
        // Autoplay MUDO liberado como no Chrome e no Firefox: sites com vídeos mudos em loop (higgsfield)
        // ficavam com dezenas de "clique para tocar", repintando sem parar. Com som, segue negado.
        if (perm.permission == PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE) {
            return GeckoResult.fromValue(ContentPermission.VALUE_ALLOW)
        }
        val pedido = descricao(perm.permission) ?: return GeckoResult.fromValue(ContentPermission.VALUE_DENY)
        val site = host(perm.uri)
        val chave = "${perm.permission}|$site|${perm.thirdPartyOrigin}"
        val activity = superficie.atividadeParaDialogos()
        if (activity == null || chave in recusadasNestaPagina) {
            return GeckoResult.fromValue(ContentPermission.VALUE_PROMPT)
        }
        val resultado = GeckoResult<Int>()
        val mensagem = if (perm.permission == PermissionDelegate.PERMISSION_STORAGE_ACCESS) {
            "${host(perm.thirdPartyOrigin)} quer usar os próprios cookies dentro de outro site (comum em logins)."
        } else {
            pedido
        }
        perguntar(activity, "$site pede permissão", mensagem) { permitiu ->
            if (!permitiu) recusadasNestaPagina.add(chave)
            resultado.complete(if (permitiu) ContentPermission.VALUE_ALLOW else ContentPermission.VALUE_PROMPT)
        }
        return resultado
    }

    /** O motor pede isto pela janela mais recente, que pode não ser a visível: não depende de visibilidade. */
    override fun onAndroidPermissionsRequest(
        session: GeckoSession,
        permissions: Array<out String>?,
        callback: PermissionDelegate.Callback,
    ) {
        val pedidor = superficie.context as? PedidorDePermissoesDoAndroid
        if (pedidor == null || permissions.isNullOrEmpty()) {
            callback.reject()
            return
        }
        val fina = Manifest.permission.ACCESS_FINE_LOCATION
        val aproximada = Manifest.permission.ACCESS_COARSE_LOCATION
        // Android 12+: a localização exata só pode ser pedida junto com a aproximada (e a pessoa escolhe).
        val lista = (permissions.toList() + listOfNotNull(aproximada.takeIf { fina in permissions })).distinct()
        pedidor.pedirPermissoesDoAndroid(lista) { concedidas ->
            val ok = permissions.all { concedidas[it] == true || (it == fina && concedidas[aproximada] == true) }
            if (ok) callback.grant() else callback.reject()
        }
    }

    override fun onMediaPermissionRequest(
        session: GeckoSession,
        uri: String,
        video: Array<out MediaSource>?,
        audio: Array<out MediaSource>?,
        callback: PermissionDelegate.MediaCallback,
    ) {
        val activity = superficie.atividadeParaDialogos()
        val cameras = video?.filter { it.source == MediaSource.SOURCE_CAMERA }
        val microfone = audio?.firstOrNull { it.source == MediaSource.SOURCE_MICROPHONE }
        // Pediu e não há (ou é captura de tela, que não oferecemos): recusa.
        if (activity == null || (video != null && cameras.isNullOrEmpty()) || (audio != null && microfone == null)) {
            callback.reject()
            return
        }
        val oQue = when {
            cameras != null && microfone != null -> "a câmera e o microfone"
            cameras != null -> "a câmera"
            else -> "o microfone"
        }
        if (cameras != null && cameras.size > 1) {
            escolherCamera(activity, "${host(uri)} quer usar $oQue", cameras, microfone, callback)
            return
        }
        perguntar(activity, "${host(uri)} pede permissão", "O site quer usar $oQue agora.") { permitiu ->
            if (permitiu) callback.grant(cameras?.firstOrNull(), microfone) else callback.reject()
        }
    }

    private fun escolherCamera(
        activity: Activity,
        titulo: String,
        cameras: List<MediaSource>,
        microfone: MediaSource?,
        callback: PermissionDelegate.MediaCallback,
    ) {
        val densidade = activity.resources.displayMetrics.density
        val lista = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        lateinit var caixa: CaixaDoPedidoDaPagina
        caixa = CaixaDoPedidoDaPagina(
            activity,
            titulo,
            "Toque na câmera que o site pode usar.",
            conteudo = ScrollView(activity).apply { addView(lista) },
            textoConfirmar = null,
            textoCancelar = "Agora não",
            fecharAoTocarFora = false,
            aoCancelar = {
                abertas.remove(caixa)
                callback.reject()
            },
        )
        cameras.forEach { camera ->
            val item = TextView(activity).apply {
                text = nomeDaCamera(camera.name)
                textSize = 16f
                setTextColor(Color.parseColor("#F4F7FB"))
                gravity = Gravity.CENTER_VERTICAL
                minHeight = (48 * densidade).toInt()
                setOnClickListener {
                    caixa.responder {
                        abertas.remove(caixa)
                        callback.grant(camera, microfone)
                    }
                }
            }
            lista.addView(item)
        }
        abertas.add(caixa)
        caixa.mostrar()
    }

    private fun nomeDaCamera(nome: String?): String {
        val texto = nome.orEmpty()
        return when {
            texto.contains("front", ignoreCase = true) -> "Câmera frontal ($texto)"
            texto.contains("back", ignoreCase = true) -> "Câmera traseira ($texto)"
            else -> texto.ifBlank { "Câmera" }
        }
    }

    companion object {
        private const val PREFS = "permissoes_dos_sites_no_motor"
        private const val CHAVE_NEGADAS_DESFEITAS = "negadas_automaticas_desfeitas"

        fun host(uri: String?): String =
            runCatching { Uri.parse(uri).host }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Este site"

        /** null = não perguntamos (segue negado): notificações, VR, rastreamento, autoplay com som. */
        fun descricao(permissao: Int): String? = when (permissao) {
            PermissionDelegate.PERMISSION_GEOLOCATION -> "Saber a sua localização."
            PermissionDelegate.PERMISSION_MEDIA_KEY_SYSTEM_ACCESS -> "Tocar conteúdo protegido (DRM)."
            PermissionDelegate.PERMISSION_STORAGE_ACCESS -> "Usar cookies dentro de outro site."
            PermissionDelegate.PERMISSION_LOCAL_DEVICE_ACCESS -> "Acessar serviços deste aparelho."
            PermissionDelegate.PERMISSION_LOCAL_NETWORK_ACCESS -> "Acessar aparelhos da sua rede local."
            else -> null
        }

        /** Até aqui tudo era negado sem perguntar, e o motor gravou: volta esses sites para "perguntar". */
        fun desfazerNegadasAutomaticasUmaVez(contexto: Context, motor: GeckoRuntime) {
            val prefs = contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (prefs.getBoolean(CHAVE_NEGADAS_DESFEITAS, false)) return
            motor.storageController.allPermissions.accept { lista ->
                lista.orEmpty()
                    .filter { it.value == ContentPermission.VALUE_DENY && descricao(it.permission) != null }
                    .forEach { motor.storageController.setPermission(it, ContentPermission.VALUE_PROMPT) }
                prefs.edit().putBoolean(CHAVE_NEGADAS_DESFEITAS, true).apply()
            }
        }

        /**
         * Lista o que o site tem salvo; tocar num item volta a perguntar. Inclui bloqueios: negar a
         * localização no pedido do Android faz o próprio motor gravar "negado" para o site.
         */
        fun mostrarDoSite(activity: Activity, endereco: String) {
            val motor = MotorDoFirefoxDoNavegadorChromebrothers.obter(activity)
            motor.storageController.getPermissions(endereco).accept { lista ->
                val salvas = lista.orEmpty().filter {
                    it.value != ContentPermission.VALUE_PROMPT && descricao(it.permission) != null
                }
                if (salvas.isEmpty()) {
                    Toast.makeText(activity, "${host(endereco)} não tem permissões salvas.", Toast.LENGTH_SHORT).show()
                } else {
                    CaixaDeEscolhaDoNavegador.mostrar(
                        activity,
                        "${host(endereco)}: toque para voltar a perguntar",
                        salvas.map {
                            val estado = if (it.value == ContentPermission.VALUE_ALLOW) "Permitido" else "Bloqueado"
                            "$estado: ${descricao(it.permission)}" to it
                        },
                    ) { motor.storageController.setPermission(it, ContentPermission.VALUE_PROMPT) }
                }
            }
        }
    }
}
