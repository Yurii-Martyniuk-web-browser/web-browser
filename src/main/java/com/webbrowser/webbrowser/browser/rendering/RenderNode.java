package com.webbrowser.webbrowser.browser.rendering;

import java.util.*;

public class RenderNode {
    public enum Type { BLOCK, INLINE, TEXT, IMAGE, TABLE, ROW, CELL }
    public Type type;
    public String text;
    public String src;
    public Map<String,String> style = new HashMap<>();
    public List<RenderNode> children = new ArrayList<>();
}