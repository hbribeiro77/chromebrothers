# Componentes ancorados como abas na gaveta do chat

**Status:** aprovado (“pode fazer”, 2026-11-08)

## Objetivo
Permitir que janelas da coleção flutuante (componentes HTML) sejam **ancoradas na gaveta do chat** como abas, estilo navegador, e **desvinculadas** de volta ao modo flutuante/bolha.

## Comportamento

### Faixa de abas
- Primeira linha útil da gaveta: faixa horizontal rolável de abas.
- Aba fixa **Chat** (sempre presente).
- Demais abas = componentes ancorados (título do componente).
- Toque simples na aba: foca aquele painel (Chat ou WebView do componente).
- Toque longo na aba de componente: ação **Desvincular** (volta flutuante).
- Aba Chat não tem desvincular.

### Ancorar (entrar na gaveta)
Dois caminhos:
1. Botão na janela flutuante (“Ancorar na gaveta”).
2. Toque longo na bolha → Ancorar.

Ao ancorar:
- A WebView/conteúdo **move** para um host na gaveta (não recarrega HTML).
- Janela flutuante e bolha daquele item somem.
- Gaveta abre (se fechada) e a aba do componente fica ativa.

### Desvincular
- Longo na aba → Desvincular.
- Conteúdo volta como janela flutuante (mesma WebView quando possível).
- Aba some; se era a ativa, foca Chat.

### Gaveta fechada
- Componentes ancorados **não** viram bolha e **não** aparecem na tela.
- Só existem dentro da gaveta; ao reabrir a gaveta, as abas e o conteúdo seguem lá.

### Limites
- Máximo de **3** componentes ancorados (mesmo teto das janelas flutuantes visíveis).
- Aba Chat não conta no limite.
- Se já houver 3 ancorados, ancorar de novo recusa com mensagem clara.

### Gravador de roteiro
- Modo exclusivo atual: ao mostrar o host do gravador, a faixa de abas de componentes / hosts de componentes ficam ocultos (como o corpo do chat hoje).
- Ao voltar do gravador ao chat, restaura a faixa e o painel ativo anterior (Chat ou componente).

## Técnica (abordagem)
- **Mover a WebView** da janela flutuante para um `FrameLayout` host por aba na gaveta (não duplicar HTML).
- Gestor: lista de `{ idJanela, titulo, webView/host }` ancorados + id da aba ativa.
- Reuso do padrão `host_do_gravador_na_gaveta_do_chat` / `mostrarHostDoGravador`.

## Fora de escopo (nesta leva)
- Persistência das abas ancoradas entre mortes do processo.
- Várias abas Chat / split view.
- Arrastar aba para reordenar (pode vir depois).

## Critérios de aceite
- [ ] Ancorar por botão da janela e por longo na bolha.
- [ ] Faixa de abas com Chat + N componentes; troca por toque.
- [ ] Longo na aba → desvincular → flutuante de novo.
- [ ] Fechar gaveta: nada flutuando na tela por causa dos ancorados.
- [ ] Limite 3 ancorados com recusa clara.
- [ ] Gravador continua exclusando o corpo como hoje.
