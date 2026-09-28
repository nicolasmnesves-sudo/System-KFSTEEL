package kfsteel.expedicao;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Database.initDatabase();

        FXMLLoader fxmlLoader =
                new FXMLLoader(getClass().getResource("/Login-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());

        // Carrega o ícone a partir de src/main/resources
        Image icon = new Image(
                getClass().getResourceAsStream("/Images/kfsteel_logo.jpg")
        );

        stage.setTitle("Sistema de Expedição - KF Steel");
        stage.setScene(scene);

        // Define o ícone da janela
        stage.getIcons().add(icon);

        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}