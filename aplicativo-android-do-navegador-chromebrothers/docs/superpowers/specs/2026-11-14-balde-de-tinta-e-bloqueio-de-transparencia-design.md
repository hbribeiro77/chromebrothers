# Design: balde de tinta e bloqueio de transparência

Data: 2026-11-14
Status: implementado

## Balde de tinta

- **Gesto:** arrastar a bolinha de cor da barra de cima até a folha. A gota aparece 28 dp acima do dedo e o preenchimento cai no centro dela. Um toque simples na bolinha continua abrindo o painel de cor.
- **Referência:** a imagem de tudo o que se vê, com todas as camadas visíveis e sem o papel, em até 4 Mpx. Assim dá para colorir numa camada embaixo do contorno.
- **Área:** o preenchimento pega a área contínua cuja cor fica dentro da tolerância, medida pela maior diferença entre os canais, alfa incluído. Dois pixels transparentes contam sempre como iguais. A área cresce 1 px para cobrir a borda suavizada da linha.
- **Destino:** a camada ativa. Uma camada de traços vira pixels antes de receber o preenchimento.
  - O giro e o espelho da camada são desfeitos ao copiar a área para os pixels dela.
  - A opacidade usada é a do pincel.
- **Desfazer:** o preenchimento entra no desfazer como `Passo.AlteraBitmap`.
- **Tolerância:** o controle fica no painel de cor (padrão 25%) e o valor é salvo nas preferências do estúdio.
- **Código:** a regra pura está em `PreenchimentoDoBalde` (faixas horizontais com pilha própria, crescimento da borda e limite da referência) e tem teste.

## Bloqueio de transparência

- O flag `alfaBloqueado` fica em cada peça e é ligado e desligado pelo menu da camada. A lista de camadas mostra "transparência bloqueada" na camada.
- O flag é salvo no rascunho como a linha `bloqueada <id>`. Ligar e desligar não entra no desfazer.
- **Ao gravar o traço:** a pintura passa por um `saveLayer` com `SRC_ATOP`. A camada não cresce para caber o traço.
- **Prévia ao vivo:** a camada é isolada (bitmap) e a tinta em andamento entra com `SRC_ATOP`.
- **Camada de traços bloqueada:** vira pixels no primeiro traço. A promoção leva o flag e o desfazer o devolve.
- **Balde numa camada bloqueada:** também usa `SRC_ATOP`.
- **Borracha:** continua apagando normalmente.
