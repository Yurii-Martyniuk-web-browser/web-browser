module com.webbrowser.webbrowser {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.scripting;
    requires com.google.gson;
    requires java.net.http;
    requires java.sql;
    requires org.xerial.sqlitejdbc;


    opens com.webbrowser.webbrowser to javafx.fxml;
    exports com.webbrowser.webbrowser;
    exports com.webbrowser.webbrowser.ui;
    opens com.webbrowser.webbrowser.dto to javafx.base, com.google.gson;
    opens com.webbrowser.webbrowser.ui to javafx.fxml;
}