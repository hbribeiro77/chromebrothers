package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDaGeracaoDeImagemEVideoPeloHiggsfieldNoChat {
    @Test
    fun imagemExigePromptEAceitaModeloOpcional() {
        assertIs<ResultadoDaGeracaoHiggsfieldDoChat.Recusado>(
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem("""{}"""),
        )
        val semModelo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(
            """{"prompt":"um gato no espaço"}""",
        )
        assertIs<ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem>(semModelo)
        assertEquals("um gato no espaço", semModelo.prompt)
        assertEquals(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_IMAGEM, semModelo.modelo)

        val comModelo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(
            """{"prompt":"retrato","modelo":" Nano Banana 2.1 "}""",
        )
        assertIs<ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem>(comModelo)
        assertEquals("nano_banana_2_1", comModelo.modelo)

        val generico = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(
            """{"prompt":"retrato","modelo":"nano banana"}""",
        )
        assertIs<ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem>(generico)
        assertEquals("nano_banana_2_1", generico.modelo)
    }

    @Test
    fun imagemAceitaIdDaBibliotecaComoReferencia() {
        val comRef = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(
            """{"prompt":"mesmo personagem de noite","modelo":"nano_banana_2_1","id_imagem":"abc-123"}""",
        )
        assertIs<ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem>(comRef)
        assertEquals("abc-123", comRef.idImagem)
        assertEquals("image_references", comRef.roleReferencia)

        val comRole = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(
            """{"prompt":"edita","modelo":"gpt_image_2","id_imagem":"img-1","role_referencia":"image"}""",
        )
        assertIs<ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem>(comRole)
        assertEquals("image", comRole.roleReferencia)

        val idLongo = "x".repeat(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.LIMITE_DO_ID_IMAGEM + 1)
        assertEquals(
            "O id da imagem passou do limite.",
            (GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(
                """{"prompt":"ok","id_imagem":"$idLongo"}""",
            ) as ResultadoDaGeracaoHiggsfieldDoChat.Recusado).mensagem,
        )
    }

    @Test
    fun imagemAceitaIdAnexoParaReferenciaDoChat() {
        val comAnexo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(
            """{"prompt":"coloca a máscara","modelo":"nano_banana_2_1","id_imagem":"anexo"}""",
        )
        assertIs<ResultadoDaGeracaoHiggsfieldDoChat.ProntoImagem>(comAnexo)
        assertEquals("anexo", comAnexo.idImagem)
        assertTrue(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.ehReferenciaDeAnexoDoChat("anexo"))
        assertTrue(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.ehReferenciaDeAnexoDoChat("anexo_recente"))
        assertTrue(!GeracaoDeImagemEVideoPeloHiggsfieldNoChat.ehReferenciaDeAnexoDoChat("abc-123"))
    }

    @Test
    fun perguntaImagemMencionaReferenciaQuandoHouver() {
        val comRef = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.perguntaImagem(
            "gato",
            "nano_banana_2_1",
            idImagem = "abc",
            roleReferencia = "image_references",
        )
        assertTrue(comRef.contains("abc"))
        assertTrue(comRef.contains("referência") || comRef.contains("referencia") || comRef.contains("id_imagem"))
        val comAnexo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.perguntaImagem(
            "gato",
            "nano_banana_2_1",
            idImagem = "anexo",
        )
        assertTrue(comAnexo.contains("anexo do chat"))
    }

    @Test
    fun rolePadraoDependeDoModelo() {
        assertEquals(
            "image_references",
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverRoleReferencia("nano_banana_2_1", ""),
        )
        assertEquals(
            "image",
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverRoleReferencia("soul_2", ""),
        )
        assertEquals(
            "mask",
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverRoleReferencia("nano_banana_2_1", "mask"),
        )
    }

    @Test
    fun videoExigePromptEAceitaModelo() {
        val pronto = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerVideo(
            """{"prompt":"onda no mar","modelo":"Wan 3"}""",
        )
        assertIs<ResultadoDaGeracaoHiggsfieldDoChat.ProntoVideo>(pronto)
        assertEquals("onda no mar", pronto.prompt)
        assertEquals("wan3_0", pronto.modelo)
    }

    @Test
    fun wan3NaoConfundeComWan27() {
        assertEquals("wan3_0", GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloVideo("Wan 3"))
        assertEquals("wan3_0", GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloVideo("wan3_0"))
        assertEquals("wan2_7", GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloVideo("Wan 2.7"))
        assertEquals("wan3_0_prime", GeracaoDeImagemEVideoPeloHiggsfieldNoChat.resolverModeloVideo("Wan 3.0 Prime"))
        assertEquals("wan3_0", GeracaoDeImagemEVideoPeloHiggsfieldNoChat.MODELO_PADRAO_VIDEO)
    }

    @Test
    fun promptOuModeloAcimaDoLimiteSaoRecusados() {
        val longo = "x".repeat(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.LIMITE_DO_PROMPT + 1)
        assertEquals(
            "O prompt passou do limite.",
            (GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem(
                """{"prompt":"$longo"}""",
            ) as ResultadoDaGeracaoHiggsfieldDoChat.Recusado).mensagem,
        )
        val modeloLongo = "m".repeat(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.LIMITE_DO_MODELO + 1)
        assertEquals(
            "O modelo passou do limite.",
            (GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerVideo(
                """{"prompt":"ok","modelo":"$modeloLongo"}""",
            ) as ResultadoDaGeracaoHiggsfieldDoChat.Recusado).mensagem,
        )
    }

    @Test
    fun perguntaMostraTipoPromptEModelo() {
        val comModelo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.perguntaImagem("gato", "nano_banana_2_1")
        assertTrue(comModelo.contains("imagem"))
        assertTrue(comModelo.contains("gato"))
        assertTrue(comModelo.contains("nano_banana_2_1"))
        val semModelo = GeracaoDeImagemEVideoPeloHiggsfieldNoChat.perguntaVideo("onda", "")
        assertTrue(semModelo.contains("vídeo"))
        assertTrue(semModelo.contains("onda"))
        assertTrue(semModelo.contains("padrão"))
    }

    @Test
    fun mensagensDeErroEstaveis() {
        assertEquals(
            "Ligue o Higgsfield na engrenagem do chat.",
            GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO,
        )
        assertTrue(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.sucessoImagem("id-1", "gato").contains("id-1"))
        assertTrue(GeracaoDeImagemEVideoPeloHiggsfieldNoChat.sucessoVideo("vid-2", "onda").contains("vid-2"))
    }
}
