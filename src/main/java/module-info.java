module com.example.universocadenas {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.universocadenas to javafx.fxml;
    exports com.example.universocadenas;
}