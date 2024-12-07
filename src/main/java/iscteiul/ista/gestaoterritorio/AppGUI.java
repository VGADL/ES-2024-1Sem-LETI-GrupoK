package iscteiul.ista.gestaoterritorio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AppGUI extends Application {

    private static Stage stage;
    private static Scene sceneFicheiro,sceneMenu,sceneSearch,sceneGrafo,sceneMedia;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carregar os ficheiros FXML
        GUIController guiController = new GUIController();
        FXMLLoader Menu = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Menu-view.fxml"));
        Menu.setController(guiController);
        Parent parentMenu = Menu.load();
        FXMLLoader Search = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Search-view.fxml"));
        Search.setController(guiController);
        Parent parentSearch = Search.load();
        FXMLLoader Grafo = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Graph-view.fxml"));
        Grafo.setController(guiController);
        Parent parentGrafo = Grafo.load();
        FXMLLoader Media = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Media-view.fxml"));
        Media.setController(guiController);
        Parent parentMedia = Media.load();
        FXMLLoader Ficheiro = new FXMLLoader(getClass().getResource("/iscteiul.ista.gestaoterritorio/Ficheiro-view.fxml"));
        Ficheiro.setController(guiController);
        Parent parentFicheiro = Ficheiro.load();
        stage=primaryStage;
        // Definir o título e a cena
        primaryStage.setTitle("Gestão do Território");
        // Passar o controlador compartilhado para todos os FXM
        sceneFicheiro = new Scene(parentFicheiro);
        sceneMenu = new Scene(parentMenu);
        sceneSearch = new Scene(parentSearch);
        sceneGrafo = new Scene(parentGrafo);
        sceneMedia = new Scene(parentMedia);
        primaryStage.setScene(sceneFicheiro);
        stage.setResizable(false);
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


}
