package org.example.uididididii;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

public class BackgroundService {
    private static final BackgroundService INSTANCE = new BackgroundService();
    private final ObjectProperty<Image> background = new SimpleObjectProperty<>(null);

    private BackgroundService() {}

    public static BackgroundService getInstance() {
        return INSTANCE;
    }

    public Image getBackground() { return background.get(); }
    public ObjectProperty<Image> backgroundProperty() { return background; }

    public void setBackground(Image image) {
        if (Platform.isFxApplicationThread()) {
            background.set(image);
        } else {
            Platform.runLater(() -> background.set(image));
        }
    }
}
