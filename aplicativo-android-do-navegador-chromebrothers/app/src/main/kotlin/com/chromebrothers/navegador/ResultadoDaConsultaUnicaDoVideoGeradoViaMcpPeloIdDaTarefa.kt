package com.chromebrothers.navegador

/** Uma consulta só (sem esperar) do vídeo de uma tarefa já criada no Higgsfield, Sjinn ou Topview. */
sealed class ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa {
    data class Pronto(val bytes: ByteArray, val enderecoRemoto: String) :
        ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa()

    data object AindaGerando : ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa()

    data class Falhou(val mensagem: String) : ResultadoDaConsultaUnicaDoVideoGeradoViaMcpPeloIdDaTarefa()
}
