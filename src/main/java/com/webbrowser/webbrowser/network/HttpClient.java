package com.webbrowser.webbrowser.network;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpClient {

    private static final Logger log = Logger.getLogger(HttpClient.class.getName());
    private final Pattern CHARSET_PATTERN = Pattern.compile("charset=([\\w\\-]+)", Pattern.CASE_INSENSITIVE);

    public HttpResponse sendRequest(HttpRequest request) throws IOException {
        Socket socket = null;

        try {
            if (request.isSecure()) {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = factory.createSocket(request.getHost(), request.getPort());
            } else {
                socket = new Socket(request.getHost(), request.getPort());
            }

            OutputStream output = socket.getOutputStream();
            output.write(request.toRequestString().getBytes(StandardCharsets.UTF_8));
            output.flush();

            InputStream input = socket.getInputStream();

            return parseResponse(input);

        } catch (IOException e) {
            log.log(Level.WARNING, "Error while sending request", e);
            String protocol = request.isSecure() ? "HTTPS" : "HTTP";
            return HttpResponse.serviceUnavailable("Connection via " + protocol + " to " + request.getHost() + " failed.");
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Error while closing socket", e);
                }
            }
        }
    }

    private HttpResponse parseResponse(InputStream input) throws IOException {

        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int prev = -1, curr;

        while ((curr = input.read()) != -1) {
            headerBuffer.write(curr);

            if (prev == '\r' && curr == '\n') {
                byte[] hb = headerBuffer.toByteArray();
                int len = hb.length;

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

        String headerText = headerBuffer.toString(StandardCharsets.ISO_8859_1);
        String[] headerLines = headerText.split("\r\n");

        String[] statusParts = headerLines[0].split(" ", 3);
        int statusCode = Integer.parseInt(statusParts[1]);
        String statusText = statusParts[2];

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

        String contentType = headers.getOrDefault("Content-Type", "");
        Charset charset = determineCharset(contentType);

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
                    log.log(Level.WARNING, "Unsupported charset: " + charsetName);
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
}