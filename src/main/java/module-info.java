module iscteiul.ista.gestaodeterritorio {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    //requires org.junit.platform.launcher;
    requires org.apiguardian.api;
    requires org.jgrapht.core;
    requires org.locationtech.jts;
    requires java.desktop;

    opens iscteiul.ista.gestaoterritorio to javafx.fxml;

    exports iscteiul.ista.gestaoterritorio;
}