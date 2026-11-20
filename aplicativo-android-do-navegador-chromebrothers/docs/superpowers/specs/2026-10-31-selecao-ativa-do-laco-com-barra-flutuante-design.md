# Seleção ativa do laço com barra flutuante

Data: 2026-10-31  
Status: implementado (2026-10-31)

## Problema

Depois de fechar o laço, as ações (destacar/recortar) ficam longe da seleção, no canto da tela. Não há como reposicionar a máscara antes de aplicar. Copiar, mover (destacar) e recortar a moldura não estão claros como três escolhas distintas perto do que o usuário acabou de marcar.

## Objetivo

Com o laço fechado, a seleção vira um objeto temporário de máscara (sem preview de conteúdo), com alças de mover/girar e três ações flutuantes perto da seleção: Copiar, Mover e Recortar.

## Fora de escopo desta entrega

- Replicar o mesmo padrão no retângulo (fica para depois).
- Distinguir “mover máscara” vs “mover peça já destacada” além do já definido (máscara antes; peça depois do Mover).
- Copiar para a área de transferência do sistema.

## Abordagem

**Seleção como objeto temporário no editor (canvas)**  
Máscara, alças e barra flutuante desenhadas/tratadas no `EditorDeRecorteETracoDaImagemDaBiblioteca`, reutilizando a lógica atual de extrair da camada ativa. Modal de confirmação do Recortar no padrão `Dialog` + layout de caixa do editor.

## Estado: Seleção ativa

Ao fechar o laço, entra o modo **Seleção ativa**:

1. Contorno do laço + caixa envolvente.
2. Alça de mover e cabo de girar (padrão visual das peças).
3. Arrastar/girar reposiciona **somente a máscara** no quadro — sem fantasma do conteúdo.
4. Barra flutuante perto da bbox: **Copiar · Mover · Recortar**.
5. Toque em área vazia ou Voltar cancela a seleção sem aplicar e sai do modo Seleção ativa.

Neste modo, Destacar/Recortar do canto deixam de ser o caminho principal (podem ficar ocultos ou inativos enquanto a Seleção ativa existir).

## Ações

Fonte do conteúdo: **camada ativa** (imagem ou folha promovida a bitmap), igual ao Destacar atual — tinta e imagem colada sem diferença de tratamento.

| Ação | Efeito | Depois |
|------|--------|--------|
| **Copiar** | Nova camada com o pedaço mascarado na **mesma posição**; origem intacta (sem buraco). | Fecha Seleção ativa; nova camada fica ativa. |
| **Mover** | Destaca: buraco na origem + nova camada na posição. | Fecha Seleção ativa; nova camada ativa (passa a ser peça com alças reais). |
| **Recortar** | Exige modal de confirmação. Se confirmar: aplica máscara e a **moldura inteira da composição** passa a ser só a seleção (fora some do quadro); recentra/ajusta conteúdo ao novo tamanho. Se cancelar o modal: seleção permanece. | Se aplicou: fecha Seleção ativa. |

## UI da barra flutuante

- Três chips de texto próximos à bbox da seleção.
- Preferência: **acima** da bbox; se não couber na área visível, abaixo ou ao lado.
- A barra acompanha zoom, pan e giro da máscara.
- Toques nos chips não iniciam arraste da máscara.

## Modal de Recortar

- `Dialog` + layout no padrão do editor (`layout_caixa_de_dialogo_do_editor_de_imagem` ou variante só com título/mensagem/botões).
- Mensagem clara: a moldura inteira será reduzida à seleção; o que estiver fora deixa de existir no quadro.
- Botões: Cancelar / Confirmar (textos no padrão do app).

## Regras (módulo `regras-de-video-do-navegador`)

Cobrir com testes unitários, no mínimo:

1. Posicionamento da barra relativa à bbox (acima vs fallback).
2. Efeito semântico de cada ação (copiar sem buraco; mover com buraco; recortar exige confirmação e reduz moldura).
3. Cancelar modal de Recortar mantém a seleção ativa.

## Integração no app

- Editor: estado de Seleção ativa, gestos de máscara, desenho de alças/barra, disparo das ações.
- Painel: modal de Recortar; eventual ocultação dos botões de canto enquanto a Seleção ativa existir.
- Strings pt-BR para chips e modal.

## Critérios de sucesso

1. Fechar laço → máscara com mover/girar e três ações perto dela.
2. Copiar cria camada no mesmo lugar sem alterar a origem.
3. Mover destaca (buraco + nova camada).
4. Recortar só após confirmar no modal; moldura vira a seleção.
5. Cancelar seleção (toque vazio/Voltar) ou cancelar modal não aplica efeito indesejado.
