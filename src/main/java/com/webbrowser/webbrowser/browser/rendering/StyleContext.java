package com.webbrowser.webbrowser.browser.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleContext {
    private final Map<String, String> styleProperties = new HashMap<>();

    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+\\.?\\d*)(px|em|%)?");

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

    public Map<String, String> getStyleProperties() {
        return new HashMap<>(styleProperties);
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
            case "padding", "margin" -> "-fx-padding";
            case "border-color" -> "-fx-border-color";
            case "border", "border-top", "border-width" -> "-fx-border-width";
            case "border-radius" -> "-fx-border-radius";
            case "display" -> "-fx-visible";

            default -> "-" + cssProperty;
        };
    }

    private String processValue(String key, String value) {
        String lowerValue = value.trim().toLowerCase();

        if (key.matches(".*color.*")) {
            try {
                Double.parseDouble(lowerValue.replace("#", ""));
                return "#000000";
            } catch (NumberFormatException ignored) {
            }
            return value.trim();
        }

        if (key.matches(".*(width|height|size|padding|margin|border).*")) {

            if (key.matches("border") || key.matches("border-top")) {
                Matcher matcher = SIZE_PATTERN.matcher(lowerValue);
                if (matcher.find()) {
                    lowerValue = matcher.group(0);
                } else {
                    return "0px";
                }
            }

            if (lowerValue.contains(" ") || lowerValue.contains("auto")) {
                if (key.matches(".*(padding|margin).*")) {
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
            }

            return convertToPx(lowerValue);
        }

        return value.trim();
    }

    private String convertToPx(String sizeValue) {
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
}