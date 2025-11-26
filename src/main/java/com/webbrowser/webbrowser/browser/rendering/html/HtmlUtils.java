package com.webbrowser.webbrowser.browser.rendering.html;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&([a-zA-Z0-9]+);|&#([xX]?)(\\d+|[a-fA-F]+);");

    private static final Map<String, String> ENTITY_MAP = new HashMap<>();

    static {
        ENTITY_MAP.put("nbsp", " ");
        ENTITY_MAP.put("amp", "&");
        ENTITY_MAP.put("lt", "<");
        ENTITY_MAP.put("gt", ">");
        ENTITY_MAP.put("quot", "\"");
        ENTITY_MAP.put("apos", "'");

        ENTITY_MAP.put("copy", "©");
        ENTITY_MAP.put("reg", "®");
        ENTITY_MAP.put("trade", "™");
        ENTITY_MAP.put("Mdash", "—");
        ENTITY_MAP.put("mdash", "—");
        ENTITY_MAP.put("ndash", "–");
        ENTITY_MAP.put("hellip", "…");
        ENTITY_MAP.put("bull", "•");
        ENTITY_MAP.put("middot", "·");

        ENTITY_MAP.put("lsquo", "‘");
        ENTITY_MAP.put("rsquo", "’");
        ENTITY_MAP.put("ldquo", "“");
        ENTITY_MAP.put("rdquo", "”");
        ENTITY_MAP.put("laquo", "«");
        ENTITY_MAP.put("raquo", "»");

        ENTITY_MAP.put("euro", "€");
        ENTITY_MAP.put("pound", "£");
        ENTITY_MAP.put("yen", "¥");
        ENTITY_MAP.put("cent", "¢");
        ENTITY_MAP.put("curren", "¤");

        ENTITY_MAP.put("deg", "°");
        ENTITY_MAP.put("plusmn", "±");
        ENTITY_MAP.put("times", "×");
        ENTITY_MAP.put("divide", "÷");
        ENTITY_MAP.put("mu", "μ");
        ENTITY_MAP.put("pi", "π");
        ENTITY_MAP.put("sup2", "²");
        ENTITY_MAP.put("sup3", "³");
        ENTITY_MAP.put("frac12", "½");
        ENTITY_MAP.put("frac14", "¼");
        ENTITY_MAP.put("frac34", "¾");
        ENTITY_MAP.put("infinity", "∞");

        ENTITY_MAP.put("larr", "←");
        ENTITY_MAP.put("uarr", "↑");
        ENTITY_MAP.put("rarr", "→");
        ENTITY_MAP.put("darr", "↓");
    }

    public static String decodeEntities(String text) {
        if (text == null || text.isEmpty()) return "";
        if (!text.contains("&")) return text;

        Matcher matcher = NUMERIC_ENTITY.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String replacement = null;

            if (matcher.group(1) != null) {
                String name = matcher.group(1);
                replacement = ENTITY_MAP.get(name);
            }
            else if (matcher.group(3) != null) {
                try {
                    String isHex = matcher.group(2);
                    String number = matcher.group(3);
                    int codePoint = (isHex != null && !isHex.isEmpty())
                            ? Integer.parseInt(number, 16)
                            : Integer.parseInt(number);

                    replacement = new String(Character.toChars(codePoint));
                } catch (Exception ignored) {
                }
            }

            if (replacement != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}