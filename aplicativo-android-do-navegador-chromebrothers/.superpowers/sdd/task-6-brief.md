### Task 6: Medidor circular, lista, excluir, resumir

**Files:**
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/VistaDoMedidorCircularDoHistoricoDoChat.kt`
- Create: `app/src/main/res/layout/layout_item_da_lista_de_conversas_do_chat.xml` (título, data, botão resumir, botão excluir)
- Modify: layout do header do chat — inserir `VistaDoMedidorCircular…` (ex. 28dp) antes do botão Nova conversa; toque abre lista
- Modify: `PainelDoChatDaOpenRouterNoNavegador.kt` — Dialog/Bottom-style lista (`AlertDialog` com `ListView`/`Recycler` simples ou `LinearLayout` em `ScrollView` dentro de dialog)
- Strings: histórico, excluir, resumir, confirmar resumo, erros

**Medidor:** `onDraw` arco de fundo + arco de progresso; cor por faixa (OK `#8AB4F8`, ATENCAO `#FDD663`, CHEIO `#F28B82`); `contentDescription` com percentual; `setProgresso(percentual, faixa)`.

**Lista:** `repositorio.listar()` ordenado por `atualizadaEm` desc; toque item → `carregarConversa`; excluir → confirm → `repositorio.excluir` + atualizar medidor; resumir → confirm → `cliente.enviar(chave, modelo, Compactacao…promptParaResumir(msgs), comFerramentas=false)`, ao terminar sem erro → `aplicar` → `salvar(id, compactado)` com flag resumida no índice → notificar ouvintes → atualizar medidor.

- [ ] **Step 1: View do medidor + layout header**

- [ ] **Step 2: Dialog da lista + excluir + resumir**

- [ ] **Step 3: Atualizar medidor em `ligar()`, pós-save, pós-excluir/resumir**

- [ ] **Step 4: assembleDebug + install**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug
if ($LASTEXITCODE -eq 0) {
  & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
}
```

Expected: BUILD SUCCESSFUL; `Success` no adb.

**Teste manual:** nova conversa com texto+anexo → matar app → retomar; medidor; resumir; excluir; duas janelas mesma conversa.

---

## Self-review (spec coverage)

| Spec | Task |
|------|------|
| JSON mensagens + índice em filesDir | 1, 2, 4 |
| Título 1ª user | 1 |
| Save pós-turno / nova conversa | 5 |
| Lista global retomar/excluir | 6 |
| Medidor + alerta limite | 2, 5, 6 |
| Resumir compacta + listener multi-janela | 3, 4, 6 |
| Teto duro bloqueia | 4, 5 |
| Sem referenciar outra conversa | (fora) |
