package iscteiul.ista.gestaoterritorio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AppGUI extends Application {

    private static Stage stage;
    private static Scene sceneMenu,sceneSearch,sceneGrafo,sceneMedia;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carregar os arquivos FXML
        FXMLLoader Menu = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Menu-view.fxml"));
        FXMLLoader Search = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Search-view.fxml"));
        FXMLLoader Grafo = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Graph-view.fxml"));
        FXMLLoader Media = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Media-view.fxml"));
        stage=primaryStage;
        // Definir o título e a cena
        primaryStage.setTitle("Gestão do Território");

        Parent parentMenu = Menu.load();
        Parent parentSearch = Search.load();
        Parent parentGrafo = Grafo.load();
        Parent parentMedia = Media.load();

        sceneMenu = new Scene(parentMenu);
        sceneSearch = new Scene(parentSearch);
        sceneGrafo = new Scene(parentGrafo);
        sceneMedia = new Scene(parentMedia);
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
                stage.setScene(sceneSearch);
                break;
            case 3:
                stage.setScene(sceneGrafo);
                break;
            case 4:
                stage.setScene(sceneMedia);
                break;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


}
