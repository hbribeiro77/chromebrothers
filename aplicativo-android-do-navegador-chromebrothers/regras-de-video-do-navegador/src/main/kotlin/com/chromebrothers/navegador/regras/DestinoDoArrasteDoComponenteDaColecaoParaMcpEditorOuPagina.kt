package com.chromebrothers.navegador.regras

/**
 * Destino do fantasma do Visualizador / coleção (além do fundir no editor):
 * gerador MCP, cola na página, salva no Visualizador (imagem vinda do site) ou cancela.
 */
enum class DestinoDoSoltarDoArrasteDoComponenteDaColecao {
    REFERENCIA_MCP,
    SALVAR_NO_VISUALIZADOR,
    /** Mídia gerada (guardada dentro do app) solta sobre o Visualizador: move para a pasta pública. */
    MOVER_PARA_O_VISUALIZADOR,
    FUNDIR_EDITOR,
    COLAR_PAGINA,
    CANCELAR,
}

object DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina {
    fun decidir(
        tipoGeradorMcp: String,
        soltouSobreLinhaDeCamada: Boolean,
        soltouSobreMoldura: Boolean,
        soltouSobreJanelaDaColecaoNaoMcp: Boolean,
        editavelProximoNaPagina: Boolean,
        /** Mantido por compat; colar na página exige verde (editável), não basta o ponto na página. */
        pontoSobrePagina: Boolean = false,
        /** A imagem veio de um site (não do próprio Visualizador). */
        veioDaPagina: Boolean = false,
        soltouSobreVisualizador: Boolean = false,
        /** A mídia arrastada é gerada e ainda está dentro do app (pode ir para a pasta pública). */
        midiaGeradaPodeMover: Boolean = false,
    ): DestinoDoSoltarDoArrasteDoComponenteDaColecao {
        if (tipoGeradorMcp.isNotBlank()) {
            return DestinoDoSoltarDoArrasteDoComponenteDaColecao.REFERENCIA_MCP
        }
        if (deveSalvarNoVisualizador(veioDaPagina, soltouSobreVisualizador)) {
            return DestinoDoSoltarDoArrasteDoComponenteDaColecao.SALVAR_NO_VISUALIZADOR
        }
        if (deveMoverParaOVisualizador(veioDaPagina, soltouSobreVisualizador, midiaGeradaPodeMover)) {
            return DestinoDoSoltarDoArrasteDoComponenteDaColecao.MOVER_PARA_O_VISUALIZADOR
        }
        if (!soltouSobreJanelaDaColecaoNaoMcp && (soltouSobreLinhaDeCamada || soltouSobreMoldura)) {
            return DestinoDoSoltarDoArrasteDoComponenteDaColecao.FUNDIR_EDITOR
        }
        if (editavelProximoNaPagina) {
            return DestinoDoSoltarDoArrasteDoComponenteDaColecao.COLAR_PAGINA
        }
        return DestinoDoSoltarDoArrasteDoComponenteDaColecao.CANCELAR
    }

    /**
     * O Visualizador fica "transparente" ao hit-test durante o arraste para o soltar cair na página
     * por baixo dele. Com o editor de imagem aberto a página nem aparece: aí o Visualizador continua
     * sólido e vira a zona de cancelar (+ amarelo; soltar ali não envia para o editor).
     */
    fun deveLiberarAPaginaSobOVisualizador(editorDeImagemAberto: Boolean): Boolean = !editorDeImagemAberto

    /** Ponto sobre o Visualizador (janela da coleção que não é gerador MCP) com o editor aberto: cancela. */
    fun estaNaZonaDeCancelar(
        editorDeImagemAberto: Boolean,
        tipoGeradorMcp: String,
        sobreJanelaDaColecao: Boolean,
    ): Boolean = editorDeImagemAberto && tipoGeradorMcp.isBlank() && sobreJanelaDaColecao

    /** Imagem do site solta sobre o Visualizador: grava em Pictures/ChromeBrothers e entra na grade. */
    fun deveSalvarNoVisualizador(veioDaPagina: Boolean, soltouSobreVisualizador: Boolean): Boolean =
        veioDaPagina && soltouSobreVisualizador

    /** Mídia gerada (ex.: imagem do MCP) solta sobre o Visualizador: move em vez de copiar. */
    fun deveMoverParaOVisualizador(
        veioDaPagina: Boolean,
        soltouSobreVisualizador: Boolean,
        midiaGeradaPodeMover: Boolean,
    ): Boolean = !veioDaPagina && soltouSobreVisualizador && midiaGeradaPodeMover

    fun corIndicaPodeSoltar(
        tipoGeradorMcp: String,
        sobreEditor: Boolean,
        editavelProximoNaPagina: Boolean,
        salvarNoVisualizador: Boolean = false,
    ): Boolean = tipoGeradorMcp.isNotBlank() || sobreEditor || editavelProximoNaPagina || salvarNoVisualizador
}
