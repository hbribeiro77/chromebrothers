# Design: importar pincéis do Procreate

Data: 2026-11-14
Status: implementado; escala de alguns valores a calibrar com arquivos reais

## Formato

- `.brush`: ZIP com `Brush.archive` (plist binário `bplist00`, NSKeyedArchiver, objeto `SilicaBrush`), `Shape.png`, `Grain.png` e `QuickLook/`.
- `.brushset`: uma pasta por pincel (UUID) e `brushset.plist` com a ordem (`brushes`).
- Ignorados: `Reset/`, `QuickLook/`, `Signature/`, `Sub*/` (pincel secundário do pincel duplo).
- Formato não público: os nomes das chaves vêm de engenharia reversa da comunidade.

## Peças

| Parte | Onde |
|---|---|
| Leitor de `bplist00` e do NSKeyedArchiver | `regras/PlistBinarioDaApple` (com teste e gravador mínimo no teste) |
| `SilicaBrush` → `PincelDoMotor`, pastas e ordem | `regras/PincelDoProcreate` |
| ZIP, imagens (reduzidas ao decodificar, máscara 256×256), catálogo | `PacoteDePinceisNoAparelho.importar` (mesmo botão do `.chromebrush`) |

## Mapeamento

| Procreate | Nosso | Observação |
|---|---|---|
| `plotSpacing` | espaçamento = √v × 1,5 | guardado ao quadrado (5% → 0,0025) |
| `plotJitter` | dispersão | |
| `plotSmoothing` | StreamLine | |
| `shapeScatter` | rotação aleatória | |
| `shapeRotation` | seguir o traço (\|v\| ≥ 0,5) | |
| `shapeRoundness`, `shapeInverted` | arredondamento, inverter a forma | |
| `textureScale`, `textureDepth`, `textureInverted`, `textureMovement` | escala, intensidade, inverter, fixo/acompanha | escala 0..1 → 0,25×..4× |
| `dynamicsPressureSize/Opacity` | pressão → tamanho/opacidade | |
| `dynamicsJitterSize/Opacity` | variação de tamanho/opacidade | |
| `dynamicsSpeedSize/Opacity` | velocidade → tamanho/opacidade | módulo |
| `maxSize`, `minSize`, `maxOpacity`, `minOpacity` | limites | |
| `taperStartLength`/`taperEndLength` (ou `pencilTaper…`) | afinamento | |
| sem `Shape.png` (`bundledShapePath`) | forma redonda | as formas internas do Procreate não vêm no arquivo |
| sem `Grain.png` com `bundledGrainPath` | granulado de papel | idem |

Sem equivalente (entra sem, o aviso diz): mistura molhada, modo de mesclagem, pincel duplo, variação de cor.

## Calibração

Cada importação grava `files/pinceis/ultima-importacao-procreate.txt` com todas as chaves e valores (as que não usamos marcadas). Com um pincel real conhecido: `adb shell run-as <pacote> cat files/pinceis/ultima-importacao-procreate.txt`, comparar e ajustar as escalas em `PincelDoProcreate`.
