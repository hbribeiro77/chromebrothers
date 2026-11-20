package com.chromebrothers.navegador.regras

enum class FerramentaDaBarraDoEditorDeImagem { RECORTE, TRACO, TEXTO }

enum class LinhaSecundariaDoEditorDeImagem { PROPORCAO, SELECAO, ESPESSURA, COR }

enum class FerramentaEmpilhadaDoEditorDeImagem { RECORTE, VERMELHO, BRANCO, BORRACHA, TEXTO }

object FaixaLateralDoEditorDeImagem {
    fun indice(ferramenta: FerramentaEmpilhadaDoEditorDeImagem): Int {
        return when (ferramenta) {
            FerramentaEmpilhadaDoEditorDeImagem.RECORTE -> 0
            FerramentaEmpilhadaDoEditorDeImagem.VERMELHO -> 1
            FerramentaEmpilhadaDoEditorDeImagem.BRANCO -> 1
            FerramentaEmpilhadaDoEditorDeImagem.BORRACHA -> 2
            FerramentaEmpilhadaDoEditorDeImagem.TEXTO -> 3
        }
    }
}

object MenuDoPontoDePintura {
    fun ficaAberto(jaNaPintura: Boolean, aberto: Boolean): Boolean = !(jaNaPintura && aberto)
}

object MenuDaFerramentaDaFaixaLateral {
    fun ficaAberto(jaNesseIcone: Boolean, aberto: Boolean): Boolean = !(jaNesseIcone && aberto)
}

object SelecaoObrigatoriaDaCamada {
    /** Mantém a seleção só se a peça ainda existe; não escolhe outra automaticamente. */
    fun manter(selecionada: Int, idsNaOrdem: List<Int>): Int {
        if (selecionada in idsNaOrdem) return selecionada
        return -1
    }
}

object ToqueParaSelecionarPecaNoEditor {
    fun podeSelecionarPeloToque(modoImagemArmado: Boolean): Boolean = modoImagemArmado

    fun podeMoverPecaTocada(pecaTocadaId: Int?, pecaSelecionadaId: Int): Boolean {
        return pecaTocadaId != null && pecaTocadaId == pecaSelecionadaId && pecaSelecionadaId != -1
    }
}

object CamadaAtivaDaEdicaoDeImagem {
    fun idAoAbrirComImagem(idDaImagem: Int): Int = idDaImagem
}

object AbrirComposicaoVaziaDaEdicao {
    /** Nova composição já nasce com uma Folha ativa para o primeiro traço. */
    fun criaCamadaInicialSelecionada(): Boolean = true
}

object PecaSelecionadaDaEdicaoDeImagem {
    /** Abrir edição não marca peça no quadro (alças/mover). */
    fun idAoAbrir(): Int = -1
}

object ToqueNaListaDeCamadasDaEdicao {
    /** Lista escolhe só a camada ativa de trabalho; não seleciona a peça no quadro. */
    fun definePecaSelecionadaNoQuadro(): Boolean = false
}

object ToqueComDedoSobreLacoFechado {
    /** Com laço fechado, o dedo não move a peça. */
    fun podeMoverPeca(lacoFechado: Boolean): Boolean = !lacoFechado

    /** Por enquanto o dedo também não move a seleção do laço. */
    fun podeMoverSelecao(lacoFechado: Boolean): Boolean = false
}

object ImagemAlvoDoRecorteDaComposicao {
    fun idExplicito(idDaImagemDoRecorte: Int, idsDeImagem: List<Int>): Int? {
        if (idDaImagemDoRecorte in idsDeImagem) return idDaImagemDoRecorte
        return null
    }
}

object FonteDoDestacarDaCamadaAtiva {
    /** Destacar/recortar usam a camada ativa: imagem colada ou folha com tinta. */
    fun aceita(ehImagem: Boolean, ehFolha: Boolean): Boolean = ehImagem || ehFolha

    fun precisaPromoverFolha(ehFolha: Boolean): Boolean = ehFolha
}

object CoresNoMenuDaFerramenta {
    fun aparecem(ehBorracha: Boolean): Boolean = !ehBorracha
}

object ToqueNaReguaDoEditor {
    fun temPrioridade(comecouNaRegua: Boolean, jaSegurandoARegua: Boolean): Boolean {
        return comecouNaRegua || jaSegurandoARegua
    }
}

object BarraContextualDoEditorDeImagem {
    fun linha(ferramenta: FerramentaDaBarraDoEditorDeImagem): LinhaSecundariaDoEditorDeImagem {
        return when (ferramenta) {
            FerramentaDaBarraDoEditorDeImagem.RECORTE -> LinhaSecundariaDoEditorDeImagem.SELECAO
            FerramentaDaBarraDoEditorDeImagem.TRACO -> LinhaSecundariaDoEditorDeImagem.ESPESSURA
            FerramentaDaBarraDoEditorDeImagem.TEXTO -> LinhaSecundariaDoEditorDeImagem.COR
        }
    }
}

enum class ToqueNaImagemDaGaveta { ABRIR_NO_VISOR, TROCAR_A_IMAGEM_DO_VISOR, INCLUIR_NA_COMPOSICAO }

object DestinoDoToqueNaImagemDaGaveta {
    fun decidir(editando: Boolean, gavetaSobreAImagem: Boolean): ToqueNaImagemDaGaveta {
        return when {
            editando && gavetaSobreAImagem -> ToqueNaImagemDaGaveta.INCLUIR_NA_COMPOSICAO
            gavetaSobreAImagem -> ToqueNaImagemDaGaveta.TROCAR_A_IMAGEM_DO_VISOR
            else -> ToqueNaImagemDaGaveta.ABRIR_NO_VISOR
        }
    }
}
