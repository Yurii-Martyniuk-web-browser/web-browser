package com.webbrowser.webbrowser.browser.rendering;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssParser {

    public static List<CssRule> parseRules(String css) {
        List<CssRule> rules = new ArrayList<>();
        if (css == null || css.isBlank()) return rules;

        css = css.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        Pattern pattern = Pattern.compile("([^{]+)\\{([^}]*)}");
        Matcher matcher = pattern.matcher(css);

        while (matcher.find()) {
            String selectorGroup = matcher.group(1);
            String body = matcher.group(2);
            Map<String, String> props = parseProperties(body);

            for (String sel : selectorGroup.split(",")) {
                rules.add(new CssRule(sel.trim(), props));
            }
        }
        return rules;
    }

    public static Map<String, String> parseProperties(String body) {
        Map<String, String> props = new HashMap<>();
        if (body == null) return props;

        for (String line : body.split(";")) {
            String[] kv = line.split(":", 2);
            if (kv.length == 2) {
                props.put(kv[0].trim().toLowerCase(), kv[1].trim());
            }
        }
        return props;
    }
}