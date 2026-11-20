# Seleção ativa do retângulo com barra flutuante

Data: 2026-10-31  
Status: implementado (2026-10-31)  
Espelha: `2026-10-31-selecao-ativa-do-laco-com-barra-flutuante-design.md`

## Problema

O retângulo ainda usa Destacar/Recortar no canto, sem Copiar/Mover/Excluir/Inverter explícitos nem barra perto da seleção. O laço já tem esse padrão.

## Objetivo

Com ferramenta Recorte + forma retângulo e um retângulo válido (`cabe`), a seleção é “ativa”: alças existentes + barra flutuante **Copiar · Mover · Excluir · Inverter · Recortar**, mesma semântica do laço.

## Fora de escopo

- Mudar seleção inteligente além de conviver com a barra.
- Girar o retângulo (mantém só alças de redimensionar/mover atuais).

## Semântica

| Ação | Efeito |
|------|--------|
| Copiar | Nova camada no lugar; origem intacta. Posição alinhada aos pixels. |
| Mover | Buraco + nova camada (antigo Destacar parcial). |
| Excluir | Só buraco; sem peça nova. |
| Inverter | Complemento da máscara retangular (dentro da imagem/moldura alvo); seleção permanece. |
| Recortar | Modal → moldura da composição vira a seleção (como no laço). |

Botões Destacar/Recortar do canto ficam ocultos enquanto a seleção retangular estiver ativa (mesmo gancho do laço).

## Toque

Chips têm prioridade sobre alças; após chip, `IGNORAR` até soltar (não inicia arraste/resize).

## Critérios de sucesso

1. Retângulo válido → barra com as cinco ações.
2. Copiar/Mover/Excluir/Inverter/Recortar espelham o laço.
3. Voltar/toque que cancela seleção (se houver cancelamento) não aplica efeito.
4. Destacar/Recortar do canto ocultos com seleção ativa.
