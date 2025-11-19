package com.webbrowser.webbrowser.browser.js;

import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

public class JsEngine {

    private final Context context;

    public JsEngine() {
        Context tempContext = null;
        try {
            tempContext = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .allowCreateThread(true)
                    .build();

            System.out.println("DEBUG: GraalVM Polyglot Context created successfully.");
        } catch (Exception e) {
            System.err.println("FATAL: JavaScript engine (GraalVM JS) could not be instantiated. Reason: " + e.getMessage());
        }
        this.context = tempContext;
    }

    public void initContext(Document document) {
        if (context == null) return;

        context.getBindings("js").putMember("document", document);

        try {
            context.eval("js", "var window = this;");
            context.eval("js", """
                 var console = { 
                     log: function(msg) { 
                         // print — це функція, доступна у JS-середовищі GraalVM 
                         print('[JS Console] ' + msg); 
                     } 
                 };
            """);

            String domApiScript = """
                document.getElementById = function(id) {
                    // Оскільки ми не можемо напряму викликати Java-методи пошуку DOM 
                    // без складної інтеграції, повертаємо document.
                    return document; 
                };
            """;
            context.eval("js", domApiScript);

        } catch (Exception e) {
            System.err.println("Error initializing JS context: " + e.getMessage());
        }
    }

    public void execute(String script) {
        if (context == null) return;
        try {
            System.out.println("[JS EXECUTE] Running script of length " + script.length());
            context.eval("js", script);
        } catch (Exception e) {
            System.err.println("[JS ERROR] Execution failed: " + e.getMessage());
        }
    }
}