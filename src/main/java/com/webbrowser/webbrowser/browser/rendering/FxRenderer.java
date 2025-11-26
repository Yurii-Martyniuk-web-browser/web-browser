package com.webbrowser.webbrowser.browser.rendering;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FxRenderer {

    private interface LayoutStrategy {
        Node createNode(RenderNode rn, FxRenderer renderer);
    }

    private final LayoutStrategy blockStrategy = new BlockLayoutStrategy();
    private final LayoutStrategy inlineStrategy = new InlineLayoutStrategy();
    private final LayoutStrategy imageStrategy = new ImageLayoutStrategy();

    public Node render(RenderNode rn) {
        if (rn == null) return new Region();

        return switch (rn.type) {
            case BLOCK -> blockStrategy.createNode(rn, this);
            case INLINE, TEXT -> inlineStrategy.createNode(rn, this);
            case IMAGE -> imageStrategy.createNode(rn, this);
            default -> new Label("Unsupported Type");
        };
    }

    private static class BlockLayoutStrategy implements LayoutStrategy {
        @Override
        public Node createNode(RenderNode rn, FxRenderer renderer) {
            VBox box = new VBox();
            applyBoxModelStyles(box, rn.style);

            box.setMaxWidth(Double.MAX_VALUE);
            box.setFillWidth(true);

            if (rn.style.containsKey("min-height")) {
                box.setMinHeight(Double.parseDouble(rn.style.get("min-height").replace("px", "")));
            }

            List<Node> currentInlineBatch = new ArrayList<>();

            for (RenderNode child : rn.children) {
                if (child.type == RenderNode.Type.BLOCK || child.type == RenderNode.Type.TABLE) {
                    if (!currentInlineBatch.isEmpty()) {
                        addTextFlowToBox(box, currentInlineBatch, rn.style.get("text-align"));
                        currentInlineBatch.clear();
                    }
                    box.getChildren().add(renderer.render(child));
                } else {
                    Node inlineNode = renderer.render(child);
                    if (inlineNode instanceof TextFlow) {
                        currentInlineBatch.addAll(((TextFlow) inlineNode).getChildren());
                    } else {
                        currentInlineBatch.add(inlineNode);
                    }
                }
            }

            if (!currentInlineBatch.isEmpty()) {
                addTextFlowToBox(box, currentInlineBatch, rn.style.get("text-align"));
            }

            return box;
        }

        private void addTextFlowToBox(VBox box, List<Node> nodes, String align) {
            TextFlow flow = new TextFlow(nodes.toArray(new Node[0]));

            flow.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            flow.setMaxWidth(Double.MAX_VALUE);

            if ("center".equalsIgnoreCase(align)) flow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            else if ("right".equalsIgnoreCase(align)) flow.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);
            else if ("justify".equalsIgnoreCase(align)) flow.setTextAlignment(javafx.scene.text.TextAlignment.JUSTIFY);
            else flow.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);

            flow.setPadding(new javafx.geometry.Insets(0, 2, 0, 2));

            box.getChildren().add(flow);
        }
    }

    private static class InlineLayoutStrategy implements LayoutStrategy {
        @Override
        public Node createNode(RenderNode rn, FxRenderer renderer) {
            if (rn.type == RenderNode.Type.TEXT) {
                Text text = new Text(rn.text);
                applyTextStyles(text, rn.style);
                return text;
            }

            TextFlow flow = new TextFlow();
            for (RenderNode child : rn.children) {
                Node childNode = renderer.render(child);
                flow.getChildren().add(childNode);
            }
            return flow;
        }
    }

    private static class ImageLayoutStrategy implements LayoutStrategy {
        @Override
        public Node createNode(RenderNode rn, FxRenderer renderer) {
            if (rn.image != null) {
                try {
                    Image img = new Image(new ByteArrayInputStream(rn.image));
                    ImageView iv = new ImageView(img);
                    iv.setPreserveRatio(true);

                    String width = rn.style.get("width");
                    if (width != null) {
                        iv.setFitWidth(parseSize(width));
                    }
                    return iv;
                } catch (Exception e) { return new Label("[Img Err]"); }
            }
            return new Label("[Img N/A]");
        }
    }

    private static void applyBoxModelStyles(Region region, Map<String, String> styles) {
        String bg = styles.get("background-color");
        if (bg != null) {
            try {
                region.setBackground(new Background(new BackgroundFill(Color.web(bg), CornerRadii.EMPTY, Insets.EMPTY)));
            } catch (Exception ignored) {}
        } else {
            // відладка
            //region.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(1))));
        }

        region.setMaxWidth(Double.MAX_VALUE);

        double pt = parseSize(styles.getOrDefault("padding-top", styles.get("padding")));
        double pr = parseSize(styles.getOrDefault("padding-right", styles.get("padding")));
        double pb = parseSize(styles.getOrDefault("padding-bottom", styles.get("padding")));
        double pl = parseSize(styles.getOrDefault("padding-left", styles.get("padding")));
        region.setPadding(new Insets(pt, pr, pb, pl));

        String borderWidth = styles.get("border-width");
        if (borderWidth != null) {
            double b = parseSize(borderWidth);
            region.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(b))));
        }

        if (styles.containsKey("width")) region.setPrefWidth(parseSize(styles.get("width")));
        if (styles.containsKey("height")) region.setPrefHeight(parseSize(styles.get("height")));


    }

    private static void applyTextStyles(Text text, Map<String, String> styles) {
        String color = styles.get("color");
        if (color != null) {
            try { text.setFill(Color.web(color)); } catch (Exception ignored) {}
        }

        double size = 16;
        String fontSize = styles.get("font-size");
        if (fontSize != null) {
            size = parseSize(fontSize);
        }

        FontWeight weight = FontWeight.NORMAL;
        String fontWeight = styles.get("font-weight");
        if (fontWeight != null) {
            fontWeight = fontWeight.toLowerCase().trim();
            if (fontWeight.contains("bold") || fontWeight.equals("700") || fontWeight.equals("800") || fontWeight.equals("900")) {
                weight = FontWeight.BOLD;
            }
        }

        javafx.scene.text.FontPosture posture = javafx.scene.text.FontPosture.REGULAR;
        String fontStyle = styles.get("font-style");
        if ("italic".equalsIgnoreCase(fontStyle)) {
            posture = javafx.scene.text.FontPosture.ITALIC;
        }

        text.setFont(Font.font("System", weight, posture, size));

        String decoration = styles.get("text-decoration");
        if (decoration != null && decoration.contains("underline")) {
            text.setUnderline(true);
        }
    }

    private static double parseSize(String val) {
        if (val == null) return 0;
        val = val.trim().toLowerCase().replace("px", "").replace("em", ""); // dirty cleanup
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}