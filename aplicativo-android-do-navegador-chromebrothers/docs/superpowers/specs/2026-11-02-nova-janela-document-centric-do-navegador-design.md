# Nova janela document-centric do navegador

Data: 2026-11-02  
Status: implementado (2026-11-02)

## Objetivo

Permitir até **3 janelas** do ChromeBrothers no overview do Android (estilo Chrome), cada uma com suas próprias abas em memória. Disparo pelo menu ⋮, pelo gerenciador de abas e pela opção “Abrir em nova janela” no menu do link.

## Decisões

| Tema | Escolha |
|------|---------|
| Modelo | Document-centric na **mesma** `AtividadePrincipalDoNavegadorChromebrothers` |
| Entrada UI | Overflow **e** gerenciador de abas |
| Conteúdo ao abrir pelo botão | Página inicial (aba nova) |
| Link | Item “Abrir em nova janela” (além de “Abrir em nova aba”) |
| Limite | **3** janelas; a 4ª tentativa recusa com aviso |
| Estado de abas | Isolado por Activity (sem mover aba entre janelas nesta versão) |
| Disco | Biblioteca, downloads, prefs e chave OpenRouter **compartilhados** no processo |

## Abordagem

**A (escolhida)** — `documentLaunchMode="always"` + Intent `FLAG_ACTIVITY_NEW_DOCUMENT | FLAG_ACTIVITY_MULTIPLE_TASK`, com EXTRA opcional de URL.

Descartadas: Activity espelho (B); só split-screen (C).

## Arquitetura

1. **Manifest** — na Activity principal: `android:documentLaunchMode="always"` e `android:maxRecents="3"` (ou equivalente suportado).
2. **Regra pura** (`regras-de-video-do-navegador`) — decidir se pode abrir nova janela dado o número atual de tasks/janelas do app; mensagens de ok/recusa.
3. **App** — `abrirNovaJanela(endereco: String? = null)` monta o Intent, aplica flags, conta tasks via `ActivityManager` (ou API adequada) e lança ou avisa.
4. **UI** — strings + item no overflow, botão/ação no gerenciador, opção no menu contextual do link.
5. **Lifecycle** — cada instância mantém sua `GestaoDeAbasDoNavegadorChromebrothers` e WebViews; sem sincronizar listas de abas entre janelas.

## Fluxos

- **Nova janela (botão/menu)** → se &lt; 3 → nova task com página inicial; senão → toast/aviso.
- **Abrir link em nova janela** → mesma checagem; EXTRA com o URL do link.
- **Fechar** → usuário fecha a task no overview ou encerra a Activity; as outras continuam.

## Multi-instância no split (follow-up)

- Padrão Samsung Internet / Chrome: Activities `…Secundaria` / `…Terciaria` + trampolim `AtividadeDeMultiInstancia…`.
- Meta Samsung `com.samsung.android.multiwindow.activity.alias.targetactivity` aponta para o trampolim.
- `multiinstance.enable` + `PROPERTY_SUPPORTS_MULTI_INSTANCE_SYSTEM_UI` + `launchMode=singleTask`.
- Em multi-janela, flags com `LAUNCH_ADJACENT`.

## Fora de escopo (v1)

- Mover aba de uma janela para outra.
- Dual Apps / recursos só OEM.
- Picture-in-Picture.
- Isolar biblioteca ou downloads por janela.
- Ferramenta do chat “abrir em nova janela” (pode vir depois).

## Critério de sucesso

1. Overflow e gerenciador abrem uma segunda (e terceira) entrada no recentes.
2. Quarta tentativa é recusada com mensagem clara.
3. “Abrir em nova janela” no link abre o endereço na nova task.
4. Abas de uma janela não aparecem na outra; biblioteca/gaveta seguem compartilhadas.
5. `:regras-de-video-do-navegador:test` + `:app:assembleDebug` passam; APK instalável.
