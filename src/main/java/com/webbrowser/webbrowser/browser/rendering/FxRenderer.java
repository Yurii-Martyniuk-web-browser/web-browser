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

    // Регулярний вираз для пошуку числового розміру та одиниці
    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+\\.?\\d*)(px|em|%)?");


    // --- 1. ЛОГІКА МАРУВАННЯ ТА ОБРОБКИ ЗНАЧЕНЬ ---

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
            default -> null; // Повертаємо null, якщо мапінгу немає
        };
    }

    /** * Конвертує розмір (em, число) в px та обробляє скорочення.
     */
    private static String processValue(String key, String value) {
        String lowerValue = value.trim().toLowerCase();

        // 1. Обробка скорочених border-властивостей (якщо це ширина)
        if (key.matches("border") || key.matches("border-top")) {
            Matcher matcher = SIZE_PATTERN.matcher(lowerValue);
            if (matcher.find()) {
                lowerValue = matcher.group(0); // Наприклад, "1px"
            } else {
                return "0px"; // Безпечний вихід
            }
        }

        // 2. Обробка 'auto' та складених значень (padding/margin)
        if (key.matches(".*(padding|margin).*") && (lowerValue.contains(" ") || lowerValue.contains("auto"))) {
            String[] parts = lowerValue.split("\\s+");
            StringBuilder converted = new StringBuilder();

            for (String part : parts) {
                if (part.equals("auto")) {
                    converted.append("0px").append(" ");
                } else {
                    // Конвертуємо кожен розмір окремо
                    converted.append(convertToPx(part)).append(" ");
                }
            }
            return converted.toString().trim();
        }

        // 3. Фінальна конверсія та забезпечення 'px'
        if (key.matches(".*(width|height|size|padding|margin|border).*")) {
            return convertToPx(lowerValue);
        }

        // 4. Для кольорів та інших значень повертаємо оригінал
        return value.trim();
    }

    /**
     * Конвертує одиницю (em, число) в px.
     */
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
            // Чисте число
            try {
                Double.parseDouble(sizeValue);
                return sizeValue + "px";
            } catch (NumberFormatException e) {
                // Це ключове слово або недійсний розмір
                return "0px";
            }
        }
    }

    // --- 2. ДОПОМІЖНИЙ МЕТОД ДЛЯ ЗАСТОСУВАННЯ СТИЛІВ ---

    private void applyFxStyles(Region node, Map<String, String> styles) {
        String styleString = createStyleString(styles);

        if (!styleString.isEmpty()) {
            node.setStyle(styleString);
        }
    }


    // --- 3. МЕТОДИ РЕНДЕРИНГУ (з інтеграцією стилів) ---

    public Node render(RenderNode rn) {
        return switch (rn.type) {
            case BLOCK -> renderBlock(rn);
            case INLINE -> new Label("Inline Error");

            case TEXT -> new Text(rn.text);

            case IMAGE -> {
                try {
                    ImageView iv = new ImageView(new Image(rn.src, true));
                    iv.setFitWidth(200); // Обмеження для тесту
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

                // Якщо це властивість кольору, мапуємо на -fx-fill для Text-вузлів
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
        // Застосовуємо стилі блоку (padding, border, bg-color)
        // Примітка: VBox (Region) використовує -fx-background-color, а Text -fx-fill.
        // Потрібно фільтрувати стилі, але для прототипу кинемо все.
        String blockStyle = createStyleString(rn.style);
        // VBox не розуміє -fx-fill (колір тексту), тому це не вплине на нього, це ок.
        box.setStyle(blockStyle);

        // БУФЕР ДЛЯ ІНЛАЙН ЕЛЕМЕНТІВ
        List<Node> inlineBuffer = new ArrayList<>();

        for (RenderNode child : rn.children) {
            if (child.type == RenderNode.Type.BLOCK || child.type == RenderNode.Type.TABLE) {
                // 1. Якщо є накопичений інлайн-контент, скидаємо його в TextFlow
                if (!inlineBuffer.isEmpty()) {
                    TextFlow flow = new TextFlow(inlineBuffer.toArray(new Node[0]));
                    // Переносимо колір тексту батька на TextFlow, якщо треба
                    box.getChildren().add(flow);
                    inlineBuffer.clear();
                }

                // 2. Рендеримо блок
                box.getChildren().add(render(child));
            } else {
                // 3. Це INLINE, TEXT або IMAGE. Додаємо в буфер.
                // Нам потрібен список JavaFX Node (Text, Hyperlink, ImageView)
                inlineBuffer.addAll(renderInlineNode(child, rn.style));
            }
        }

        // Скидаємо залишок буфера
        if (!inlineBuffer.isEmpty()) {
            TextFlow flow = new TextFlow(inlineBuffer.toArray(new Node[0]));
            box.getChildren().add(flow);
        }

        return box;
    }


    private Node renderInline(RenderNode rn) {
        TextFlow tf = new TextFlow();
        // Примітка: TextFlow не є Region, тому applyFxStyles до нього не застосовується
        // Стилі застосовуються до його дочірніх Text-вузлів або HBox/VBox-батьків
        for (RenderNode c : rn.children) {
            Node child = render(c);
            if (child instanceof Text) {
                // Стилі мають бути застосовані до Text-вузла, якщо це можливо
            }
            tf.getChildren().add(child);
        }
        return tf;
    }

    private List<Node> renderInlineNode(RenderNode rn, Map<String, String> parentStyles) {
        List<Node> nodes = new ArrayList<>();

        if (rn.type == RenderNode.Type.TEXT) {
            Text t = new Text(rn.text);
            // Комбінуємо стилі батька і власні (хоча у тексту власних зазвичай немає)
            t.setStyle(createStyleString(parentStyles) + createStyleString(rn.style));
            nodes.add(t);
        }
        else if (rn.type == RenderNode.Type.IMAGE) {
            nodes.add((Node) render(rn)); // Реюз методу render для картинки
        }
        else if (rn.type == RenderNode.Type.INLINE) {
            // Це наприклад <span>, <b>, <a>

            // Спеціальна обробка посилань
            String href = rn.style.get("href");
            boolean isLink = href != null;

            for (RenderNode child : rn.children) {
                // Рекурсивно отримуємо FX nodes для дітей
                List<Node> childFxNodes = renderInlineNode(child, rn.style);

                for (Node node : childFxNodes) {
                    if (node instanceof Text) {
                        Text t = (Text) node;
                        // Додаємо стилі поточного інлайн елемента (наприклад жирність від <b>)
                        String currentStyle = t.getStyle();
                        String newStyle = createStyleString(rn.style);
                        t.setStyle(currentStyle + newStyle);

                        if (isLink) {
                            t.setOnMouseClicked(e -> System.out.println("Navigating to: " + href));
                            t.setUnderline(true);
                            // В реальному додатку тут треба викликати callback в BrowserTabContent
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
                    // Застосовуємо стилі до CellNode (якщо це VBox)
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