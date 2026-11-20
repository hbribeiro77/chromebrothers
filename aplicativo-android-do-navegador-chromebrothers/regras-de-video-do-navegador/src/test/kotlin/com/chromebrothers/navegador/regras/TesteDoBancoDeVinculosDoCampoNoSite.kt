package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoBancoDeVinculosDoCampoNoSite {
    @Test
    fun guardaCampoEEnvioPeloHospedeiroERemoveSoOQueFoiPedido() {
        val host = BancoDeVinculosDoCampoNoSite.hospedeiro("https://Chat.Exemplo.com/c/1?x=2")
        assertEquals("chat.exemplo.com", host)
        val campo = MarcaDoElementoVinculado("caixa", "mensagem", "Escreva", "TEXTAREA", "")
        val envio = MarcaDoElementoVinculado("", "", "Enviar", "BUTTON", "button")
        val comCampo = BancoDeVinculosDoCampoNoSite.guardarCampo(emptyList(), host, campo)
        val completo = BancoDeVinculosDoCampoNoSite.guardarEnvio(comCampo, host, envio)
        assertEquals(campo, BancoDeVinculosDoCampoNoSite.doSite(completo, host)?.campo)
        assertEquals(envio, BancoDeVinculosDoCampoNoSite.doSite(completo, host)?.envio)
        val semCampo = BancoDeVinculosDoCampoNoSite.removerCampo(completo, host)
        assertNull(BancoDeVinculosDoCampoNoSite.doSite(semCampo, host)?.campo)
        assertEquals(envio, BancoDeVinculosDoCampoNoSite.doSite(semCampo, host)?.envio)
        assertEquals(completo, BancoDeVinculosDoCampoNoSite.ler(BancoDeVinculosDoCampoNoSite.gravar(completo)))
        assertEquals(emptyList(), BancoDeVinculosDoCampoNoSite.removerEnvio(semCampo, host))
    }

    @Test
    fun lembraOUltimoEnderecoDoSiteSemPerderOVinculoAntigo() {
        val campo = MarcaDoElementoVinculado("caixa", "mensagem", "Escreva", "TEXTAREA", "")
        val host = "chat.exemplo.com"
        val comCampo = BancoDeVinculosDoCampoNoSite.guardarCampo(emptyList(), host, campo)
        val antigo = BancoDeVinculosDoCampoNoSite.gravar(comCampo)
        assertEquals("", BancoDeVinculosDoCampoNoSite.ler(antigo).single().endereco)
        val linhaAntiga = "chat.exemplo.com\tcaixa\tmensagem\tEscreva\tTEXTAREA\t\t\t\t\t\t"
        assertEquals("caixa", BancoDeVinculosDoCampoNoSite.ler(linhaAntiga).single().campo?.id)
        assertEquals("", BancoDeVinculosDoCampoNoSite.ler(linhaAntiga).single().endereco)
        val comEndereco = BancoDeVinculosDoCampoNoSite.guardarEndereco(comCampo, host, "https://chat.exemplo.com/c/1")
        assertEquals("https://chat.exemplo.com/c/1", BancoDeVinculosDoCampoNoSite.doSite(comEndereco, host)?.endereco)
        assertEquals(comEndereco, BancoDeVinculosDoCampoNoSite.ler(BancoDeVinculosDoCampoNoSite.gravar(comEndereco)))
        assertEquals(comEndereco, BancoDeVinculosDoCampoNoSite.guardarCampo(comEndereco, host, campo))
        assertEquals(1, BancoDeVinculosDoCampoNoSite.guardarEndereco(comEndereco, "outro.com", "https://outro.com/").size)
        val dois = BancoDeVinculosDoCampoNoSite.guardarCampo(comEndereco, "outro.com", campo)
        val atualizado = BancoDeVinculosDoCampoNoSite.guardarEndereco(dois, host, "https://chat.exemplo.com/c/2")
        assertEquals(listOf("outro.com", host), atualizado.map { it.hospedeiro })
        assertEquals("https://chat.exemplo.com/c/2", atualizado.first { it.hospedeiro == host }.endereco)
        assertEquals("https://chat.exemplo.com/c/2", BancoDeVinculosDoCampoNoSite.enderecoParaAbrir(atualizado.first { it.hospedeiro == host }))
        assertEquals("https://$host/", BancoDeVinculosDoCampoNoSite.enderecoParaAbrir(comCampo.single()))
        val suspeito = comCampo.single().copy(endereco = "javascript:alert(1)")
        assertEquals("https://$host/", BancoDeVinculosDoCampoNoSite.enderecoParaAbrir(suspeito))
    }
}
