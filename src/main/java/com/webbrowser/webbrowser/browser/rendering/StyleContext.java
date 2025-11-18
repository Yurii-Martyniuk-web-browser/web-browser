package com.webbrowser.webbrowser.browser.rendering;

import java.util.HashMap;
import java.util.Map;

public class StyleContext {
    private final Map<String, String> styleProperties = new HashMap<>();

    public void setProperty(String propertyName, String value) {
        styleProperties.put(propertyName, value);
    }

    public String toFxStyleString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : styleProperties.entrySet()) {
            String processedValue = processValue(entry.getKey(), entry.getValue());

            String fxProp = mapCssToFx(entry.getKey());

            sb.append(fxProp)
                    .append(": ")
                    .append(processedValue)
                    .append("; ");
        }
        return sb.toString();
    }

    private String mapCssToFx(String cssProperty) {
        return switch (cssProperty.toLowerCase()) {
            case "color" -> "-fx-text-fill";
            case "background-color" -> "-fx-background-color";
            case "font-size" -> "-fx-font-size";
            case "font-weight" -> "-fx-font-weight";
            case "text-align" -> "-fx-alignment";
            case "text-decoration" -> "-fx-underline";
            case "line-height" -> "-fx-line-spacing";
            case "width" -> "-fx-pref-width";
            case "height" -> "-fx-pref-height";
            case "max-width" -> "-fx-max-width";
            case "max-height" -> "-fx-max-height";
            case "padding" -> "-fx-padding";
            case "margin" -> "-fx-padding";
            case "border" -> "-fx-border-color";
            case "border-color" -> "-fx-border-color";
            case "border-width" -> "-fx-border-width";
            case "border-radius" -> "-fx-border-radius";
            case "display" -> "-fx-visible";

            default -> "-" + cssProperty;
        };
    }

    private String processValue(String key, String value) {
        String lowerValue = value.trim().toLowerCase();

        if (lowerValue.contains(" ")) {
            return lowerValue;
        }

        if (key.matches(".*(width|height|size|padding|margin|border).*")) {

            if (lowerValue.endsWith("px")) {
                return lowerValue;
            } else if (lowerValue.endsWith("em")) {
                try {
                    double emValue = Double.parseDouble(lowerValue.replace("em", "").trim());
                    return (emValue * 16) + "px";
                } catch (NumberFormatException e) {
                    return lowerValue;
                }
            } else {
                try {
                    Double.parseDouble(lowerValue);
                    return lowerValue + "px";
                } catch (NumberFormatException e) {
                    return lowerValue;
                }
            }
        }

        return value.trim();
    }
}