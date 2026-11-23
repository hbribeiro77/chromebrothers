package com.chromebrothers.navegador.regras

import java.net.URI
import java.util.Base64

/** Tipos de falha ao carregar uma página, já agrupados como a pessoa entende. */
enum class TipoDeErroDeCarregamento {
    SEM_INTERNET,
    ENDERECO_NAO_ENCONTRADO,
    CONEXAO_RECUSADA,
    TEMPO_ESGOTADO,
    CONEXAO_INTERROMPIDA,
    CERTIFICADO_INVALIDO,
    CONEXAO_INSEGURA,
    SO_HTTPS,
    ENDERECO_INVALIDO,
    PROTOCOLO_DESCONHECIDO,
    ARQUIVO_NAO_ENCONTRADO,
    REDIRECIONAMENTO_EM_LOOP,
    CONTEUDO_COM_DEFEITO,
    SITE_PERIGOSO,
    PROXY,
    PORTA_BLOQUEADA,
    DESCONHECIDO,
}

/**
 * Página mostrada quando o Gecko não consegue carregar um endereço (sem ela a aba ficava em branco,
 * ou na página anterior com o endereço novo na barra). Título, explicação, dica e "Tentar de novo".
 */
object PaginaDeErroDeCarregamentoDoNavegador {
    data class Texto(val titulo: String, val explicacao: String, val dica: String)

    fun texto(tipo: TipoDeErroDeCarregamento, host: String): Texto {
        val site = host.ifBlank { "o site" }
        return when (tipo) {
            TipoDeErroDeCarregamento.SEM_INTERNET -> Texto(
                "Sem conexão com a internet",
                "O aparelho está sem internet no momento.",
                "Confira o Wi-Fi ou os dados móveis e tente de novo.",
            )
            TipoDeErroDeCarregamento.ENDERECO_NAO_ENCONTRADO -> Texto(
                "Site não encontrado",
                "Não foi possível encontrar o endereço $site.",
                "Confira se o endereço está escrito certo. Se estiver, pode ser um problema na sua conexão.",
            )
            TipoDeErroDeCarregamento.CONEXAO_RECUSADA -> Texto(
                "Conexão recusada",
                "$site recusou a conexão.",
                "O site pode estar fora do ar. Tente de novo daqui a pouco.",
            )
            TipoDeErroDeCarregamento.TEMPO_ESGOTADO -> Texto(
                "O site demorou demais para responder",
                "$site não respondeu a tempo.",
                "O site pode estar sobrecarregado ou a conexão está lenta. Tente de novo.",
            )
            TipoDeErroDeCarregamento.CONEXAO_INTERROMPIDA -> Texto(
                "A conexão caiu",
                "A conexão com $site foi interrompida no meio do carregamento.",
                "Tente de novo.",
            )
            TipoDeErroDeCarregamento.CERTIFICADO_INVALIDO -> Texto(
                "Conexão não é segura",
                "O certificado de segurança de $site não é válido (vencido, de outro site ou não confiável).",
                "Alguém pode estar tentando se passar pelo site. Evite continuar, principalmente se for digitar senha ou dados.",
            )
            TipoDeErroDeCarregamento.CONEXAO_INSEGURA -> Texto(
                "Não foi possível conectar com segurança",
                "Houve um erro na conexão segura com $site.",
                "Pode ser um problema no site ou na rede. Tente de novo mais tarde.",
            )
            TipoDeErroDeCarregamento.SO_HTTPS -> Texto(
                "O site não oferece conexão segura",
                "$site não está disponível com HTTPS.",
                "A página foi bloqueada para proteger seus dados.",
            )
            TipoDeErroDeCarregamento.ENDERECO_INVALIDO -> Texto(
                "Endereço inválido",
                "Esse endereço não está num formato válido.",
                "Confira o que foi digitado na barra.",
            )
            TipoDeErroDeCarregamento.PROTOCOLO_DESCONHECIDO -> Texto(
                "Endereço não suportado",
                "O navegador não sabe abrir esse tipo de endereço.",
                "Talvez ele precise de outro aplicativo para abrir.",
            )
            TipoDeErroDeCarregamento.ARQUIVO_NAO_ENCONTRADO -> Texto(
                "Arquivo não encontrado",
                "O arquivo pedido não existe ou não pode ser lido.",
                "Confira o endereço do arquivo.",
            )
            TipoDeErroDeCarregamento.REDIRECIONAMENTO_EM_LOOP -> Texto(
                "A página não carrega corretamente",
                "$site está redirecionando sem parar.",
                "Isso costuma ser problema do próprio site, ou de cookies. Tente de novo mais tarde.",
            )
            TipoDeErroDeCarregamento.CONTEUDO_COM_DEFEITO -> Texto(
                "A página veio com defeito",
                "O conteúdo recebido de $site não pôde ser exibido.",
                "Tente de novo. Se continuar, o problema é do site.",
            )
            TipoDeErroDeCarregamento.SITE_PERIGOSO -> Texto(
                "Site perigoso bloqueado",
                "$site foi identificado como perigoso (golpe, vírus ou programa indesejado).",
                "Recomendamos não continuar.",
            )
            TipoDeErroDeCarregamento.PROXY -> Texto(
                "Problema no servidor proxy",
                "Não foi possível conectar ao servidor proxy configurado.",
                "Confira as configurações de proxy ou de VPN do aparelho.",
            )
            TipoDeErroDeCarregamento.PORTA_BLOQUEADA -> Texto(
                "Endereço bloqueado",
                "Esse endereço usa uma porta que o navegador bloqueia por segurança.",
                "Confira o endereço.",
            )
            TipoDeErroDeCarregamento.DESCONHECIDO -> Texto(
                "Não foi possível abrir a página",
                "Ocorreu um erro ao carregar $site.",
                "Tente de novo.",
            )
        }
    }

    /** Página "data:" pronta para o Gecko exibir no lugar da que falhou. */
    fun enderecoDaPagina(tipo: TipoDeErroDeCarregamento, endereco: String?, escura: Boolean): String {
        val html = html(tipo, endereco, escura)
        return "data:text/html;charset=utf-8;base64," + Base64.getEncoder().encodeToString(html.toByteArray(Charsets.UTF_8))
    }

    fun html(tipo: TipoDeErroDeCarregamento, endereco: String?, escura: Boolean): String {
        val host = hostDe(endereco)
        val t = texto(tipo, host)
        val perigoso = tipo == TipoDeErroDeCarregamento.SITE_PERIGOSO || tipo == TipoDeErroDeCarregamento.CERTIFICADO_INVALIDO
        val fundo = if (escura) "#202124" else "#ffffff"
        val corTexto = if (escura) "#e8eaed" else "#202124"
        val corSecundaria = if (escura) "#9aa0a6" else "#5f6368"
        val destaque = if (perigoso) "#d93025" else "#1a73e8"
        // Só http(s) vira link de "Tentar de novo"; o resto (javascript:, data:...) não.
        val alvo = endereco?.trim().orEmpty().takeIf { it.startsWith("http://") || it.startsWith("https://") }
        val botao = if (alvo != null && !perigoso) {
            "<a class=\"botao\" href=\"${escapar(alvo)}\">Tentar de novo</a>"
        } else {
            "<a class=\"botao\" href=\"javascript:history.back()\">Voltar</a>"
        }
        val enderecoVisivel = endereco?.trim().orEmpty().take(300)
        return """<!DOCTYPE html>
<html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>${escapar(t.titulo)}</title>
<style>
html,body{margin:0;height:100%;background:$fundo;color:$corTexto;font-family:Roboto,sans-serif}
.miolo{max-width:560px;margin:0 auto;padding:12vh 24px 32px}
.icone{width:56px;height:56px;border-radius:50%;background:$destaque;color:#fff;font-size:32px;line-height:56px;text-align:center;font-weight:700}
h1{font-size:26px;font-weight:500;margin:24px 0 12px}
p{font-size:16px;line-height:1.5;margin:0 0 12px}
.dica{color:$corSecundaria}
.endereco{color:$corSecundaria;font-size:13px;word-break:break-all;margin-top:20px}
.botao{display:inline-block;margin-top:28px;padding:10px 24px;border-radius:20px;background:$destaque;color:#fff;text-decoration:none;font-size:15px}
</style></head>
<body><main class="miolo">
<div class="icone">!</div>
<h1>${escapar(t.titulo)}</h1>
<p>${escapar(t.explicacao)}</p>
<p class="dica">${escapar(t.dica)}</p>
$botao
<p class="endereco">${escapar(enderecoVisivel)}</p>
</main></body></html>"""
    }

    fun hostDe(endereco: String?): String {
        if (endereco.isNullOrBlank()) return ""
        return runCatching { URI(endereco.trim()).host }.getOrNull().orEmpty().removePrefix("www.")
    }

    private fun escapar(texto: String): String = texto
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;")
}
