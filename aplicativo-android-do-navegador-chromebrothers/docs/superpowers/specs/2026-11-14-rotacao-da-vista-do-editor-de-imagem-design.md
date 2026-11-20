# Design: rotação da vista do editor de imagem

Data: 2026-11-14
Status: aprovado (rotação livre com encaixe nos ângulos retos)

## Problema

No Procreate e em outros editores dá para girar a "folha" com dois dedos para desenhar num ângulo confortável. O editor do Chromebrothers só tem zoom e arraste da vista.

## Comportamento

- **Dois dedos giram a vista**, no mesmo gesto da pinça (zoom e arraste continuam juntos). O giro é em volta do ponto entre os dedos.
- **Limiar:** só começa a girar depois de 3° de giro acumulado no gesto (era 10°; demorava demais para responder).
- **Encaixe:** a ±6° de 0°/90°/180°/270° o ângulo encaixa no reto, com uma vibração leve ao encaixar.
- **Chip de zoom:** mostra também o ângulo quando não é 0° (`67% · 35°`). O toque restaura 100%, centro e 0°.
- **Só a vista:** não altera imagem, peças nem moldura; não entra no desfazer/refazer. Abrir ou trocar de imagem volta a 0° (junto com o zoom).

## Abordagem técnica

A vista atual é um retângulo alinhado aos eixos (`destino`), do qual dependem mais de 90 pontos do editor (desenho, seleção, recorte, régua, alças, conversão de toque). A rotação fica **por fora** desse sistema:

| Parte | Como |
|-------|------|
| Desenho | `onDraw` gira o canvas inteiro em volta do centro da view antes de pintar; tudo o que já existe gira junto |
| Toque | `onTouchEvent` faz uma cópia do evento "desgirada" (`MotionEvent.transform` com a rotação inversa) e entrega à lógica de sempre, que continua vendo uma tela reta |
| Gesto de giro | Na pinça, o ângulo dos dedos na tela = ângulo nas coordenadas desgiradas + ângulo da vista. A cada mudança, o pan compensa para o ponto entre os dedos ficar parado |
| Sempre em pé | Lupa do conta-gotas (desenhada depois de desfazer a rotação, na posição real do dedo) e o chip de zoom (fica fora do editor) |
| Régua fixa na tela | Guardada em coordenadas reais: desenhada depois do `restore`, toques convertidos para a tela real. No traço com régua armada, o ângulo usado na folha é `ângulo da régua − giro da vista`, então a linha sai paralela à régua que se vê |

Regras puras em `RotacaoDaVistaDoEditorDeImagem` (normalizar, diferença com volta em ±180°, encaixe, rótulo do chip), com teste.

## Limitações conhecidas

- ~~A barra flutuante da seleção gira junto com a vista.~~ Resolvido: com a vista girada, a barra da seleção e o "Colar" são desenhados em pé depois de desfazer a rotação, em volta do retângulo reto que cobre a seleção na tela; os botões ficam em coordenadas reais e o toque é convertido antes de testar.
- Com a folha menor que a tela, o pan é zerado pelo enquadramento atual, então o giro acontece em volta do centro.
