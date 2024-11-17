module iscteiul.ista.tictactoefx {
        requires javafx.controls;
        requires javafx.fxml;
        requires org.junit.jupiter.api;
        requires org.apiguardian.api;

        opens iscteiul.ista.gestaoterritorio to javafx.fxml;
        exports iscteiul.ista.gestaoterritorio;
}