package com.webbrowser.webbrowser.browser.rendering;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FxRenderer {

    // метод, який використовується в зовнішньому класі користувача та всередині цього класу
    public Node render(RenderNode rn) {
        return switch (rn.type) {
            case BLOCK -> renderBlock(rn);
            case INLINE -> renderInline(rn);
            case TEXT -> new Text(rn.text);
            case IMAGE -> renderImage(rn);
            case TABLE -> renderTable(rn);
            case ROW -> renderRow(rn);
            case CELL -> renderCell(rn);
        };
    }

    private static String mapCssProperty(String cssProperty, String value) {
        return switch (cssProperty.toLowerCase()) {
            case "color" -> "-fx-text-fill";
            case "background-color" -> "-fx-background-color: " + processBackgroundColor(value) + ";";
            case "font-size" -> "-fx-font-size";
            case "font-weight" -> "-fx-font-weight: " + processFontWeight(value) + ";";
            case "font-style" -> "-fx-font-style";
            case "text-decoration" -> "-fx-underline";
            case "text-align" -> "-fx-text-alignment: " + processTextAlign(value) + ";";
            case "width" -> "-fx-pref-width";
            case "height" -> "-fx-pref-height";
            case "padding-top", "padding-right", "padding-bottom", "padding-left",
                 "margin-top", "margin-right", "margin-bottom", "margin-left" -> "-fx-padding";
            case "border-color" -> "-fx-border-color";
            case "border-width" -> "-fx-border-width";
            case "border-style" -> "-fx-border-style";
            default -> null;
        };
    }

    private static String convertToPx(String sizeValue) {
        sizeValue = sizeValue.trim().toLowerCase();
        if (sizeValue.endsWith("px")) return sizeValue;
        if (sizeValue.endsWith("em")) {
            try {
                double emValue = Double.parseDouble(sizeValue.replace("em", "")) * 16;
                return emValue + "px";
            } catch (Exception e) { return "0px"; }
        }
        try { Double.parseDouble(sizeValue); return sizeValue + "px"; }
        catch (Exception e) { return "0px"; }
    }

    private static String processFontWeight(String value) {
        value = value.trim().toLowerCase();
        return switch (value) {
            case "bold", "700" -> "bold";
            default -> "normal";
        };
    }

    private static String processTextAlign(String value) {
        value = value.trim().toLowerCase();
        return switch (value) {
            case "left","center","right","justify" -> value;
            default -> "left";
        };
    }

    private static String processBackgroundColor(String value) {
        if (value == null || value.isEmpty()) return "transparent";
        value = value.trim();
        if (value.matches("#[0-9a-fA-F]{3,6}") || value.startsWith("rgb") || value.matches("[a-zA-Z]+"))
            return value;
        return "transparent";
    }

    private void applyFxStyles(Region node, Map<String, String> styles) {
        StringBuilder styleString = new StringBuilder();

        for (Map.Entry<String, String> entry : styles.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();

            switch (key) {
                case "color" -> styleString.append("-fx-text-fill: ").append(value).append("; ");
                case "background-color" -> styleString.append("-fx-background-color: ").append(value).append("; ");
                case "font-size" -> styleString.append("-fx-font-size: ").append(convertToPx(value)).append("; ");
                case "font-weight" -> styleString.append("-fx-font-weight: ").append(value).append("; ");
                case "text-align" -> styleString.append("-fx-alignment: ").append(mapTextAlign(value)).append("; ");
                case "padding" -> styleString.append("-fx-padding: ").append(convertToPx(value)).append("; ");
                case "border-color" -> styleString.append("-fx-border-color: ").append(value).append("; ");
                case "border-width" -> styleString.append("-fx-border-width: ").append(convertToPx(value)).append("; ");
            }
        }

        if (!styleString.isEmpty()) node.setStyle(styleString.toString());
    }


    private String mapTextAlign(String value) {
        return switch (value.toLowerCase()) {
            case "center" -> "CENTER";
            case "right" -> "TOP_RIGHT";
            default -> "TOP_LEFT";
        };
    }

    private Node renderBlock(RenderNode rn) {
        VBox box = new VBox();
        applyFxStyles(box, rn.style);

        List<Node> inlineBuffer = new ArrayList<>();
        for (RenderNode child : rn.children) {
            if (child.type == RenderNode.Type.BLOCK || child.type == RenderNode.Type.TABLE) {
                if (!inlineBuffer.isEmpty()) {
                    box.getChildren().add(new TextFlow(inlineBuffer.toArray(new Node[0])));
                    inlineBuffer.clear();
                }
                box.getChildren().add(render(child));
            } else {
                inlineBuffer.addAll(renderInlineNode(child, rn.style));
            }
        }
        if (!inlineBuffer.isEmpty())
            box.getChildren().add(new TextFlow(inlineBuffer.toArray(new Node[0])));
        return box;
    }

    private List<Node> renderInlineNode(RenderNode rn, Map<String,String> parentStyles) {
        List<Node> nodes = new ArrayList<>();
        if (rn.type == RenderNode.Type.TEXT) {
            Text t = new Text(rn.text);
            t.setStyle(createStyleString(parentStyles) + createStyleString(rn.style));
            nodes.add(t);
        } else if (rn.type == RenderNode.Type.IMAGE) nodes.add(renderImage(rn));
        else if (rn.type == RenderNode.Type.INLINE) {
            String href = rn.style.get("href");
            boolean isLink = href != null;
            for (RenderNode c : rn.children) {
                List<Node> children = renderInlineNode(c, rn.style);
                for (Node n : children) {
                    if (isLink && n instanceof Text t) {
                        t.setUnderline(true);
                        t.setOnMouseClicked(e -> System.out.println("Navigate to: " + href));
                    }
                    nodes.add(n);
                }
            }
        }
        return nodes;
    }

    private Node renderInline(RenderNode rn) {
        TextFlow tf = new TextFlow();
        for (RenderNode c : rn.children) tf.getChildren().addAll(renderInlineNode(c, rn.style));
        return tf;
    }

    private Node renderImage(RenderNode rn) {
        if (rn.image == null || rn.image.length == 0) return new Label("[Img Missing]");
        try {
            Image img = new Image(new ByteArrayInputStream(rn.image));
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            if (rn.style.containsKey("width")) {
                String val = rn.style.get("width").replace("px","");
                iv.setFitWidth(Double.parseDouble(val));
            }
            return iv;
        } catch (Exception e) {
            return new Label("[Img Error]");
        }
    }

    private Node renderTable(RenderNode tableNode) {
        GridPane gp = new GridPane();
        applyFxStyles(gp, tableNode.style);

        int rowIndex = 0;
        for (RenderNode row : tableNode.children) {
            if (row.type != RenderNode.Type.ROW) continue;

            int colIndex = 0;
            for (RenderNode cell : row.children) {
                if (cell.type != RenderNode.Type.CELL) continue;

                VBox cellBox = new VBox();
                applyFxStyles(cellBox, cell.style);

                for (RenderNode c : cell.children) {
                    Node child = render(c);
                    if (child instanceof Text t) {
                        t.setStyle(createStyleString(cell.style));
                    }
                    cellBox.getChildren().add(child);
                }

                gp.add(cellBox, colIndex, rowIndex);
                colIndex++;
            }
            rowIndex++;
        }

        return gp;
    }



    private Node renderRow(RenderNode rn) {
        HBox hb = new HBox();
        applyFxStyles(hb, rn.style);
        for (RenderNode c : rn.children) hb.getChildren().add(render(c));
        return hb;
    }

    private Node renderCell(RenderNode rn) {
        VBox cellBox = new VBox();
        applyFxStyles(cellBox, rn.style);

        for (RenderNode child : rn.children) {
            Node fxChild = render(child);
            if (fxChild instanceof Text t) t.setStyle(createStyleString(rn.style));
            cellBox.getChildren().add(fxChild);
        }
        return cellBox;
    }

    private String createStyleString(Map<String,String> styles) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> e : styles.entrySet()) {
            String fx = mapCssProperty(e.getKey(), e.getValue());
            if (fx != null) sb.append(fx).append(": ").append(e.getValue()).append("; ");
        }
        return sb.toString();
    }
}
