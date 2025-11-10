module org.example.uididididii {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.prefs;

    opens org.example.uididididii to javafx.fxml;
    exports org.example.uididididii;
}
