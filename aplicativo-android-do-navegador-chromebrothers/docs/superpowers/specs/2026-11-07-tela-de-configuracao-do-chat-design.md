# Tela de configuração do chat — Design

**Status:** aprovado (opção B, conversa 2026-11-07)

## Problema

O ⚙️ abre um bloco vertical no meio da gaveta com chave, Higgsfield, Topview, Sjinn, notas e sites — difícil de ler e empurra o chat.

## Solução

- **Overlay próprio** dentro da gaveta: cobre mensagens + compositor; cabeçalho vira “Configuração” com voltar.
- **Seções colapsáveis** com linha-resumo (título + estado); detalhe só ao expandir.
- Seções: Conta (OpenRouter) · Integrações (Higgsfield / Topview / Sjinn) · Sites (notas / navegação liberada).
- **Modelo** permanece no compositor do chat (fora desta tela).
- Sem chave ao abrir o chat → abre a config (como hoje o painel da chave).
- IDs dos controles existentes preservados onde possível (menos risco no Kotlin).

## Fora de escopo

- Redesign visual completo do chat; PreferenceFragment; commit automático.
