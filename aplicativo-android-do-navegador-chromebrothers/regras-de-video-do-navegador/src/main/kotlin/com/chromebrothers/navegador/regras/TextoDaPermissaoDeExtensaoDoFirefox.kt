package com.chromebrothers.navegador.regras

object TextoDaPermissaoDeExtensaoDoFirefox {
    fun frase(permissao: String): String {
        return when (permissao) {
            "<all_urls>" -> "Ler e alterar dados em todos os sites"
            "tabs" -> "Ver e alterar as abas"
            "activeTab" -> "Acessar a aba que você está usando"
            "webRequest" -> "Ver os pedidos de rede"
            "webRequestBlocking" -> "Bloquear pedidos de rede"
            "downloads" -> "Gerir downloads"
            "storage" -> "Guardar dados no aparelho"
            "unlimitedStorage" -> "Guardar dados sem limite de tamanho"
            "cookies" -> "Ler e alterar cookies"
            "scripting" -> "Inserir script nas páginas"
            "contextMenus" -> "Adicionar itens no menu"
            "notifications" -> "Mostrar notificações"
            "clipboardRead" -> "Ler a área de transferência"
            "clipboardWrite" -> "Escrever na área de transferência"
            "bookmarks" -> "Ler e alterar favoritos"
            "history" -> "Ler e alterar o histórico"
            "geolocation" -> "Saber a sua localização"
            "privacy" -> "Alterar configurações de privacidade"
            else -> if (permissao.contains("://") || permissao.contains("*")) {
                "Acessar $permissao"
            } else {
                permissao
            }
        }
    }
}
