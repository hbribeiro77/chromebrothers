### Task 5: Gravação com id + preview

**Files:**
- Modify: `app/.../GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers.kt`

**Interfaces:**
- Produces: `data class ImagemHiggsfieldGuardada(val id: String, val caminhoArquivo: String)`  
  `fun guardarImagemComMetadados(bytes: ByteArray, prompt: String): ImagemHiggsfieldGuardada?`

- [ ] **Step 1:** Implementar usando `guardarQuadro` existente; caminho = `File(pastaDaBiblioteca, item.nomeDoArquivo).absolutePath`.
- [ ] **Step 2:** Manter `guardarImagem` antigo delegando para o novo (texto de sucesso igual).

---

