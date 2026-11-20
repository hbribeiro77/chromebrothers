# Task 4 — Cliente MCP: count + múltiplas URLs (relatório)

Status: concluído (sem commit).

## Arquivo
- `app/src/main/kotlin/com/chromebrothers/navegador/ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers.kt`
  - `gerarImagem(..., quantidade: Int = 1)` propaga para `gerar`.
  - Depois de `aspect_ratio`, se `imagem && quantidade > 1`: `params.put("count", quantidade.coerceIn(1, 4))`.
  - `ResultadoDaGeracaoMcpHiggsfield.Pronto` inalterado (uma URL, um job).
  - Header `If-None-Match: *` do PUT da referência permanece.

## Verificação
- `:app:assembleDebug` — **BUILD SUCCESSFUL** in 15s (37 actionable tasks: 7 executed, 30 up-to-date).
- JAVA_HOME: `C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot`.
- Aviso pré-existente do SDK (XML v4 vs ferramentas que entendem até v3). Não falhou a build.

## Comportamento
- `quantidade <= 1` (default, chat atual, vídeo): não envia `count`.
- `quantidade` 2–4 em imagem: envia esse `count`.
- `quantidade > 4`: envia `count = 4`.
- Continua extraindo e baixando só a primeira URL (`extrairUrlHttp` / `Pronto.enderecoRemoto`).

## Preocupações
- Várias URLs no mesmo job são descartadas. O serviço (task 6) precisa repetir a chamada se o MCP devolver uma URL só.
- Não passar `quantidade` no loop da task 6. O plano faz `repeat(quantidade) { gerarImagem(...) }`. Com o default `1`, cada volta pede uma imagem e não manda `count`. Se o loop também passar `quantidade > 1`, cada volta pede `count` imagens e o cliente guarda só a primeira (gera a mais e joga o resto fora).
- Sem teste unitário: `gerar` é privado e depende de rede/MCP. A checagem desta tarefa é a compilação.
