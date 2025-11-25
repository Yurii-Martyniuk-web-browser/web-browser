package com.webbrowser.webbrowser.browser.core;

import javafx.application.Platform;

class JavaFxUpdater {

    public static void update(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
