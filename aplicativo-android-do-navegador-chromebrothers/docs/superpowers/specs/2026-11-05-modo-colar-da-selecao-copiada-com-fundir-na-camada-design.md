# Modo Colar da seleção copiada (fundir na camada)

Data: 2026-11-05  
Status: implementado (2026-11-05)

## Problema

O chip **Copiar** cria uma camada no mesmo lugar, sem moldura de transformar nem confirmação. Parece que “não aconteceu nada” e não dá para escolher onde fundir.

## Objetivo

Copiar entra num **modo Colar**: prévia flutuante com moldura amarela (mover / redimensionar / girar) e chip **Colar**. A camada ativa na lista é o destino. Colar funde no bitmap da imagem destino. Cancelar descarta a prévia.

## Comportamento

1. Seleção ativa (laço ou retângulo) → **Copiar**.
2. Extrai bitmap mascarado; origem **sem** buraco.
3. Prévia temporária (peça em `objetos`, **fora da lista de camadas**), selecionada, `pecaEmModoTransformar`, chip **Colar**.
4. Clique na lista → só muda o **destino** (`idDaCamadaAtiva`); a prévia continua transformável.
5. Destino válido = peça `Imagem` (não a própria prévia). Folha/texto: Colar não confirma (destino inválido).
6. **Colar** → funde prévia no bitmap do destino (com posição/ângulo/tamanho atuais) + undo (`FundirPecaTransformada` / equivalente) + encerra modo.
7. **Cancelar** → toque fora da prévia/chrome **ou** troca de ferramenta → remove prévia, origem intacta, **sem** passo de histórico da cópia.
8. Gestos na prévia **não** empilham `Transformacao` até o Colar (prévia efêmera).

## Diferença do Mover

| | Mover | Copiar → Colar |
|---|---|---|
| Origem | buraco | intacta |
| Ao encerrar fora | funde ou mantém camada (pref.) | **cancela** (descarta prévia) |
| Confirmação | implícita ao encerrar / preferência | chip **Colar** |

## Fora de escopo

- Colar em Folha (promover) nesta entrega.
- Offset automático da prévia.
- Área de transferência do sistema.

## Técnico

- Regra pura: modo colar / destino válido / cancelar vs confirmar.
- `AcaoDaSelecaoAtivaDoLaco.COLAR` + efeito confirmar fundir.
- Editor: flag de modo; armar prévia; filtrar lista; Colar / cancelar; reutilizar lógica de fundir do Mover.
