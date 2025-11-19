package com.webbrowser.webbrowser.browser.rendering.dom;

import java.util.HashMap;
import java.util.Map;


public class Attributes {
    private final Map<String, String> attributeMap = new HashMap<>();
    private final Map<String, Object> userDataMap = new HashMap<>();

    public void put(String key, String value) {
        attributeMap.put(key, value);
    }

    public void userData(String key, Object data) {
        userDataMap.put(key, data);
    }

    public Object userData(String key) {
        return userDataMap.getOrDefault(key, null);
    }

    public Object get(String key) {
        return userDataMap.getOrDefault(key, "");
    }
}