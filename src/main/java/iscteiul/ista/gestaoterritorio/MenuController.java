package iscteiul.ista.gestaoterritorio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    protected void procurarTerrenoClick(ActionEvent event) {
        AppGUI.mudarScene(2);
    }

    @FXML
    protected void vizualizarGrafoClick(ActionEvent event) {
        AppGUI.mudarScene(3);
    }

    @FXML
    protected void calcularAreaClick(ActionEvent event) {
        AppGUI.mudarScene(4);
    }

    @FXML
    void close(ActionEvent event) {
        System.exit(0);
    }
}
