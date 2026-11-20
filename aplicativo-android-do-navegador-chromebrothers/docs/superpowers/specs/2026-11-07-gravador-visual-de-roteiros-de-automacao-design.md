# Gravador visual de roteiros de automação — Design

**Status:** aprovado em conversa (2026-11-07)  
**Depende de:** roteiros v1 (`RoteiroDeAutomacaoDaPagina`, executor, store `roteiros-de-automacao`)

## Objetivo

Permitir que o usuário monte um roteiro determinístico **sem depender do chat**: aponta elementos na página, escolhe como identificá-los, testa com Play e salva a mesma receita JSON que o motor já executa.

## Decisões fechadas

| Tema | Escolha |
|------|---------|
| Pick de elemento | Overlay na página (principal) + atalho “usar vínculo do site” quando couber |
| Destino da receita | Salvar direto + Play na UI; chat/JSON como secundário |
| Forma da UI | Faixa compacta sobre a página (não gaveta, não janela tipo coleção) |
| Abordagem v1 | Gravador guiado por passos (não espionar todos os toques; não montar via IA) |

## Arquitetura

```
[Faixa PainelDoGravadorDeRoteiro]
        │ adiciona/edita passos
        ▼
RoteiroDeAutomacaoDaPagina (memória)
        ├─ Salvar → GravacaoDosRoteiros… (files/roteiros-de-automacao)
        └─ Play   → ExecutorDosRoteiros… (já existente)
        ▲
[Modo pick JS na WebView da aba]
        │ candidatas
        ▼
RanqueamentoDeCandidatasDoElemento (Kotlin) → usuário escolhe → passo
```

- Entrada: botão “gravar” perto do ▶ dos roteiros no chat (e/ou long-press no ▶).
- Uma sessão de gravação por vez; abrir o gravador cancela pick anterior.
- Play cancela execução anterior (mesmo padrão do executor atual).

## Fluxo da UI

1. Abrir faixa → nome do roteiro + URL inicial (pré-preenche aba atual).
   - Confirmar URL: grava passo `abrir` e navega **ou** opção “já estou na página” (só registra a URL / não navega de novo — detalhe de implementação: se “já estou”, pode omitir `abrir` ou incluir `abrir` da URL atual sem forçar reload; preferência: incluir `abrir` com a URL atual e **não** recarregar se a aba já estiver nela).
2. Chips: Aguardar | Preencher | Clicar | Clicar se | Esperar ms | Rolar.
3. Passos com pick:
   - entram em modo pick → highlight → toque → lista de candidatas → escolha → completa o passo (valor/timeout/`contem`/`se_nao` conforme o tipo).
4. Lista compacta de passos; item → editar / remover / subir / descer.
5. Play: executa a receita em memória (mesmo sem salvar).
6. Salvar: persiste no store; toast com id.
7. Menu ⋯: copiar JSON / enviar texto ao chat / fechar.

### Clicar se

- Pick foca o **conjunto** (ex. links de resultado): usuário escolhe seletor da lista + índice + `contem` + `se_nao` (`parar` \| `seguir`).
- Reusa o passo `clicar_se` já existente no schema.

### Atalho de vínculo

- Se o site tiver campo/envio vinculado e o passo for preencher ou clicar no envio, oferecer “usar vínculo do site” sem pick.

## Candidatas de identificação

Script na aba devolve marcas do elemento tocado; Kotlin ranqueia e mostra 3–6 opções, tipicamente:

1. `#id` (se estável)
2. `[name="…"]`
3. `[aria-label="…"]` / placeholder
4. texto visível curto (para `aguardar`/`clicar` por texto)
5. seletor por tag + `nth-of-type` / índice na lista (`querySelectorAll`)

A escolha do usuário vira `seletor` e/ou `texto`/`contem` no passo, no schema **atual** (sem ações novas na v1).

## Erros

| Situação | Comportamento |
|----------|----------------|
| Sem página / pick sem resposta | Toast; volta à faixa |
| Play falha no passo N | Toast com relato do executor; destaca o passo |
| Salvar sem nome ou sem passos | Recusa na UI |

## Fora de escopo (v1)

- Gravação automática de todos os toques/scrolls
- Gerar sozinho o botão `chromebrothers.rodarRoteiro` no componente da coleção
- Multi-aba no mesmo roteiro
- Branching além do `clicar_se` existente
- Editor visual de JSON livre como caminho principal

## Testes

- Unit: ranqueamento de candidatas; montagem de cada tipo de passo a partir de uma escolha.
- Manual: gravar fluxo curto (abrir → aguardar → preencher → clicar → clicar_se) → Play → Salvar → rodar de novo pelo ▶ do chat.

## Arquivos previstos (orientação)

- `PainelDoGravadorDeRoteiroDeAutomacaoDoNavegador.kt` (+ layout/strings)
- Script/regras de pick + `RanqueamentoDeCandidatasDoElementoParaORoteiro.kt`
- Wire em `AtividadePrincipal…` e atalho no `PainelDoChat…`
- Sem mudar o schema do roteiro, salvo bugs descobertos no caminho
