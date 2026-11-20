# Task 5 review package


===== NEW AlertDialog check in Painel (should be pre-existing only) =====

35:import androidx.appcompat.app.AlertDialog
1286:        AlertDialog.Builder(activity)
1309:        AlertDialog.Builder(activity)
2740:        AlertDialog.Builder(activity)

===== Modal + chips symbols =====

103:    private val faixaDeChipsDeOrigem: View = activity.findViewById(R.id.faixa_chips_origem_da_biblioteca)
105:    private var origemFiltroAtual = "todos"
751:        if (origemFiltroAtual != "todos" && origemFiltroAtual !in visiveis) origemFiltroAtual = "todos"
766:        val selecionado = origemFiltroAtual == valor
791:            origemFiltroAtual = valor
816:                    origemFiltroAtual,
837:                    origemFiltroAtual,
1056:        cartao.addView(botaoDeInformacao(lado) { mostrarModalDoDetalheDoVideo(item) })
1418:        cartao.addView(botaoDeInformacao(lado) { mostrarModalDoDetalheDaImagem(item) })
2785:    private fun mostrarModalDoDetalheDoVideo(item: ItemDaBibliotecaDeVideos) {
2790:        mostrarModalDoDetalheDaMidia(
2800:            val arquivo = arquivoLocal ?: return@mostrarModalDoDetalheDaMidia false
2804:            val uri = ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.exportar(activity, arquivo, nome)
2817:    private fun mostrarModalDoDetalheDaImagem(item: ItemDaBibliotecaDeImagensCapturadas) {
2820:        mostrarModalDoDetalheDaMidia(
2858:    private fun mostrarModalDoDetalheDaMidia(
2871:        val raiz = activity.layoutInflater.inflate(R.layout.layout_modal_do_detalhe_da_midia_na_biblioteca, null)

===== LAYOUT MODAL =====

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:id="@+id/titulo_do_modal_do_detalhe_da_midia"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/titulo_do_detalhe_da_midia_na_biblioteca"
        android:textColor="#F4F7FB"
        android:textSize="20sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/origem_e_modelo_do_detalhe_da_midia"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:textColor="#8AB4F8"
        android:textSize="14sp" />

    <ScrollView
        android:id="@+id/rolagem_do_prompt_do_detalhe_da_midia"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:fadeScrollbars="false">

        <TextView
            android:id="@+id/prompt_do_detalhe_da_midia"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textColor="#C4C7CE"
            android:textIsSelectable="true"
            android:textSize="15sp" />
    </ScrollView>

    <HorizontalScrollView
        android:id="@+id/linha_refs_do_detalhe"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:scrollbars="none"
        android:visibility="gone">

        <LinearLayout
            android:id="@+id/conteudo_das_refs_do_detalhe"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal" />
    </HorizontalScrollView>

    <TextView
        android:id="@+id/caminho_do_detalhe_da_midia"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:textColor="#8A9099"
        android:textSize="13sp" />

    <HorizontalScrollView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:fillViewport="true"
        android:scrollbars="none">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:gravity="end"
            android:orientation="horizontal">

            <Button
                android:id="@+id/botao_fechar_do_detalhe_da_midia"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:background="@android:color/transparent"
                android:minWidth="0dp"
                android:minHeight="0dp"
                android:paddingStart="12dp"
                android:paddingEnd="12dp"
                android:stateListAnimator="@null"
                android:text="@string/fechar_detalhe_da_midia_na_biblioteca"
                android:textAllCaps="false"
                android:textColor="#8A9099"
                android:textSize="14sp" />

            <Button
                android:id="@+id/botao_copiar_prompt_do_detalhe_da_midia"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:background="@android:color/transparent"
                android:minWidth="0dp"
                android:minHeight="0dp"
                android:paddingStart="12dp"
                android:paddingEnd="12dp"
                android:stateListAnimator="@null"
                android:text="@string/copiar_prompt_do_detalhe_da_midia"
                android:textAllCaps="false"
                android:textColor="#8AB4F8"
                android:textSize="14sp" />

            <Button
                android:id="@+id/botao_copiar_caminho_do_detalhe_da_midia"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:background="@android:color/transparent"
                android:minWidth="0dp"
                android:minHeight="0dp"
                android:paddingStart="12dp"
                android:paddingEnd="12dp"
                android:stateListAnimator="@null"
                android:text="@string/copiar_caminho_do_detalhe_da_midia"
                android:textAllCaps="false"
                android:textColor="#8AB4F8"
                android:textSize="14sp" />

            <Button
                android:id="@+id/botao_baixar_do_detalhe_da_midia"
                android:layout_width="wrap_content"
                android:layout_height="40dp"
                android:background="@drawable/fundo_do_campo_da_caixa_de_dialogo_do_editor_de_imagem"
                android:minWidth="0dp"
                android:minHeight="0dp"
                android:paddingStart="16dp"
                android:paddingEnd="16dp"
                android:stateListAnimator="@null"
                android:text="@string/baixar_do_detalhe_da_midia"
                android:textAllCaps="false"
                android:textColor="#8AB4F8"
                android:textSize="14sp" />
        </LinearLayout>
    </HorizontalScrollView>
</LinearLayout>


===== CHIP ROW in gaveta layout =====

140-                android:minWidth="0dp"
141-                android:text="@string/videos_favoritos" />
142-        </LinearLayout>
143-
144-        <HorizontalScrollView
145:            android:id="@+id/faixa_chips_origem_da_biblioteca"
146-            android:layout_width="match_parent"
147-            android:layout_height="wrap_content"
148-            android:layout_marginTop="4dp"
149-            android:scrollbars="none"
150-            android:visibility="gone">
151-
152-            <LinearLayout
153:                android:id="@+id/linha_chips_origem_da_biblioteca"
154-                android:layout_width="wrap_content"
155-                android:layout_height="wrap_content"
156-                android:gravity="center_vertical"
157-                android:orientation="horizontal" />
158-        </HorizontalScrollView>

===== Painel functions (grep context) =====

799:        botao.text = "ⓘ"
967:    private fun cartao(item: ItemDaBibliotecaDeVideos): View {
1056:        cartao.addView(botaoDeInformacao(lado) { mostrarModalDoDetalheDoVideo(item) })
1418:        cartao.addView(botaoDeInformacao(lado) { mostrarModalDoDetalheDaImagem(item) })
2015:        fun atualizarChipsDaCaixa(forcarLivre: Boolean = false) {
2785:    private fun mostrarModalDoDetalheDoVideo(item: ItemDaBibliotecaDeVideos) {
2790:        mostrarModalDoDetalheDaMidia(
2800:            val arquivo = arquivoLocal ?: return@mostrarModalDoDetalheDaMidia false
2817:    private fun mostrarModalDoDetalheDaImagem(item: ItemDaBibliotecaDeImagensCapturadas) {
2820:        mostrarModalDoDetalheDaMidia(
2848:    private fun copiarTextoDoDetalhe(rotulo: String, texto: String, mensagem: Int) {
2858:    private fun mostrarModalDoDetalheDaMidia(
2949:                copiarTextoDoDetalhe("prompt", prompt, R.string.prompt_copiado_do_detalhe_da_midia)
2957:                copiarTextoDoDetalhe("caminho", caminhoParaCopiar, R.string.caminho_copiado_do_detalhe_da_midia)
