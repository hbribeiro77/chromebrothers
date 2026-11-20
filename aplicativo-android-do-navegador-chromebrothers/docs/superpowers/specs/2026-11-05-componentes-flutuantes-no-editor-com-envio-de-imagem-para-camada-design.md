# Componentes flutuantes no editor (envio de `<img>` para a camada)

Data: 2026-11-05  
Status: implementado (2026-11-05)

## Problema

No navegador dá para abrir **componentes** (mini-páginas HTML da coleção flutuante). No editor de imagem isso não existe: não há gancho para a biblioteca nem para as janelas flutuantes. O único caminho componente → editor hoje é `chromebrothers.abrirImagemNoEditor` (bitmap avulso), sem fundir na camada selecionada nem usar o componente como ferramenta ao lado do quadro.

## Objetivo

1. Abrir e usar a **mesma coleção flutuante de componentes** com o editor na frente.
2. Enviar uma **`<img>` específica** do componente para a **camada imagem selecionada**, **fundindo** por cima (espírito do Colar).
3. Entregar em duas fatias: **toque longo** primeiro; **arrastar** depois.

## Abordagem

Reaproveitar o gestor de janelas flutuantes + biblioteca já existentes (não criar WebView “só do editor”). Amarrar a Activity/painel do editor ao mesmo fluxo de abrir componente.

## Comportamento — abrir

1. No chrome do editor: entrada **Componentes** (visível; não só no ⋮ se couber no layout).
2. Seletor no espírito da gaveta/overflow: recentes + lista da biblioteca.
3. Toque abre janela flutuante pelo **mesmo gestor** (máx. 3, arrastar/redimensionar/minimizar iguais ao navegador).
4. Canvas do editor continua usável sob/ao lado das janelas.
5. Ao **fechar o editor**: **fechar** as janelas flutuantes que estavam abertas nesse contexto — não deixar órfãs sobre a UI seguinte.

## Comportamento — enviar `<img>` (fundir)

### Fatia 1 — toque longo

1. Toque longo em `<img>` na WebView do componente → ação **Enviar à camada**.
2. Bridge resolve o bitmap a partir do `src` permitido pela sanitização (`data:`, `blob:`, `file`, `content`, etc.).
3. Destino = camada ativa do editor: **Imagem** aceita; **Folha** vazia **promove** a imagem (como no Colar) e então funde; texto / sem camada → inválido.
4. Sem destino válido → mensagem curta; não funde; não crasha.
5. Decode/`src` ilegível → feedback curto; não crasha.
6. Sucesso → **fundir** o bitmap por cima do bitmap da camada destino + passo de **desfazer**.

### Fatia 2 — arrastar

1. Arrastar a mesma `<img>` até soltar no **quadro** do editor → mesmo fundir da fatia 1.
2. Soltar fora do quadro → cancela.
3. Feedback visual leve durante o arraste.

## Fora de escopo

- Captura da janela inteira do componente.
- Substituir o conteúdo da camada.
- Criar camada/peça nova só com o envio.
- Stickers/templates nativos (componente continua sendo HTML da coleção).

## Técnico

- UI editor → `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador` / `PainelDaColecaoFlutuante…` / biblioteca `ItemDaBibliotecaDeComponentesHtml…`.
- Bridge JS (`chromebrothers`): gesto na `<img>` → entregar bytes/URI ao nativo; API explícita de “enviar à camada do editor” (distinta de só `abrirImagemNoEditor` no visor).
- Editor: API de **fundir bitmap na camada ativa** reutilizando o caminho de fundir do Colar/Mover + undo.
- Regras puras testáveis: destino válido para envio; resultado do gesto (enviar / cancelar / ignorar); fatia 2: soltar no quadro vs fora.
- Limite de 3 janelas: inalterado.

## Ordem de entrega

1. Abrir componentes no editor + toque longo → fundir + undo.
2. Arrastar `<img>` → mesmo fundir.

## Testes

- Unitário (regras): destino válido / inválido; prioridade do gesto de envio.
- Manual: abrir componente no editor; longo na img; desfazer; (fatia 2) arrastar e soltar no quadro / fora.
