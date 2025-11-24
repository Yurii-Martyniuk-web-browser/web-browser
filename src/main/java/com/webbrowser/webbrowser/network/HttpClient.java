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

            return parseResponse(input);

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

    private HttpResponse parseResponse(InputStream input) throws IOException {

        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int prev = -1, curr;

        // 1. Read headers manually byte-by-byte until CRLF CRLF
        while ((curr = input.read()) != -1) {
            headerBuffer.write(curr);

            // detect \r\n\r\n
            if (prev == '\r' && curr == '\n') {
                byte[] hb = headerBuffer.toByteArray();
                int len = hb.length;

                // check last 4 bytes = \r\n\r\n
                if (len >= 4 &&
                        hb[len - 4] == '\r' &&
                        hb[len - 3] == '\n' &&
                        hb[len - 2] == '\r' &&
                        hb[len - 1] == '\n') {

                    break;
                }
            }

            prev = curr;
        }

        // parse header string
        String headerText = new String(headerBuffer.toByteArray(), StandardCharsets.ISO_8859_1);
        String[] headerLines = headerText.split("\r\n");

        // --- STATUS LINE ---
        String[] statusParts = headerLines[0].split(" ", 3);
        int statusCode = Integer.parseInt(statusParts[1]);
        String statusText = statusParts[2];

        // --- HEADERS ---
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < headerLines.length; i++) {
            String line = headerLines[i];
            if (line.isEmpty()) break;
            int idx = line.indexOf(":");
            if (idx > 0) {
                String k = line.substring(0, idx).trim();
                String v = line.substring(idx + 1).trim();
                headers.put(k, v);
            }
        }

        // determine charset
        String contentType = headers.getOrDefault("Content-Type", "");
        Charset charset = determineCharset(contentType);

        // 2. Now read body RAW
        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int read;

        while ((read = input.read(buf)) != -1) {
            bodyBuffer.write(buf, 0, read);
        }

        byte[] bodyBytes = bodyBuffer.toByteArray();

        return HttpResponse.create(statusCode, statusText, headers, bodyBytes, charset);
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