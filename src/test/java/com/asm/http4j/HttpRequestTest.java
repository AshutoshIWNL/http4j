package com.asm.http4j;

import com.asm.http4j.exception.HttpParsingException;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ashutosh
 * @since 7/5/25
 */
public class HttpRequestTest {
    private HttpRequest parseFrom(String rawRequest) throws Exception {
        return HttpRequest.parse(new BufferedReader(new StringReader(rawRequest)));
    }

    @Test
    void testValidGetRequest() throws Exception {
        String req = """
                GET /hello HTTP/1.1\r
                Host: localhost\r
                \r
                """;

        HttpRequest request = parseFrom(req);

        assertEquals("GET", request.method);
        assertEquals("/hello", request.path);
        assertEquals("HTTP/1.1", request.version);
        assertEquals("localhost", request.getHeader("host"));
        assertEquals("", request.getBody());
    }

    @Test
    void testValidPostRequestWithBody() throws Exception {
        String req = """
                POST /echo HTTP/1.1\r
                Host: localhost\r
                Content-Length: 11\r
                \r
                Hello World""";

        HttpRequest request = parseFrom(req);

        assertEquals("POST", request.method);
        assertEquals("/echo", request.path);
        assertEquals("Hello World", request.getBody());
    }

    @Test
    void testMissingHostHeader() {
        String req = """
                GET / HTTP/1.1\r
                \r
                """;

        HttpParsingException ex = assertThrows(HttpParsingException.class, () -> parseFrom(req));
        assertTrue(ex.getMessage().contains("Host"));
    }

    @Test
    void testInvalidRequestLine() {
        String req = """
                GARBAGE\r
                Host: localhost\r
                \r
                """;

        HttpParsingException ex = assertThrows(HttpParsingException.class, () -> parseFrom(req));
        assertTrue(ex.getMessage().contains("Invalid request line"));
    }

    @Test
    void testUnsupportedHttpVersion() {
        String req = """
                GET / HTTP/2.0\r
                Host: localhost\r
                \r
                """;

        HttpParsingException ex = assertThrows(HttpParsingException.class, () -> parseFrom(req));
        assertTrue(ex.getMessage().contains("Unsupported HTTP version"));
    }

    @Test
    void testBadHeaderFormat() {
        String req = """
                GET / HTTP/1.1\r
                Host localhost\r
                \r
                """;

        HttpParsingException ex = assertThrows(HttpParsingException.class, () -> parseFrom(req));
        assertTrue(ex.getMessage().contains("Malformed header line"));
    }

    @Test
    void testInvalidContentLength() {
        String req = """
                POST / HTTP/1.1\r
                Host: localhost\r
                Content-Length: notANumber\r
                \r
                """;

        HttpParsingException ex = assertThrows(HttpParsingException.class, () -> parseFrom(req));
        assertTrue(ex.getMessage().contains("Invalid Content-Length"));
    }

    @Test
    void testUnsupportedTransferEncoding() {
        String req = """
                POST / HTTP/1.1\r
                Host: localhost\r
                Transfer-Encoding: chunked\r
                \r
                """;

        HttpParsingException ex = assertThrows(HttpParsingException.class, () -> parseFrom(req));
        assertTrue(ex.getMessage().contains("Unsupported Transfer-Encoding"));
    }

    @Test
    void testBlankLineIsIgnored() throws Exception {
        String rawRequest = "\r\nGET / HTTP/1.1\r\nHost: localhost\r\n\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(rawRequest));
        HttpRequest request = HttpRequest.parse(reader);
        assertEquals("GET", request.method);
        assertEquals("/", request.path);
    }

    @Test
    void testInvalidRequestLineThrows() {
        String rawRequest = "INVALID\r\nHost: localhost\r\n\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(rawRequest));
        assertThrows(HttpParsingException.class, () -> HttpRequest.parse(reader));
    }

}
