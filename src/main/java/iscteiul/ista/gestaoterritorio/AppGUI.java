package iscteiul.ista.gestaoterritorio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carregar o arquivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("/iscteiul.ista.gestaoterritorio/main-view.fxml"));

        // Definir o título e a cena
        primaryStage.setTitle("Gestão do Território");
        primaryStage.setScene(new Scene(root));

        // Exibir a janela
        primaryStage.show();
    }
}
