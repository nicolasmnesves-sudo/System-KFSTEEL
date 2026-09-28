package kfsteel.expedicao;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

/** Utilitários de navegação entre telas e de diálogos. */
public final class Ui {
    private Ui() {}

    /** Troca a cena da janela atual. Use largura <= 0 para usar o tamanho preferido do FXML. */
    public static <T> T ir(Node origem, String fxml, String titulo, double largura, double altura) throws IOException {
        FXMLLoader loader = new FXMLLoader(Ui.class.getResource(fxml));
        Parent root = loader.load();

        Stage stage = (Stage) origem.getScene().getWindow();
        stage.setScene(largura > 0 ? new Scene(root, largura, altura) : new Scene(root));
        stage.setTitle(titulo);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
        return loader.getController();
    }

    public static void alerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public static boolean confirmar(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        return alert.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }
}
