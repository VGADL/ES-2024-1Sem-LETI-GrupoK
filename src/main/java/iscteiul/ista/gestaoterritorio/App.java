package iscteiul.ista.gestaoterritorio;

import javafx.application.Application;

/**
 * Classe principal que serve como ponto de entrada para a aplicação.
 * Esta classe inicializa a aplicação, pela classe {@link AppGUI},
 * que gere a interface gráfica da aplicação JavaFX.
 */
public class App {

    public static void main(String[] args) {
        // Lança a aplicação JavaFX a partir da classe AppGUI
        Application.launch(AppGUI.class, args);

    }

}