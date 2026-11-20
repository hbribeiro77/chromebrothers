package com.chromebrothers.navegador

/**
 * localState do drag iniciado por long-press nos cartões da gaveta da biblioteca.
 * Usado no drop (Task 4) para decidir se converte vídeo em quadro JPEG.
 */
class EstadoDoArrasteDaBiblioteca(
    val tipo: String, // "image" | "video"
    val caminhoLocal: String,
    val id: String,
)
