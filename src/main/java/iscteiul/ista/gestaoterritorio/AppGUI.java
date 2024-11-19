package iscteiul.ista.gestaoterritorio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AppGUI extends Application {

    private static Stage stage;
    private static Scene sceneMenu,sceneProcuraID;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carregar os arquivos FXML
        FXMLLoader Menu = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Menu-view.fxml"));
        FXMLLoader procuraID = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/main-view.fxml"));

        stage=primaryStage;
        // Definir o título e a cena
        primaryStage.setTitle("Gestão do Território");

        Parent parentMenu = Menu.load();
        Parent parentProcuraID = procuraID.load();
        sceneMenu = new Scene(parentMenu);
        sceneProcuraID = new Scene(parentProcuraID);

        primaryStage.setScene(sceneMenu);

        // Exibir a janela
        primaryStage.show();
    }

    public static void mudarScene(int option){
        switch (option){
            case 1:
                stage.setScene(sceneMenu);
                break;
            case 2:
                stage.setScene(sceneProcuraID);
                break;

        }
    }

    public static void main(String[] args) {
        launch(args);
    }


}
