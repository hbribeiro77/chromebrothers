package com.chromebrothers.navegador.regras

object PersistenciaDasMensagensDoChatDaOpenRouter {
    fun escrever(mensagens: List<MensagemDeChat>): String {
        val itens = mensagens.joinToString(",") { jsonDaMensagem(it) }
        return """{"mensagens":[$itens]}"""
    }

    fun ler(bruto: String): List<MensagemDeChat> {
        val objetos = TextoJsonDoChat.objetosDaChave(bruto, "mensagens") ?: return emptyList()
        return objetos.map { lerMensagem(it) }
    }

    private fun jsonDaMensagem(mensagem: MensagemDeChat): String {
        val papel = TextoJsonDoChat.escapar(mensagem.papel)
        val conteudo = TextoJsonDoChat.escapar(mensagem.conteudo)
        val idDaChamada = TextoJsonDoChat.escapar(mensagem.idDaChamada)
        val partes = mensagem.partes.joinToString(",") { jsonDaParte(it) }
        val chamadas = mensagem.chamadas.joinToString(",") { jsonDaChamada(it) }
        return """{"papel":"$papel","conteudo":"$conteudo","idDaChamada":"$idDaChamada","partes":[$partes],"chamadas":[$chamadas]}"""
    }

    private fun jsonDaParte(parte: ParteDeConteudoDoChat): String {
        val tipo = TextoJsonDoChat.escapar(parte.tipo)
        val texto = TextoJsonDoChat.escapar(parte.texto)
        val mime = TextoJsonDoChat.escapar(parte.mime)
        val nome = TextoJsonDoChat.escapar(parte.nome)
        val dados = TextoJsonDoChat.escapar(parte.dados)
        return """{"tipo":"$tipo","texto":"$texto","mime":"$mime","nome":"$nome","dados":"$dados"}"""
    }

    private fun jsonDaChamada(chamada: ChamadaDeFerramenta): String {
        val id = TextoJsonDoChat.escapar(chamada.id)
        val nome = TextoJsonDoChat.escapar(chamada.nome)
        val argumentos = TextoJsonDoChat.escapar(chamada.argumentos)
        return """{"id":"$id","nome":"$nome","argumentos":"$argumentos"}"""
    }

    private fun lerMensagem(objeto: String): MensagemDeChat {
        return MensagemDeChat(
            papel = TextoJsonDoChat.valorDaChave(objeto, "papel") ?: "",
            conteudo = TextoJsonDoChat.valorDaChave(objeto, "conteudo") ?: "",
            partes = TextoJsonDoChat.objetosDaChave(objeto, "partes").orEmpty().map { lerParte(it) },
            chamadas = TextoJsonDoChat.objetosDaChave(objeto, "chamadas").orEmpty().map { lerChamada(it) },
            idDaChamada = TextoJsonDoChat.valorDaChave(objeto, "idDaChamada") ?: "",
        )
    }

    private fun lerParte(objeto: String): ParteDeConteudoDoChat {
        return ParteDeConteudoDoChat(
            tipo = TextoJsonDoChat.valorDaChave(objeto, "tipo") ?: "",
            texto = TextoJsonDoChat.valorDaChave(objeto, "texto") ?: "",
            mime = TextoJsonDoChat.valorDaChave(objeto, "mime") ?: "",
            nome = TextoJsonDoChat.valorDaChave(objeto, "nome") ?: "",
            dados = TextoJsonDoChat.valorDaChave(objeto, "dados") ?: "",
        )
    }

    private fun lerChamada(objeto: String): ChamadaDeFerramenta {
        return ChamadaDeFerramenta(
            id = TextoJsonDoChat.valorDaChave(objeto, "id") ?: "",
            nome = TextoJsonDoChat.valorDaChave(objeto, "nome") ?: "",
            argumentos = TextoJsonDoChat.valorDaChave(objeto, "argumentos") ?: "",
        )
    }
}
