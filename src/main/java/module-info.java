module org.example.ProgettoUIDFinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.prefs;
    requires javafx.base;

    opens org.example.ProgettoUIDFinal to javafx.fxml;
    exports org.example.ProgettoUIDFinal;
    exports org.example.ProgettoUIDFinal.model;
    opens org.example.ProgettoUIDFinal.model to javafx.fxml;
}
