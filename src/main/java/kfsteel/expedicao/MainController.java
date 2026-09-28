package kfsteel.expedicao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

public class MainController {

    @FXML private Label lblBadge;

    // Conferência por código de barras
    @FXML private TextField txtCodigoBarras;

    // Tabela
    @FXML private TableView<ItemPedido> tabelaItens;
    @FXML private TableColumn<ItemPedido, String> colCodigo;
    @FXML private TableColumn<ItemPedido, String> colDescricao;
    @FXML private TableColumn<ItemPedido, String> colTipo;
    @FXML private TableColumn<ItemPedido, String> colStatus;

    // Formulário CRUD
    @FXML private TextField txtCodigo;
    @FXML private TextField txtDescricao;
    @FXML private ComboBox<String> cbTipo;
    @FXML private ComboBox<String> cbStatus;

    // Progresso
    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgresso;

    private final ObservableList<ItemPedido> listaItens = FXCollections.observableArrayList();
    private Projeto projeto;

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tabelaItens.setItems(listaItens);

        cbTipo.setItems(FXCollections.observableArrayList("Conjunto", "Subconjunto", "Peça"));
        cbStatus.setItems(FXCollections.observableArrayList("PENDENTE", "SEPARADO"));
        limparFormulario();

        // Ao selecionar uma linha, os dados vão para o formulário (para editar)
        tabelaItens.getSelectionModel().selectedItemProperty().addListener((obs, antigo, item) -> {
            if (item != null) {
                txtCodigo.setText(item.getCodigoBarras());
                txtDescricao.setText(item.getDescricao());
                cbTipo.setValue(item.getTipo());
                cbStatus.setValue(item.getStatus());
            }
        });
    }

    /** Chamado pela tela de projetos logo depois de carregar o FXML. */
    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
        lblBadge.setText("Pedido #" + projeto.getNumeroPedido() + " - Etiqueta " + projeto.getCorEtiqueta());
        carregarItens();
    }

    private void carregarItens() {
        listaItens.setAll(Database.listarItens(projeto.getId()));
        atualizarProgresso();
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

    @FXML
    private void handleAdicionar() {
        if (!formularioValido()) return;

        boolean ok = Database.inserirItem(projeto.getId(), txtCodigo.getText().trim(),
                txtDescricao.getText().trim(), cbTipo.getValue(), cbStatus.getValue());

        if (ok) {
            carregarItens();
            limparFormulario();
        } else {
            Ui.alerta(Alert.AlertType.ERROR, "Erro", "Já existe um item com este código de barras.");
        }
    }

    @FXML
    private void handleSalvar() {
        ItemPedido selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Ui.alerta(Alert.AlertType.WARNING, "Nenhum item selecionado", "Selecione um item na tabela para editar.");
            return;
        }
        if (!formularioValido()) return;

        boolean ok = Database.atualizarItem(selecionado.getId(), txtCodigo.getText().trim(),
                txtDescricao.getText().trim(), cbTipo.getValue(), cbStatus.getValue());

        if (ok) {
            carregarItens();
            limparFormulario();
        } else {
            Ui.alerta(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar. Verifique se o código de barras já existe.");
        }
    }

    @FXML
    private void handleExcluir() {
        ItemPedido selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Ui.alerta(Alert.AlertType.WARNING, "Nenhum item selecionado", "Selecione um item na tabela para excluir.");
            return;
        }
        if (!Ui.confirmar("Excluir item", "Excluir o item " + selecionado.getCodigoBarras() + "?")) return;

        if (Database.excluirItem(selecionado.getId())) {
            carregarItens();
            limparFormulario();
        } else {
            Ui.alerta(Alert.AlertType.ERROR, "Erro", "Não foi possível excluir o item.");
        }
    }

    @FXML
    private void handleLimpar() {
        limparFormulario();
    }

    private boolean formularioValido() {
        if (txtCodigo.getText().trim().isEmpty() || txtDescricao.getText().trim().isEmpty()
                || cbTipo.getValue() == null || cbStatus.getValue() == null) {
            Ui.alerta(Alert.AlertType.WARNING, "Campos Incompletos", "Preencha código, descrição, tipo e status.");
            return false;
        }
        return true;
    }

    private void limparFormulario() {
        tabelaItens.getSelectionModel().clearSelection();
        txtCodigo.clear();
        txtDescricao.clear();
        cbTipo.getSelectionModel().selectFirst();
        cbStatus.setValue("PENDENTE");
    }

    // ------------------------------------------------------------------
    // CONFERÊNCIA
    // ------------------------------------------------------------------

    @FXML
    private void handleConferirPeca() {
        String codigoBipado = txtCodigoBarras.getText().trim();
        if (codigoBipado.isEmpty()) return;

        for (ItemPedido item : listaItens) {
            if (item.getCodigoBarras().equalsIgnoreCase(codigoBipado)) {
                if ("SEPARADO".equals(item.getStatus())) {
                    Ui.alerta(Alert.AlertType.INFORMATION, "Já conferida", "Esta peça já foi separada.");
                } else {
                    item.setStatus("SEPARADO");
                    Database.atualizarStatus(item.getId(), "SEPARADO");
                    tabelaItens.refresh();
                    atualizarProgresso();
                }
                txtCodigoBarras.clear();
                return;
            }
        }
        Ui.alerta(Alert.AlertType.WARNING, "Atenção", "Peça não pertence a este pedido ou código inválido!");
    }

    private void atualizarProgresso() {
        if (listaItens.isEmpty()) {
            progressBar.setProgress(0);
            lblProgresso.setText("0% (0 / 0 itens)");
            return;
        }
        long separados = listaItens.stream().filter(i -> "SEPARADO".equals(i.getStatus())).count();
        double progresso = (double) separados / listaItens.size();

        progressBar.setProgress(progresso);
        lblProgresso.setText(String.format("%.0f%% (%d / %d itens)", progresso * 100, separados, listaItens.size()));
    }

    // ------------------------------------------------------------------
    // NAVEGAÇÃO
    // ------------------------------------------------------------------

    @FXML
    private void handleVoltar() {
        try {
            Ui.ir(tabelaItens, "/Projetos-view.fxml", "Sistema de Expedição - Projetos", 780, 600);
        } catch (IOException e) {
            e.printStackTrace();
            Ui.alerta(Alert.AlertType.ERROR, "Erro de Navegação", "Erro ao voltar para os projetos: " + e.getMessage());
        }
    }
}
