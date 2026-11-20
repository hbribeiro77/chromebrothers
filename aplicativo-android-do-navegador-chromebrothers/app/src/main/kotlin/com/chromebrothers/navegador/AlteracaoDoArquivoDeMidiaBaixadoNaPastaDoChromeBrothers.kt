package com.chromebrothers.navegador

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import com.chromebrothers.navegador.regras.NomeRenomeadoDoArquivoDeMidiaBaixado
import java.io.IOException

object AlteracaoDoArquivoDeMidiaBaixadoNaPastaDoChromeBrothers {
    fun renomear(resolvedor: ContentResolver, uri: String, nomeAtual: String, nomeDigitado: String): String {
        val nome = NomeRenomeadoDoArquivoDeMidiaBaixado.montar(nomeAtual, nomeDigitado)
            ?: throw IOException("O vídeo precisa de um nome.")
        val valores = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, nome)
        }
        val alterados = resolvedor.update(Uri.parse(uri), valores, null, null)
        if (alterados == 0) throw IOException("A pasta não aceitou o novo nome.")
        return nome
    }

    fun excluir(resolvedor: ContentResolver, uri: String): Boolean {
        return resolvedor.delete(Uri.parse(uri), null, null) > 0
    }
}
