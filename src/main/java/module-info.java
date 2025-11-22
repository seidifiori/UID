module org.example.ProgettoUIDFinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires java.prefs;

    opens org.example.ProgettoUIDFinal to javafx.fxml;
    exports org.example.ProgettoUIDFinal;
    exports org.example.ProgettoUIDFinal.model;
    opens org.example.ProgettoUIDFinal.model to javafx.fxml;
}
