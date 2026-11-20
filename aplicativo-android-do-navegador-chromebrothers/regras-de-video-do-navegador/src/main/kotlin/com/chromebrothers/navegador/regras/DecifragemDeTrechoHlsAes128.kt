package com.chromebrothers.navegador.regras

import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * HLS com `METHOD=AES-128`: cada trecho inteiro é AES-128-CBC com padding PKCS#7 (a mesma coisa que o
 * PKCS5Padding do Java para blocos de 16 bytes). A chave tem 16 bytes e vem do endereço da EXT-X-KEY.
 */
object DecifragemDeTrechoHlsAes128 {
    fun chaveValida(chave: ByteArray): Boolean = chave.size == 16

    fun decifrando(entrada: InputStream, chave: ByteArray, ivHex: String): InputStream =
        CipherInputStream(entrada, cifra(chave, ivHex, Cipher.DECRYPT_MODE))

    fun cifra(chave: ByteArray, ivHex: String, modo: Int): Cipher {
        require(chaveValida(chave)) { "A chave AES-128 precisa de 16 bytes." }
        return Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(modo, SecretKeySpec(chave, "AES"), IvParameterSpec(bytesDoHex(ivHex)))
        }
    }

    fun bytesDoHex(hex: String): ByteArray {
        val limpo = hex.padStart(32, '0').takeLast(32)
        return ByteArray(16) { i -> limpo.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }
}
