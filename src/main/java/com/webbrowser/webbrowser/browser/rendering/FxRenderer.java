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
    private final LayoutStrategy tableStrategy = new TableLayoutStrategy();

    public Node render(RenderNode rn) {
        if (rn == null) return new Region();

        return switch (rn.type) {
            case BLOCK, CELL -> blockStrategy.createNode(rn, this);
            case INLINE, TEXT -> inlineStrategy.createNode(rn, this);
            case IMAGE -> imageStrategy.createNode(rn, this);
            case TABLE -> tableStrategy.createNode(rn, this);
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

    private static class TableLayoutStrategy implements LayoutStrategy {
        @Override
        public Node createNode(RenderNode rn, FxRenderer renderer) {
            GridPane grid = new GridPane();

            applyBoxModelStyles(grid, rn.style);

            grid.setHgap(2);
            grid.setVgap(2);

            int rowIndex = 0;

            List<RenderNode> rows = extractRows(rn);

            for (RenderNode rowNode : rows) {
                int colIndex = 0;

                for (RenderNode cellNode : rowNode.children) {
                    if (cellNode.type != RenderNode.Type.CELL) continue;

                    VBox cellBox = new VBox();
                    applyBoxModelStyles(cellBox, cellNode.style);

                    for (RenderNode content : cellNode.children) {
                        cellBox.getChildren().add(renderer.render(content));
                    }

                    grid.add(cellBox, colIndex, rowIndex);

                    GridPane.setHgrow(cellBox, Priority.ALWAYS);
                    GridPane.setVgrow(cellBox, Priority.ALWAYS);

                    colIndex++;
                }
                rowIndex++;
            }

            return grid;
        }

        private List<RenderNode> extractRows(RenderNode tableNode) {
            List<RenderNode> rows = new ArrayList<>();
            for (RenderNode child : tableNode.children) {
                if (child.type == RenderNode.Type.ROW) {
                    rows.add(child);
                } else if (child.type == RenderNode.Type.BLOCK) {
                    rows.addAll(extractRows(child));
                }
            }
            return rows;
        }
    }

    private static void applyBoxModelStyles(Region region, Map<String, String> styles) {
        String bg = styles.get("background-color");
        if (bg != null) {
            try {
                region.setBackground(new Background(new BackgroundFill(Color.web(bg), CornerRadii.EMPTY, Insets.EMPTY)));
            } catch (Exception ignored) {}
        }

        region.setMaxWidth(Double.MAX_VALUE);
        if (styles.containsKey("width")) {
            region.setPrefWidth(parseSize(styles.get("width")));
            if (!styles.get("width").contains("%")) {
                region.setMaxWidth(Region.USE_PREF_SIZE);
            }
        }
        if (styles.containsKey("height")) {
            region.setPrefHeight(parseSize(styles.get("height")));
        }

        Insets padding = resolveInsets(styles, "padding");
        region.setPadding(padding);

        Insets margin = resolveInsets(styles, "margin");
        if (!margin.equals(Insets.EMPTY)) {
            VBox.setMargin(region, margin);
            HBox.setMargin(region, margin);
        }

        String borderWidth = styles.get("border-width");
        if (borderWidth != null) {
            double b = parseSize(borderWidth);
            String borderColorStr = styles.getOrDefault("border-color", "black");
            Color borderColor = Color.BLACK;
            try { borderColor = Color.web(borderColorStr); } catch (Exception ignored) {}

            region.setBorder(new Border(new BorderStroke(
                    borderColor,
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(b)
            )));
        }
    }

    private static Insets resolveInsets(Map<String, String> styles, String prefix) {
        double top = 0, right = 0, bottom = 0, left = 0;
        String shorthand = styles.get(prefix);

        if (shorthand != null && !shorthand.isEmpty()) {
            String[] parts = shorthand.trim().split("\\s+");
            switch (parts.length) {
                case 1 -> {
                    double val = parseSize(parts[0]);
                    top = right = bottom = left = val;
                }
                case 2 -> {
                    double v = parseSize(parts[0]);
                    double h = parseSize(parts[1]);
                    top = bottom = v;
                    right = left = h;
                }
                case 3 -> {
                    top = parseSize(parts[0]);
                    right = left = parseSize(parts[1]);
                    bottom = parseSize(parts[2]);
                }
                case 4 -> {
                    top = parseSize(parts[0]);
                    right = parseSize(parts[1]);
                    bottom = parseSize(parts[2]);
                    left = parseSize(parts[3]);
                }
            }
        }

        if (styles.containsKey(prefix + "-top")) top = parseSize(styles.get(prefix + "-top"));
        if (styles.containsKey(prefix + "-right")) right = parseSize(styles.get(prefix + "-right"));
        if (styles.containsKey(prefix + "-bottom")) bottom = parseSize(styles.get(prefix + "-bottom"));
        if (styles.containsKey(prefix + "-left")) left = parseSize(styles.get(prefix + "-left"));

        return new Insets(top, right, bottom, left);
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