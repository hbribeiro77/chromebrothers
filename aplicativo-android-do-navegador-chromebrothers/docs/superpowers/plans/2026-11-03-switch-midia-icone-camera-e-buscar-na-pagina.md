# Plano: switch mídia, câmera, buscar na página

**Goal:** Overflow com capturar mídia; ícone câmera na captura; find-in-page Chrome via SessionFinder.

## Arquivos

- `DisposicaoDaBarraDoNavegadorPorLarguraDaTela.kt` + teste — `CAPTURA_MIDIA` só no overflow (como bloqueio); novo `BUSCAR_NA_PAGINA` na barra ampla / overflow compacto
- `icone_de_captura_nativa_...xml` — redesenhar como câmera (ou novo drawable e apontar refs)
- Layout principal — botão lupa + faixa de busca
- `SuperficieDaPaginaNoMotorDoFirefox.kt` — encapsular SessionFinder
- `AtividadePrincipal...` — ligar UI, overflow, dispose barra
- Strings

## Tarefas

1. TDD disposição: CAPTURA_MIDIA não visível na barra ampla; vai ao overflow ampla+compacta; BUSCAR_NA_PAGINA visível ampla, overflow compacta
2. Implementar regra
3. Ícone câmera
4. UI busca + SessionFinder + wire overflow/barra
5. assembleDebug + adb install
