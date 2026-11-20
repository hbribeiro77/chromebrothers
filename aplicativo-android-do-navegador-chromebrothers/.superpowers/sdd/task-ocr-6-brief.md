### Task 6: Wire menu do visor/biblioteca

**Files:**
- Modify: `app/src/main/res/layout/layout_caixa_de_opcoes_do_toque_longo_na_imagem_ampliada_do_visor_da_biblioteca.xml`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` (passar callback/`temChave`/orquestrador ao painel se necessário)

**Interfaces:**
- Em `mostrarOpcoesDoToqueLongoNaImagemAmpliada`: botão Extrair texto VISIBLE só se `temChave`; ao clicar, obter bytes do item aberto ou PNG do `quadroAmpliado` (mesmo padrão de `copiarImagemAberta`) e chamar orquestrador.

- [ ] **Step 1: XML + listener**

Se o painel não tem referência ao orquestrador, adicionar:

```kotlin
fun ligarExtracaoDeTexto(
    temChave: () -> Boolean,
    extrair: (ByteArray) -> Unit,
)
```

- [ ] **Step 2: Wire na Activity** após criar painel/orquestrador.

- [ ] **Step 3: assembleDebug + adb install**

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "...\app\build\outputs\apk\debug\app-debug.apk"
```

- [ ] **Step 4: Smoke manual**

1. Sem chave: botão sumido nos dois menus.  
2. Com chave: extrair na página → modal → copiar.  
3. Enviar ao chat → gaveta abre com texto no campo.  
4. Visor da biblioteca: mesmo fluxo.  
5. Histórico do chat **não** ganha bolhas da OCR.

---

## Spec coverage (self-review)

| Spec | Task |
|------|------|
| Ação nos dois menus | 5, 6 |
| Só com chave | 1 (`mostrarAcao`), 5, 6 |
| Chamada avulsa sem histórico/tools | 3 |
| Modal + Copiar + Enviar (preencher campo) | 2, 3, 4 |
| Loading / erro / sem texto / 2º toque | 1, 2, 3 |
| Compactação multimodal | 3 |

Placeholders: nenhum. Tipos alinhados entre tasks.
