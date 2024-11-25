module iscteiul.ista.tictactoefx {
        requires javafx.controls;
        requires javafx.fxml;
        requires org.junit.jupiter.api;
        requires org.apiguardian.api;
        requires org.jgrapht.core;
        requires org.locationtech.jts;

        opens iscteiul.ista.gestaoterritorio to javafx.fxml;
    requires java.desktop;
    exports iscteiul.ista.gestaoterritorio;
}