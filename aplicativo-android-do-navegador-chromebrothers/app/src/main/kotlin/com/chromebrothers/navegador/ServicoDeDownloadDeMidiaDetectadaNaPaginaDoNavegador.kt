package com.chromebrothers.navegador

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chromebrothers.navegador.regras.TipoDePedidoDeMidiaParaDownload
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val canal = NotificationChannel(CANAL, "Downloads de vídeo", NotificationManager.IMPORTANCE_LOW)
        notificacoes().createNotificationChannel(canal)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACAO_CANCELAR) {
            val id = intent.getIntExtra(EXTRA_ID, 0)
            cancelamentos[id]?.set(true)
            return START_NOT_STICKY
        }
        val endereco = intent?.getStringExtra(EXTRA_ENDERECO).orEmpty()
        val tipo = runCatching {
            TipoDePedidoDeMidiaParaDownload.valueOf(intent?.getStringExtra(EXTRA_TIPO).orEmpty())
        }.getOrDefault(TipoDePedidoDeMidiaParaDownload.ARQUIVO)
        val referer = intent?.getStringExtra(EXTRA_REFERER).orEmpty()
        val agente = intent?.getStringExtra(EXTRA_AGENTE).orEmpty()
        val titulo = intent?.getStringExtra(EXTRA_TITULO).orEmpty()
        val outros = intent?.getStringArrayExtra(EXTRA_OUTROS)?.toList().orEmpty()
        val audioSeparado = intent?.getStringExtra(EXTRA_AUDIO_SEPARADO)?.takeIf { it.isNotBlank() }
        val destino = com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia.peloNome(intent?.getStringExtra(EXTRA_DESTINO))
        // Envio automático: se a opção escolhida falhar, tenta as próximas (endereço + tipo, na ordem).
        val enderecosAlternativos = intent?.getStringArrayExtra(EXTRA_ALTERNATIVAS_ENDERECOS)?.toList().orEmpty()
        val tiposAlternativos = intent?.getStringArrayExtra(EXTRA_ALTERNATIVAS_TIPOS)?.toList().orEmpty()
        val tentativas = listOf(endereco to tipo) + enderecosAlternativos.mapIndexedNotNull { i, alternativo ->
            val tipoAlternativo = runCatching { TipoDePedidoDeMidiaParaDownload.valueOf(tiposAlternativos.getOrNull(i).orEmpty()) }
                .getOrNull() ?: return@mapIndexedNotNull null
            alternativo to tipoAlternativo
        }.filter { it.first != endereco }
        // Motor do Firefox (criado aqui, na thread principal): baixa com os cookies/sessão das páginas.
        val transporte = TransporteComReserva(
            runCatching {
                TransportePeloMotorDoFirefox(org.mozilla.geckoview.GeckoWebExecutor(MotorDoFirefoxDoNavegadorChromebrothers.obter(this)))
            }.getOrNull(),
            TransportePorConexaoComum(),
        )
        val id = (System.nanoTime() and 0x7fffffff).toInt()
        val idDoAcompanhamento = id.toString()
        val acompanhamento = AcompanhamentoGlobalDosDownloadsDeMidiaNaGaveta.atual
        acompanhamento.iniciar(idDoAcompanhamento, titulo, System.currentTimeMillis())
        val cancelado = AtomicBoolean(false)
        cancelamentos[id] = cancelado
        val aviso = notificacao(id, "Baixando vídeo", 0, 0, indeterminado = true)
        startForeground(id, aviso, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        emCurso.incrementAndGet()
        Thread {
            try {
                if (endereco.isBlank()) throw FalhaNoDownloadDeMidia("Não achei o endereço do vídeo.")
                var primeiraFalha: FalhaNoDownloadDeMidia? = null
                var publicado: ArquivoPublicadoDoDownloadDeMidia? = null
                for ((enderecoDaVez, tipoDaVez) in tentativas) {
                    try {
                        publicado = DownloadEmPartesDaMidiaDetectadaNaPagina(
                            pastaTemporaria = File(cacheDir, "downloads-de-midia"),
                            resolvedor = contentResolver,
                            cancelado = cancelado,
                            aoProgresso = { baixado, total ->
                                atualizarProgresso(id, baixado, total)
                                acompanhamento.progresso(idDoAcompanhamento, baixado, total)
                            },
                            transporte = transporte,
                        ).executar(enderecoDaVez, tipoDaVez, referer, agente, titulo, outros, destino, audioSeparado)
                        break
                    } catch (erro: Exception) {
                        val causa = causaDe(erro)
                        if (causa is DownloadCancelado) throw causa
                        // Guarda o motivo da primeira opção (a mais provável) e tenta a próxima.
                        if (primeiraFalha == null) {
                            primeiraFalha = causa as? FalhaNoDownloadDeMidia
                                ?: FalhaNoDownloadDeMidia(com.chromebrothers.navegador.regras.MensagensDeFalhaDoDownloadDeMidia.NAO_TERMINOU)
                        }
                    }
                }
                val nome = publicado ?: throw (primeiraFalha
                    ?: FalhaNoDownloadDeMidia(com.chromebrothers.navegador.regras.MensagensDeFalhaDoDownloadDeMidia.NAO_TERMINOU))
                acompanhamento.concluir(idDoAcompanhamento, nome.uri, titulo, System.currentTimeMillis(), nome.nome)
                mostrar(
                    id,
                    notificacaoPronta(
                        com.chromebrothers.navegador.regras.PastaDoDownloadDeMidia.avisoDePronto(nome.naGaleria),
                        nome.nome,
                    ),
                )
            } catch (erro: Exception) {
                when (val causa = causaDe(erro)) {
                    is DownloadCancelado -> {
                        acompanhamento.cancelar(idDoAcompanhamento)
                        if (emCurso.get() <= 1) stopForeground(STOP_FOREGROUND_REMOVE)
                        else notificacoes().cancel(id)
                    }
                    is FalhaNoDownloadDeMidia -> {
                        acompanhamento.falhar(idDoAcompanhamento, causa.aviso)
                        mostrar(id, notificacaoPronta("Não deu para baixar", causa.aviso))
                    }
                    else -> {
                        val aviso = com.chromebrothers.navegador.regras.MensagensDeFalhaDoDownloadDeMidia.NAO_TERMINOU
                        acompanhamento.falhar(idDoAcompanhamento, aviso)
                        mostrar(id, notificacaoPronta(aviso, "Download"))
                    }
                }
            } finally {
                cancelamentos.remove(id)
                if (emCurso.decrementAndGet() == 0) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
                stopSelf(startId)
            }
        }.start()
        return START_NOT_STICKY
    }

    private fun atualizarProgresso(id: Int, baixado: Long, total: Long) {
        val agora = System.currentTimeMillis()
        val ultimo = ultimosAvisos[id] ?: 0L
        if (agora - ultimo < 700 && total > 0 && baixado < total) return
        ultimosAvisos[id] = agora
        mostrar(id, notificacao(id, "Baixando vídeo", baixado, total, indeterminado = total <= 0))
    }

    private fun notificacao(id: Int, titulo: String, baixado: Long, total: Long, indeterminado: Boolean): Notification {
        val builder = NotificationCompat.Builder(this, CANAL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(titulo)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", pendenteDeCancelar(id))
        if (indeterminado || total <= 0) {
            builder.setProgress(0, 0, true)
            builder.setContentText("Preparando o arquivo")
        } else {
            val percentual = ((baixado * 100) / total).toInt().coerceIn(0, 100)
            builder.setProgress(100, percentual, false)
            builder.setContentText("$percentual%")
        }
        return builder.build()
    }

    private fun notificacaoPronta(titulo: String, texto: String): Notification {
        return NotificationCompat.Builder(this, CANAL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(titulo)
            .setContentText(texto)
            // Explicação completa (por que falhou) aparece ao expandir.
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setAutoCancel(true)
            .build()
    }

    private fun pendenteDeCancelar(id: Int): PendingIntent {
        val cancelar = Intent(this, ServicoDeDownloadDeMidiaDetectadaNaPaginaDoNavegador::class.java).apply {
            action = ACAO_CANCELAR
            putExtra(EXTRA_ID, id)
        }
        return PendingIntent.getService(
            this,
            id,
            cancelar,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun mostrar(id: Int, aviso: Notification) {
        notificacoes().notify(id, aviso)
    }

    private fun notificacoes(): NotificationManager {
        return getSystemService(NotificationManager::class.java)
    }

    companion object {
        const val EXTRA_ENDERECO = "endereco"
        const val EXTRA_TIPO = "tipo"
        const val EXTRA_REFERER = "referer"
        const val EXTRA_AGENTE = "agente"
        const val EXTRA_TITULO = "titulo"
        const val EXTRA_OUTROS = "outros"
        /** [com.chromebrothers.navegador.regras.DestinoDoDownloadDeMidia] pelo nome (padrão: Downloads). */
        const val EXTRA_DESTINO = "destino"
        /** Áudio que vem num arquivo separado do vídeo (VK/Boosty): juntado se o vídeo baixado vier sem som. */
        const val EXTRA_AUDIO_SEPARADO = "audio_separado"
        /** Opções seguintes do envio automático (mesmo tamanho e ordem nos dois arrays). */
        const val EXTRA_ALTERNATIVAS_ENDERECOS = "alternativas_enderecos"
        const val EXTRA_ALTERNATIVAS_TIPOS = "alternativas_tipos"
        const val EXTRA_ID = "id"
        const val ACAO_CANCELAR = "cancelar"
        private const val CANAL = "downloads_de_midia_da_pagina"
        private val emCurso = AtomicInteger(0)
        private val cancelamentos = java.util.concurrent.ConcurrentHashMap<Int, AtomicBoolean>()
        private val ultimosAvisos = java.util.concurrent.ConcurrentHashMap<Int, Long>()

        private fun causaDe(erro: Throwable): Throwable {
            var atual = erro
            while (atual is java.util.concurrent.ExecutionException && atual.cause != null) {
                atual = atual.cause ?: break
            }
            return atual
        }
    }
}
