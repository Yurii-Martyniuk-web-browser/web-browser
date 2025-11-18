package com.webbrowser.webbrowser.network;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpClient {

    private final Pattern CHARSET_PATTERN = Pattern.compile("charset=([\\w\\-]+)", Pattern.CASE_INSENSITIVE);

    public HttpResponse sendRequest(HttpRequest request) throws IOException {
        Socket socket = null;

        try {
            if (request.isSecure()) {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = factory.createSocket(request.getHost(), request.getPort());
                System.out.println("Established secure connection to " + request.getHost());
            } else {
                socket = new Socket(request.getHost(), request.getPort());
            }

            OutputStream output = socket.getOutputStream();
            output.write(request.toRequestString().getBytes(StandardCharsets.UTF_8));
            output.flush();

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.ISO_8859_1));

            return parseResponse(reader);

        } catch (IOException e) {
            System.err.println("Network error: Could not connect to " + request.getHost() + " over port " + request.getPort() + ". Error: " + e.getMessage());
            String protocol = request.isSecure() ? "HTTPS" : "HTTP";
            return HttpResponse.serviceUnavailable("Connection via " + protocol + " to " + request.getHost() + " failed.");
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private HttpResponse parseResponse(BufferedReader reader) throws IOException {
        String statusLine = reader.readLine();
        if (statusLine == null || statusLine.isEmpty()) {
            return HttpResponse.internalError("Empty or malformed response from server.");
        }

        String[] statusParts = statusLine.split(" ", 3);
        if (statusParts.length < 3) {
            return HttpResponse.internalError("Malformed status line: " + statusLine);
        }

        int statusCode = Integer.parseInt(statusParts[1]);
        String statusText = statusParts[2];

        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while (!(headerLine = reader.readLine()).isEmpty()) {
            String[] headerParts = headerLine.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        String contentType = headers.get("Content-Type");
        Charset charset = determineCharset(contentType);

        System.out.println("Content-Type: " + contentType);
        System.out.println("Charset " + charset);

        StringBuilder bodyBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            bodyBuilder.append(line).append("\n");
        }

        byte[] isoBytes = bodyBuilder.toString().getBytes(StandardCharsets.ISO_8859_1);
        String body = new String(isoBytes, charset);

        return HttpResponse.create(statusCode, statusText, headers, body);
    }

    private Charset determineCharset(String contentTypeHeader) {
        if (contentTypeHeader != null) {
            Matcher matcher = CHARSET_PATTERN.matcher(contentTypeHeader);
            if (matcher.find()) {
                String charsetName = matcher.group(1);
                try {
                    return Charset.forName(charsetName);
                } catch (UnsupportedCharsetException e) {
                    System.err.println("Unsupported charset found: " + charsetName);
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
}