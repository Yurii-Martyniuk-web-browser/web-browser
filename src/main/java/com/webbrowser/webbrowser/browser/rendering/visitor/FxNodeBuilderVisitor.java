package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.visitor.NodeVisitor;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jsoup.nodes.Element;

public class FxNodeBuilderVisitor implements NodeVisitor {

    private final VBox viewPort;

    public FxNodeBuilderVisitor(VBox viewPort) {
        this.viewPort = viewPort;
    }

    @Override
    public void visit(Element element) {
        String tagName = element.tagName().toLowerCase();

        if (tagName.matches("h[1-6]|p|div")) {
            Label label = new Label(element.text());

            if (tagName.startsWith("h")) {
                int level = Integer.parseInt(tagName.substring(1));
                label.setStyle("-fx-font-weight: bold; -fx-font-size: " + (30 - 2 * level) + "pt;");
            }

            viewPort.getChildren().add(label);

        } else if (tagName.equals("a")) {
            Label link = new Label(element.text());
            link.setStyle("-fx-text-fill: blue; -fx-underline: true;");
            link.setCursor(javafx.scene.Cursor.HAND);
            link.setUserData(element.attr("href"));

            viewPort.getChildren().add(link);
        }
    }
}