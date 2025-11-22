package com.webbrowser.webbrowser.browser.rendering;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FxRenderer {

    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+\\.?\\d*)(px|em|%)?");

    private static String mapCssProperty(String cssProperty) {
        return switch (cssProperty.toLowerCase()) {
            case "color" -> "-fx-text-fill";
            case "background-color" -> "-fx-background-color";
            case "font-size" -> "-fx-font-size";
            case "font-weight" -> "-fx-font-weight";
            case "text-align" -> "-fx-alignment";
            case "text-decoration" -> "-fx-underline";
            case "width" -> "-fx-pref-width";
            case "height" -> "-fx-pref-height";
            case "padding", "margin" -> "-fx-padding";
            case "border-color" -> "-fx-border-color";
            case "border", "border-top", "border-width" -> "-fx-border-width";
            case "border-radius" -> "-fx-border-radius";
            default -> null;
        };
    }

    private static String processValue(String key, String value) {
        String lowerValue = value.trim().toLowerCase();

        if (key.matches("border") || key.matches("border-top")) {
            Matcher matcher = SIZE_PATTERN.matcher(lowerValue);
            if (matcher.find()) {
                lowerValue = matcher.group(0);
            } else {
                return "0px";
            }
        }

        if (key.matches(".*(padding|margin).*") && (lowerValue.contains(" ") || lowerValue.contains("auto"))) {
            String[] parts = lowerValue.split("\\s+");
            StringBuilder converted = new StringBuilder();

            for (String part : parts) {
                if (part.equals("auto")) {
                    converted.append("0px").append(" ");
                } else {
                    converted.append(convertToPx(part)).append(" ");
                }
            }
            return converted.toString().trim();
        }

        if (key.matches(".*(width|height|size|padding|margin|border).*")) {
            return convertToPx(lowerValue);
        }

        return value.trim();
    }

    private static String convertToPx(String sizeValue) {
        sizeValue = sizeValue.trim().toLowerCase();

        if (sizeValue.endsWith("px")) {
            return sizeValue;
        } else if (sizeValue.endsWith("em")) {
            try {
                double emValue = Double.parseDouble(sizeValue.replace("em", "").trim());
                return (emValue * 16) + "px";
            } catch (NumberFormatException e) {
                return "0px";
            }
        } else {
            try {
                Double.parseDouble(sizeValue);
                return sizeValue + "px";
            } catch (NumberFormatException e) {
                return "0px";
            }
        }
    }

    private void applyFxStyles(Region node, Map<String, String> styles) {
        String styleString = createStyleString(styles);

        if (!styleString.isEmpty()) {
            node.setStyle(styleString);
        }
    }

    public Node render(RenderNode rn) {
        return switch (rn.type) {
            case BLOCK -> renderBlock(rn);
            case INLINE -> new Label("Inline Error");

            case TEXT -> new Text(rn.text);

            case IMAGE -> {
                try {
                    ImageView iv = new ImageView(new Image(rn.src, true));
                    iv.setFitWidth(200);
                    iv.setPreserveRatio(true);
                    yield iv;
                } catch (Exception e) { yield new Label("[Img Err]"); }
            }

            case TABLE -> renderTable(rn);
            case ROW -> renderRow(rn);
            case CELL -> renderCell(rn);
        };
    }

    private static String createStyleString(Map<String, String> styles) {
        StringBuilder styleString = new StringBuilder();
        for (Map.Entry<String, String> entry : styles.entrySet()) {
            String cssProp = entry.getKey();
            String value = entry.getValue();

            String fxProp = mapCssProperty(cssProp);

            if (fxProp != null) {
                String processedValue = processValue(cssProp, value);

                if (fxProp.equals("-fx-text-fill")) {
                    fxProp = "-fx-fill";
                }

                styleString.append(fxProp)
                        .append(": ")
                        .append(processedValue)
                        .append("; ");
            }
        }
        return styleString.toString();
    }


    private Node renderBlock(RenderNode rn) {
        VBox box = new VBox();
        String blockStyle = createStyleString(rn.style);
        box.setStyle(blockStyle);

        List<Node> inlineBuffer = new ArrayList<>();

        for (RenderNode child : rn.children) {
            if (child.type == RenderNode.Type.BLOCK || child.type == RenderNode.Type.TABLE) {
                if (!inlineBuffer.isEmpty()) {
                    TextFlow flow = new TextFlow(inlineBuffer.toArray(new Node[0]));
                    box.getChildren().add(flow);
                    inlineBuffer.clear();
                }

                box.getChildren().add(render(child));
            } else {
                inlineBuffer.addAll(renderInlineNode(child, rn.style));
            }
        }

        if (!inlineBuffer.isEmpty()) {
            TextFlow flow = new TextFlow(inlineBuffer.toArray(new Node[0]));
            box.getChildren().add(flow);
        }

        return box;
    }


    private Node renderInline(RenderNode rn) {
        TextFlow tf = new TextFlow();
        for (RenderNode c : rn.children) {
            Node child = render(c);
            if (child instanceof Text) {
            }
            tf.getChildren().add(child);
        }
        return tf;
    }

    private List<Node> renderInlineNode(RenderNode rn, Map<String, String> parentStyles) {
        List<Node> nodes = new ArrayList<>();

        if (rn.type == RenderNode.Type.TEXT) {
            Text t = new Text(rn.text);
            t.setStyle(createStyleString(parentStyles) + createStyleString(rn.style));
            nodes.add(t);
        }
        else if (rn.type == RenderNode.Type.IMAGE) {
            nodes.add((Node) render(rn));
        }
        else if (rn.type == RenderNode.Type.INLINE) {
            String href = rn.style.get("href");
            boolean isLink = href != null;

            for (RenderNode child : rn.children) {
                List<Node> childFxNodes = renderInlineNode(child, rn.style);

                for (Node node : childFxNodes) {
                    if (node instanceof Text) {
                        Text t = (Text) node;
                        String currentStyle = t.getStyle();
                        String newStyle = createStyleString(rn.style);
                        t.setStyle(currentStyle + newStyle);

                        if (isLink) {
                            t.setOnMouseClicked(e -> System.out.println("Navigating to: " + href));
                            t.setUnderline(true);
                        }
                    }
                    nodes.add(node);
                }
            }
        }

        return nodes;
    }


    private Node renderTable(RenderNode rn) {
        GridPane gp = new GridPane();
        applyFxStyles(gp, rn.style);
        int r = 0;
        for (RenderNode row : rn.children) {
            int c = 0;
            for (RenderNode cell : row.children) {
                Node cellNode = render(cell);
                if (cellNode instanceof Region) {
                    applyFxStyles((Region) cellNode, row.children.get(c).style);
                }
                gp.add(cellNode, c, r);
                c++;
            }
            r++;
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
        VBox v = new VBox();
        applyFxStyles(v, rn.style);
        for (RenderNode c : rn.children) v.getChildren().add(render(c));
        return v;
    }
}