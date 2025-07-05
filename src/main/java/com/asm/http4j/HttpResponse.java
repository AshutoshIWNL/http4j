package com.asm.http4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.asm.http4j.util.ServerInfo.VERSION;

/**
 * @author ashutosh
 * @since 7/5/25
 */
public class HttpResponse {
    private final int status;
    private final String reason;
    private final byte[] body;
    private final String contentType;
    private final Map<String, String> headers;

    private HttpResponse(int status, String reason, byte[] body, String contentType, Map<String, String> headers) {
        this.status = status;
        this.reason = reason;
        this.body = body;
        this.contentType = contentType;
        this.headers = headers != null ? headers : new HashMap<>();
    }

    private HttpResponse(int status, String reason, byte[] body, String contentType) {
        this(status, reason, body, contentType, new HashMap<>());
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public byte[] getBody() {
        return body;
    }

    public static HttpResponse ok(byte[] body, String contentType) {
        return new HttpResponse(200, "OK", body, contentType);
    }

    public static HttpResponse notFound(byte[] body, String contentType) {
        return new HttpResponse(404, "NOT_FOUND", body, contentType);
    }

    public static HttpResponse badRequest(byte[] body, String contentType) {
        return new HttpResponse(400, "BAD_REQUEST", body, contentType);
    }

    public static HttpResponse versionNotSupported(String body) {
        return HttpResponse.of(505, "HTTP Version Not Supported", body.getBytes(), "text/plain");
    }

    public static HttpResponse methodNotAllowed(Set<String> allowedMethods) {
        String allowHeader = String.join(", ", allowedMethods);
        String body = "Method Not Allowed. Allowed: " + allowHeader;
        return new HttpResponse(405, "Method Not Allowed", body.getBytes(), "text/plain", Map.of("Allow", allowHeader));
    }

    public static HttpResponse of(int status, String reason, byte[] body, String contentType) {
        return new HttpResponse(status, reason, body, contentType);
    }

    public void writeTo(String requestMethod, BufferedWriter writer, OutputStream outputStream) throws IOException {
        if (!headers.containsKey("Date")) {
            String date = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            headers.put("Date", date);
        }
        writer.write("HTTP/1.1 " + status + " " + reason + "\r\n");

        Map<String, String> allHeaders = headers != null ? headers : new HashMap<>();

        // Ensure Content-Type is present
        if (contentType != null && !allHeaders.containsKey("Content-Type")) {
            allHeaders.put("Content-Type", contentType);
        }

        //
        allHeaders.put("Server", "http4j" + "/" + VERSION);

        // Always set Content-Length (must be correct)
        allHeaders.put("Content-Length", String.valueOf(body.length));

        // Optional: default Connection to close if not explicitly set
        allHeaders.putIfAbsent("Connection", "close");

        // Write all headers
        for (Map.Entry<String, String> entry : allHeaders.entrySet()) {
            writer.write(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }

        writer.write("\r\n");
        writer.flush();

        if (!"HEAD".equalsIgnoreCase(requestMethod)) {
            outputStream.write(body);
            outputStream.flush();
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
