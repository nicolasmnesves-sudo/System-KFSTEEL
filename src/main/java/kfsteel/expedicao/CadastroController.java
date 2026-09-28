package kfsteel.expedicao;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class CadastroController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    public void handleCadastrar(ActionEvent event) {
        String usuario = usernameField.getText().trim();
        String senha = passwordField.getText();
        String confirmaSenha = confirmPasswordField.getText();

        if (usuario.isEmpty() || senha.isEmpty() || confirmaSenha.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Preencha todos os campos.");
            return;
        }

        if (!senha.equals(confirmaSenha)) {
            exibirAlerta(Alert.AlertType.ERROR, "Senhas divergentes", "As senhas informadas não correspondem.");
            return;
        }

        boolean sucesso = Database.cadastrarUsuario(usuario, senha);

        if (sucesso) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Usuário cadastrado com sucesso!");
            voltarParaLogin(event);
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro no Cadastro", "Este nome de usuário já está em uso.");
        }
    }

    @FXML
    public void handleVoltarLogin(ActionEvent event) {
        voltarParaLogin(event);
    }

    private void voltarParaLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sistema de Expedição - Login");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de login.");
        }
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}