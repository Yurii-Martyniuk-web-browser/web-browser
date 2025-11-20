module com.webbrowser.webbrowser {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.scripting;

    opens com.webbrowser.webbrowser to javafx.fxml;
    exports com.webbrowser.webbrowser;
}