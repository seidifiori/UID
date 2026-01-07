module org.example.ProgettoUIDFinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires java.prefs;
    requires javafx.media;
    requires java.net.http;
    requires java.xml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens org.example.ProgettoUIDFinal to javafx.fxml;
    exports org.example.ProgettoUIDFinal;
    exports org.example.ProgettoUIDFinal.model;
    opens org.example.ProgettoUIDFinal.model to javafx.fxml;
    exports org.example.ProgettoUIDFinal.Services;
    opens org.example.ProgettoUIDFinal.Services to javafx.fxml;
}
