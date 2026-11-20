### Task 2: Modal UI

**Files:**
- Create: `app/src/main/res/layout/layout_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador.xml`
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt`
- Modify: `app/src/main/res/values/strings_do_navegador_chromebrothers.xml`

**Interfaces:**
- Consumes: `ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados`
- Produces:
  - `class ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador(activity, aoCopiar, aoEnviarAoChat)`
  - `fun mostrarCarregando()`
  - `fun mostrarResultado(texto: String)`
  - `fun mostrarErro(mensagem: String)`
  - `fun fechar()`

- [ ] **Step 1: Add strings**

```xml
<string name="extrair_texto_da_imagem">Extrair texto</string>
<string name="titulo_da_extracao_de_texto_da_imagem">Texto da imagem</string>
<string name="extraindo_texto_da_imagem">Extraindo texto…</string>
<string name="copiar_texto_extraido_da_imagem">Copiar</string>
<string name="enviar_texto_extraido_ao_chat">Enviar ao chat</string>
<string name="texto_extraido_copiado">Texto copiado</string>
```

- [ ] **Step 2: Create layout**

Reuse visual da caixa de opções: fundo `@drawable/fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem`, título, `ProgressBar` + TextView de status (loading), `TextView` com `textIsSelectable="true"` e `maxHeight` ~320dp + scroll, botões Copiar / Enviar ao chat / Fechar (mesmo estilo transparente das outras caixas).

- [ ] **Step 3: Implement modal class**

```kotlin
class ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador(
    private val activity: AppCompatActivity,
    private val aoCopiar: (String) -> Unit,
    private val aoEnviarAoChat: (String) -> Unit,
) {
    // Dialog 420dp; mostrarCarregando esconde texto/botões de ação;
    // mostrarResultado/mostrarErro atualiza TextView e habilita Copiar/Enviar via
    // ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados(texto)
}
```

Verificação: `assembleDebug` compila (pode ser junto da Task 3).

---

