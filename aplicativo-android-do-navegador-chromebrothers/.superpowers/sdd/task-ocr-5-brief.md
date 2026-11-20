### Task 5: Wire menu da imagem na página

**Files:**
- Modify: `app/src/main/res/layout/layout_caixa_de_opcoes_da_imagem_tocada_na_pagina_do_navegador.xml`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/AtividadePrincipalDoNavegadorChromebrothers.kt`

**Interfaces:**
- Consumes: `ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao`, orquestrador
- Extends `CopiaDeImagem...` com `temChave: () -> Boolean` e `aoExtrairTexto: (ByteArray) -> Unit`

- [ ] **Step 1: Botão no XML** após Salvar (ou antes de Cancelar), id `botao_extrair_texto_da_imagem_da_pagina_no_toque_longo`, text `@string/extrair_texto_da_imagem`, default `visibility="gone"` ou GONE no código.

- [ ] **Step 2: Em `perguntar`**

```kotlin
val extrair = raiz.findViewById<View>(R.id.botao_extrair_texto_da_imagem_da_pagina_no_toque_longo)
extrair.visibility = if (temChave()) View.VISIBLE else View.GONE
extrair.setOnClickListener {
    dialogo.dismiss()
    baixar(vista, endereco, DestinoDaImagemDaPagina.EXTRAIR_TEXTO) // OU callback dedicado após baixar
}
```

Preferência: adicionar `EXTRAIR_TEXTO` ao enum `DestinoDaImagemDaPagina` e em `receberImagemDaPagina` despachar para o orquestrador — **ou** callback `aoExtrairTexto` separado no construtor de `CopiaDeImagem...` chamado com os bytes (mais limpo que misturar biblioteca).

Recomendado no código:

```kotlin
enum class DestinoDaImagemDaPagina { COPIAR, SALVAR, EDITAR, ACRESCENTAR, EXTRAIR_TEXTO }
```

Em `AtividadePrincipal.receberImagemDaPagina`: se `EXTRAIR_TEXTO` → `orquestrador.extrairDeBytes(bytes)`.

Criar orquestrador na Activity com `CofreDaChaveDaOpenRouter`, `{ preferencias.modeloDoChat }`, `{ painelDoChat.abrirComTextoNoCampo(it) }`.

- [ ] **Step 3: assembleDebug**

---

