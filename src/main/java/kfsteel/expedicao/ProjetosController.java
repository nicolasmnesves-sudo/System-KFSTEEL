package kfsteel.expedicao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;

public class ProjetosController {

    @FXML private ComboBox<String> cbCor;
    @FXML private TextField txtNumeroPedido;
    @FXML private TextField txtPesquisa;
    @FXML private ListView<Projeto> listaProjetos;

    private final ObservableList<Projeto> projetos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbCor.setItems(FXCollections.observableArrayList(
                "Amarela", "Azul", "Verde", "Vermelha", "Laranja", "Roxa", "Rosa", "Branca", "Preta"));

        listaProjetos.setItems(projetos);
        listaProjetos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Projeto p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Circle bolinha = new Circle(8, Color.web(Projeto.corHex(p.getCorEtiqueta())));
                bolinha.setStroke(Color.web("#cbd5e1"));

                Label nome = new Label("PEDIDO-" + p.getNumeroPedido());
                nome.getStyleClass().add("cell-title");

                Label cor = new Label("Etiqueta " + p.getCorEtiqueta());
                cor.getStyleClass().add("cell-sub");

                Region espaco = new Region();
                HBox.setHgrow(espaco, Priority.ALWAYS);

                Label progresso = new Label(p.getSeparados() + " / " + p.getTotalItens() + " itens separados");
                progresso.getStyleClass().add("cell-sub");

                HBox linha = new HBox(12, bolinha, nome, cor, espaco, progresso);
                linha.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(linha);
            }
        });

        // Duplo clique abre o projeto
        listaProjetos.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) handleAbrir();
        });

        recarregar("");
    }

    private void recarregar(String filtro) {
        projetos.setAll(Database.listarProjetos(filtro));
    }

    @FXML
    private void handleCriar() {
        String numero = txtNumeroPedido.getText().trim();
        String cor = cbCor.getEditor().getText().trim();

        if (numero.isEmpty() || cor.isEmpty()) {
            Ui.alerta(Alert.AlertType.WARNING, "Campos Incompletos", "Informe a cor da etiqueta e o número do pedido.");
            return;
        }

        if (Database.criarProjeto(numero, cor)) {
            txtNumeroPedido.clear();
            cbCor.getSelectionModel().clearSelection();
            cbCor.getEditor().clear();
            txtPesquisa.clear();
            recarregar("");
        } else {
            Ui.alerta(Alert.AlertType.ERROR, "Erro", "Já existe um pedido com o número " + numero + ".");
        }
    }

    @FXML
    private void handleExcluir() {
        Projeto p = listaProjetos.getSelectionModel().getSelectedItem();
        if (p == null) {
            Ui.alerta(Alert.AlertType.WARNING, "Nenhum pedido selecionado", "Selecione um pedido na lista para excluir.");
            return;
        }

        boolean ok = Ui.confirmar("Excluir pedido",
                "Excluir o PEDIDO-" + p.getNumeroPedido() + " e todos os seus " + p.getTotalItens() + " itens?\nEsta ação não pode ser desfeita.");
        if (!ok) return;

        if (Database.excluirProjeto(p.getId())) {
            recarregar(txtPesquisa.getText());
        } else {
            Ui.alerta(Alert.AlertType.ERROR, "Erro", "Não foi possível excluir o pedido.");
        }
    }

    @FXML
    private void handlePesquisar() {
        recarregar(txtPesquisa.getText());
    }

    @FXML
    private void handleAbrir() {
        Projeto p = listaProjetos.getSelectionModel().getSelectedItem();
        if (p == null) {
            Ui.alerta(Alert.AlertType.WARNING, "Nenhum pedido selecionado", "Selecione um pedido na lista para abrir.");
            return;
        }
        try {
            MainController controller = Ui.ir(listaProjetos, "/Principal-view.fxml",
                    "Sistema de Expedição - PEDIDO-" + p.getNumeroPedido(), 940, 740);
            controller.setProjeto(p);
        } catch (IOException e) {
            e.printStackTrace();
            Ui.alerta(Alert.AlertType.ERROR, "Erro de Navegação", "Erro ao carregar a tela do pedido: " + e.getMessage());
        }
    }

    @FXML
    private void handleSair() {
        try {
            Ui.ir(listaProjetos, "/Login-view.fxml", "Sistema de Expedição - Login", 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
            Ui.alerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de login.");
        }
    }
}
