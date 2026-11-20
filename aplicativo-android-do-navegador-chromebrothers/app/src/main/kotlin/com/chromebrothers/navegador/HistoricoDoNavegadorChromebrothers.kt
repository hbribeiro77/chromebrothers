package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.chromebrothers.navegador.regras.EntradaDoHistoricoDoNavegador
import com.chromebrothers.navegador.regras.RegrasDoHistoricoCompletoDoNavegador
import java.util.concurrent.Executors

/**
 * Histórico completo do navegador (90 dias, contagem de visitas e de digitação), compartilhado
 * por todas as janelas. Fica num SQLite próprio; as consultas da barra usam a cópia em memória
 * (só na thread principal) e as gravações vão para uma thread de fundo.
 */
object HistoricoDoNavegadorChromebrothers {
    private const val TAG = "ChromeBrothersHistorico"
    private val principal = Handler(Looper.getMainLooper())
    private val gravador = Executors.newSingleThreadExecutor { r -> Thread(r, "historico-do-navegador") }
    private val memoria = LinkedHashMap<String, EntradaDoHistoricoDoNavegador>()
    private val pendentesAntesDeCarregar = mutableListOf<(MutableMap<String, EntradaDoHistoricoDoNavegador>) -> Unit>()
    private var banco: BancoDoHistorico? = null
    private var carregado = false
    private var instantaneo: List<EntradaDoHistoricoDoNavegador>? = null

    fun iniciar(context: Context) {
        if (banco != null) return
        val app = context.applicationContext
        val ajudante = BancoDoHistorico(app)
        banco = ajudante
        gravador.execute {
            val agora = System.currentTimeMillis()
            val db = ajudante.writableDatabase
            migrarHistoricoAntigoSeNecessario(app, db, agora)
            val corte = agora - RegrasDoHistoricoCompletoDoNavegador.RETENCAO_MS
            db.delete(TABELA, "$C_ULTIMA < ?", arrayOf(corte.toString()))
            val lidas = ler(db)
            val podadas = RegrasDoHistoricoCompletoDoNavegador.podar(lidas, agora)
            if (podadas.size < lidas.size) {
                val manter = podadas.mapTo(HashSet()) { it.endereco }
                lidas.filterNot { it.endereco in manter }.forEach {
                    db.delete(TABELA, "$C_URL = ?", arrayOf(it.endereco))
                }
            }
            principal.post {
                // Mais recentes primeiro na ordem de inserção.
                podadas.forEach { memoria[it.endereco] = it }
                carregado = true
                pendentesAntesDeCarregar.forEach { it(memoria) }
                pendentesAntesDeCarregar.clear()
                instantaneo = null
                Log.i(TAG, "histórico carregado: ${memoria.size} páginas")
            }
        }
    }

    /** Cópia para ranquear (thread principal). */
    fun entradas(): List<EntradaDoHistoricoDoNavegador> {
        instantaneo?.let { return it }
        val copia = memoria.values.toList()
        instantaneo = copia
        return copia
    }

    fun entrada(endereco: String): EntradaDoHistoricoDoNavegador? = memoria[endereco.trim()]

    /** Recentes primeiro (para a faixa de atalhos e telas que só precisam da ordem). */
    fun recentes(): List<EntradaDoHistoricoDoNavegador> =
        entradas().sortedByDescending { it.ultimaVisitaMs }

    fun registrarVisita(endereco: String, titulo: String?, digitada: Boolean) {
        val url = endereco.trim()
        if (!RegrasDoHistoricoCompletoDoNavegador.deveGuardar(url)) return
        val agora = System.currentTimeMillis()
        aplicar { mapa ->
            val nova = RegrasDoHistoricoCompletoDoNavegador.registrarVisita(
                mapa[url],
                url,
                titulo.orEmpty(),
                digitada,
                agora,
            )
            mapa.remove(url)
            mapa[url] = nova
            gravar(nova)
        }
    }

    fun atualizarTitulo(endereco: String, titulo: String?) {
        val url = endereco.trim()
        val nome = titulo?.trim().orEmpty()
        if (nome.isEmpty() || !RegrasDoHistoricoCompletoDoNavegador.deveGuardar(url)) return
        aplicar { mapa ->
            val atual = mapa[url] ?: return@aplicar
            if (atual.titulo == nome) return@aplicar
            val nova = atual.copy(titulo = nome)
            mapa[url] = nova
            gravar(nova)
        }
    }

    fun esquecer(endereco: String) {
        val url = endereco.trim()
        aplicar { mapa ->
            mapa.remove(url)
            gravador.execute {
                runCatching { banco?.writableDatabase?.delete(TABELA, "$C_URL = ?", arrayOf(url)) }
            }
        }
    }

    private fun aplicar(mudanca: (MutableMap<String, EntradaDoHistoricoDoNavegador>) -> Unit) {
        val rodar = Runnable {
            if (carregado) {
                mudanca(memoria)
                instantaneo = null
            } else {
                pendentesAntesDeCarregar.add(mudanca)
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) rodar.run() else principal.post(rodar)
    }

    private fun gravar(entrada: EntradaDoHistoricoDoNavegador) {
        gravador.execute {
            runCatching {
                banco?.writableDatabase?.insertWithOnConflict(
                    TABELA,
                    null,
                    valores(entrada),
                    SQLiteDatabase.CONFLICT_REPLACE,
                )
            }.onFailure { Log.w(TAG, "falha ao gravar histórico: $it") }
        }
    }

    private fun valores(entrada: EntradaDoHistoricoDoNavegador) = ContentValues().apply {
        put(C_URL, entrada.endereco)
        put(C_TITULO, entrada.titulo)
        put(C_VISITAS, entrada.visitas)
        put(C_DIGITADAS, entrada.digitadas)
        put(C_ULTIMA, entrada.ultimaVisitaMs)
    }

    private fun ler(db: SQLiteDatabase): List<EntradaDoHistoricoDoNavegador> {
        val saida = mutableListOf<EntradaDoHistoricoDoNavegador>()
        db.query(
            TABELA,
            arrayOf(C_URL, C_TITULO, C_VISITAS, C_DIGITADAS, C_ULTIMA),
            null,
            null,
            null,
            null,
            "$C_ULTIMA DESC",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                saida.add(
                    EntradaDoHistoricoDoNavegador(
                        endereco = cursor.getString(0),
                        titulo = cursor.getString(1).orEmpty(),
                        visitas = cursor.getInt(2),
                        digitadas = cursor.getInt(3),
                        ultimaVisitaMs = cursor.getLong(4),
                    ),
                )
            }
        }
        return saida
    }

    /** Traz para cá os 80 endereços que ficavam nas preferências (uma vez só). */
    private fun migrarHistoricoAntigoSeNecessario(context: Context, db: SQLiteDatabase, agora: Long) {
        val preferencias = PreferenciasVisuaisEDeNavegacaoDoChromebrothers(context)
        val antigas = preferencias.lerHistoricoDePaginas()
        if (antigas.isEmpty()) return
        val migradas = RegrasDoHistoricoCompletoDoNavegador.migrarDoHistoricoAntigo(antigas, agora)
        db.beginTransaction()
        try {
            migradas.forEach {
                db.insertWithOnConflict(TABELA, null, valores(it), SQLiteDatabase.CONFLICT_IGNORE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        preferencias.apagarHistoricoAntigoDePaginas()
        Log.i(TAG, "migradas ${migradas.size} páginas do histórico antigo")
    }

    private const val TABELA = "paginas"
    private const val C_URL = "url"
    private const val C_TITULO = "titulo"
    private const val C_VISITAS = "visitas"
    private const val C_DIGITADAS = "digitadas"
    private const val C_ULTIMA = "ultima_visita"

    private class BancoDoHistorico(context: Context) :
        SQLiteOpenHelper(context, "historico-do-navegador.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE $TABELA (" +
                    "$C_URL TEXT PRIMARY KEY NOT NULL, " +
                    "$C_TITULO TEXT NOT NULL DEFAULT '', " +
                    "$C_VISITAS INTEGER NOT NULL DEFAULT 1, " +
                    "$C_DIGITADAS INTEGER NOT NULL DEFAULT 0, " +
                    "$C_ULTIMA INTEGER NOT NULL)",
            )
            db.execSQL("CREATE INDEX idx_${TABELA}_ultima ON $TABELA($C_ULTIMA)")
        }

        override fun onUpgrade(db: SQLiteDatabase, antiga: Int, nova: Int) = Unit
    }
}
