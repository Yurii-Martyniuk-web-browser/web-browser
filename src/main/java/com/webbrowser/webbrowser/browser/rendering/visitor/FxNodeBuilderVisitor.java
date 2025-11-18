package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.StyleContext;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jsoup.nodes.Element;
import java.util.Stack;


public class FxNodeBuilderVisitor implements NodeVisitor {

    private final Stack<Pane> containerStack = new Stack<>();

    public FxNodeBuilderVisitor(VBox initialViewPort) {
        this.containerStack.push(initialViewPort);
    }

    private String getFinalFxStyle(Element element) {
        Object contextObj = element.attributes().userData(CssApplierVisitor.STYLE_CONTEXT_KEY);
        return (contextObj instanceof StyleContext) ? ((StyleContext) contextObj).toFxStyleString() : "";
    }

    private boolean isBlockElement(String tagName) {
        return tagName.matches("h[1-6]|p|div|ul|ol|li|hr|form|header|footer|section|article");
    }

    @Override
    public void head(Element element) {
        final String tagName = element.tagName().toLowerCase();
        final String fxStyle = getFinalFxStyle(element);

        Platform.runLater(() -> {
            Pane parentContainer = containerStack.peek();

            if (isBlockElement(tagName)) {
                VBox newBlock = new VBox();
                newBlock.setStyle(fxStyle + " -fx-spacing: 5;");

                parentContainer.getChildren().add(newBlock);

                containerStack.push(newBlock);
                parentContainer = newBlock;

                if (!element.ownText().isEmpty() || tagName.matches("h[1-6]|p")) {
                    Label textPart = new Label(element.ownText());
                    textPart.setStyle(fxStyle);
                    newBlock.getChildren().add(textPart);
                }

            } else {
                javafx.scene.Node fxNode = null;

                if (tagName.equals("a")) {
                    Label link = new Label(element.text());
                    link.setStyle(fxStyle + " -fx-text-fill: blue; -fx-underline: true;");
                    fxNode = link;
                } else if (tagName.equals("img")) {
                    String src = element.attr("src");
                    if (!src.isEmpty()) {
                        try {
                            ImageView imageView = new ImageView(new Image(src, true));
                            imageView.setStyle(fxStyle);
                            fxNode = imageView;
                        } catch (Exception e) {
                            fxNode = new Label("[Image Error: " + src + "]");
                        }
                    }
                }

                if (fxNode != null) {
                    parentContainer.getChildren().add(fxNode);
                }
            }
        });
    }

    @Override
    public void tail(Element element) {
        final String tagName = element.tagName().toLowerCase();

        Platform.runLater(() -> {
            if (isBlockElement(tagName)) {
                if (containerStack.size() > 1) {
                    containerStack.pop();
                }
            }
        });
    }
}