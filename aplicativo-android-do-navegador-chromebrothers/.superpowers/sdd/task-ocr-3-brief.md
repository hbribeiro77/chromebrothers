### Task 3: Orquestrador (compactar + API + modal)

**Files:**
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt`

**Interfaces:**
- Consumes: `ClienteDeChatDaOpenRouter`, `CompactacaoDoBitmap...paraParte` ou `preparar`, `ExtracaoDeTextoDeImagemViaIaDoNavegador`, `ModalDa...`
- Produces:
  - `class OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador(activity, cofre, lerModelo, aoAbrirChatComTexto)`
  - `fun extrairDeBytes(bytes: ByteArray)`
  - `fun extrairDeBitmap(bitmap: Bitmap)`

- [ ] **Step 1: Implement**

Fluxo:
1. Se `emAndamento` → return (ignorar 2º toque).
2. Decodificar bytes → Bitmap; `CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador.paraParte(bitmap)`.
3. Montar `MensagemDeChat("user", ExtracaoDeTextoDeImagemViaIaDoNavegador.promptDoUsuario(), listOf(parte))`.
4. `modal.mostrarCarregando()`; `emAndamento = true`.
5. Cliente **separado** (não o do painel do chat, para não cancelar envio do usuário):  
   `cliente.enviar(chave, modelo, listOf(mensagem), comFerramentas = false, aoTrecho = { acumular }, aoTerminar = { fim -> ... })`.
6. No fim: se `fim.erro != null` → `modal.mostrarErro(mensagemDeFalha(...))`; senão `modal.mostrarResultado(textoParaExibir(acumulado))`.
7. `emAndamento = false`.
8. Copiar: ClipboardManager + toast `texto_extraido_copiado`.
9. Enviar: `modal.fechar()` + `aoAbrirChatComTexto(texto)`.

- [ ] **Step 2: assembleDebug**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:assembleDebug -p "D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers"
```

Expected: BUILD SUCCESSFUL

---

