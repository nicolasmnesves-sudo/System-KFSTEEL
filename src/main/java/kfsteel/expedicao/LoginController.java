package kfsteel.expedicao;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Button loginButton;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            Ui.alerta(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor, preencha o usuário e a senha.");
            return;
        }

        if (Database.autenticar(username, password)) {
            abrirTela(event, "/Projetos-view.fxml", "Sistema de Expedição - Projetos", 780, 600);
        } else {
            Ui.alerta(Alert.AlertType.ERROR, "Falha no Login", "Usuário ou senha incorretos.");
        }
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        Ui.alerta(Alert.AlertType.INFORMATION, "Recuperação de Senha", "Entre em contato com o suporte do sistema.");
    }

    @FXML
    public void handleAbrirCadastro(ActionEvent event) {
        abrirTela(event, "/Cadastro.fxml", "Sistema de Expedição - Criar Conta", 0, 0);
    }

    private void abrirTela(ActionEvent event, String fxml, String titulo, double largura, double altura) {
        try {
            Ui.ir((Node) event.getSource(), fxml, titulo, largura, altura);
        } catch (IOException e) {
            e.printStackTrace();
            Ui.alerta(Alert.AlertType.ERROR, "Erro de Navegação", "Erro ao carregar a tela: " + e.getMessage());
        }
    }
}
