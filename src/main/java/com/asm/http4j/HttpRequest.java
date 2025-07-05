package com.asm.http4j;

import com.asm.http4j.exception.HttpParsingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ashutosh
 * @since 7/5/25
 * Represents a parsed HTTP request
 * Parses the request line, headers, and optional body
 */
public class HttpRequest {
    public final String method;
    public final String path;
    public final String version;
    private String body = "";
    private final Map<String, List<String>> headers = new HashMap<>();


    private HttpRequest(String method, String path, String version) {
        this.method = method;
        this.path = path;
        this.version = version;
    }

    /**
     * Parses the incoming HTTP request from a reader.
     */
    public static HttpRequest parse(BufferedReader reader) throws IOException, HttpParsingException {
        //Parse request line: METHOD /path HTTP/1.1
        String line;

        while (true) {
            line = reader.readLine();

            if (line == null) {
                return null;
            }

            if (!line.isBlank()) {
                break;
            }

            // If the client just sends CRLF repeatedly without closing connection,
            // you keep looping — that's fine (eventually timeout or close)
        }

        HttpRequest request = getHttpRequest(line);

        //Parse headers
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx == -1) {
                throw new HttpParsingException("Malformed header line: " + line);
            }

            String key = line.substring(0, idx).trim().toLowerCase();
            String value = line.substring(idx + 1).trim();

            request.headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        //Reject unsupported transfer encodings
        String transferEncoding = request.getHeader("transfer-encoding");
        if (transferEncoding != null && !"identity".equalsIgnoreCase(transferEncoding)) {
            throw new HttpParsingException("Unsupported Transfer-Encoding: " + transferEncoding);
        }

        //Read body if Content-Length is present
        String contentLengthHeader = request.getHeader("content-length");
        if (contentLengthHeader != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthHeader);
                char[] bodyChars = new char[contentLength];
                int read = reader.read(bodyChars, 0, contentLength);
                request.setBody(new String(bodyChars, 0, read));
            } catch (NumberFormatException e) {
                throw new HttpParsingException("Invalid Content-Length value: " + contentLengthHeader);
            }
        }

        if (!request.headers.containsKey("host")) {
            throw new HttpParsingException("Missing required Host header");
        }

        return request;
    }

    private static HttpRequest getHttpRequest(String line) throws HttpParsingException {
        if (line == null || line.isEmpty()) {
            throw new HttpParsingException("Empty request line");
        }

        String[] parts = line.split(" ");
        if (parts.length != 3) {
            throw new HttpParsingException("Invalid request line: " + line);
        }

        if (!"HTTP/1.1".equalsIgnoreCase(parts[2])) {
            throw new HttpParsingException("Unsupported HTTP version: " + parts[2]);
        }

        return new HttpRequest(parts[0], parts[1], parts[2]);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getHeaderValues(String key) {
        return headers.getOrDefault(key.toLowerCase(), List.of());
    }

    public String getHeader(String key) {
        List<String> values = headers.get(key.toLowerCase());
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }


    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                '}';
    }
}
