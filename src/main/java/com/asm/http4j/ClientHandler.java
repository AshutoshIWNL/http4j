package com.asm.http4j;

import com.asm.http4j.exception.HttpParsingException;
import com.asm.http4j.util.ServerInfo;
import com.asm.http4j.util.MimeTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

/**
 * @author ashutosh
 * @since 7/5/25
 */
public class ClientHandler {

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    private final Socket socket;
    private final Router router;
    private final File staticRoot;

    public ClientHandler(Socket socket, Router router, File staticRoot) {
        this.socket = socket;
        this.router = router;
        this.staticRoot = staticRoot;
    }

    public void handle() {
        try (socket;
             InputStream input = socket.getInputStream();
             OutputStream output = socket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {

            while (true) {
                HttpRequest request;
                try {
                    request = HttpRequest.parse(reader);
                    if (request == null) {
                        logger.info("Client closed connection.");
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    logger.info("Keep-alive timeout reached. Closing connection.");
                    break;
                } catch (HttpParsingException e) {
                    logger.warn("Bad request: {}", e.getMessage());
                    HttpResponse.badRequest(("Bad request: " + e.getMessage()).getBytes(), "text/plain")
                            .writeTo("UNKNOWN",writer, output);
                    break;
                } catch (IOException e) {
                    logger.warn("Client I/O error: {}", e.getMessage());
                    break;
                }

                logger.info("{} {} from {}", request.method, request.path, socket.getRemoteSocketAddress());
                if (ServerInfo.DEBUG_ENABLED) logger.debug("{}", request);

                HttpHandler handler = router.findHandler(request.method, request.path);

                if (handler == null && "HEAD".equalsIgnoreCase(request.method)) {
                    // Try fallback to GET handler
                    handler = router.findHandler("GET", request.path);
                }

                HttpResponse response;

                if (handler != null) {
                    response = handler.handle(request);
                } else if (isGetOrHead(request.method) && staticRoot != null) {
                    response = serveStaticFile(request.path);
                } else {
                    Set<String> allowed = router.allowedMethods(request.path);
                    if (!allowed.isEmpty()) {
                        response = HttpResponse.methodNotAllowed(allowed);
                    } else {
                        response = HttpResponse.notFound("Route not found".getBytes(), "text/plain");
                    }
                }

                boolean close = "close".equalsIgnoreCase(request.getHeader("connection"));
                if (close) {
                    response.getHeaders().put("Connection", "close");
                } else {
                    response.getHeaders().put("Connection", "keep-alive");
                }

                logger.info("{} {} -> {} from {}", request.method, request.path, response.getStatus(), socket.getRemoteSocketAddress());
                if (ServerInfo.DEBUG_ENABLED) logger.debug("{}", response);

                response.writeTo(request.method,writer, output);

                if (close) break;
            }
        } catch (IOException e) {
            logger.error("IO error: {}", e.getMessage());
        }
    }

    /**
     * Figures out whether the request method is a GET or HEAD
     */
    private boolean isGetOrHead(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    /**
     * Serves static files from the configured static root directory.
     */
    private HttpResponse serveStaticFile(String requestPath) {
        try {
            File file = new File(staticRoot, requestPath);
            if (!file.getCanonicalPath().startsWith(staticRoot.getCanonicalPath())) {
                return HttpResponse.of(403, "Forbidden", "Access Denied".getBytes(), "text/plain");
            }

            if (!file.exists()) {
                return HttpResponse.notFound("File not found".getBytes(), "text/plain");
            }

            if (file.isDirectory()) {
                File indexFile = new File(file, "index.html");
                if (indexFile.exists()) {
                    file = indexFile;
                } else {
                    return listDirectory(file, requestPath);
                }
            }

            String mimeType = MimeTypes.guessMimeType(file.getName());
            byte[] content = Files.readAllBytes(file.toPath());
            return HttpResponse.ok(content, mimeType);

        } catch (IOException e) {
            logger.error("Error serving static file: {}", e.getMessage());
            return HttpResponse.of(500, "Internal Server Error", "Error reading file".getBytes(), null);
        }
    }

    private HttpResponse listDirectory(File directory, String requestPath) {
        File[] files = directory.listFiles();
        if (files == null) {
            return HttpResponse.of(500, "Internal Server Error", "Unable to list directory".getBytes(), "text/plain");
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("  <meta charset=\"UTF-8\">\n")
                .append("  <title>Index of ").append(requestPath).append("</title>\n")
                .append("  <style>\n")
                .append("    body { font-family: Arial, sans-serif; padding: 20px; }\n")
                .append("    h1 { border-bottom: 1px solid #ccc; }\n")
                .append("    table { width: 100%; border-collapse: collapse; }\n")
                .append("    th, td { text-align: left; padding: 8px; border-bottom: 1px solid #eee; }\n")
                .append("    tr:hover { background-color: #f9f9f9; }\n")
                .append("    a { text-decoration: none; color: #007bff; }\n")
                .append("    a:hover { text-decoration: underline; }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("  <h1>Index of ").append(requestPath).append("</h1>\n")
                .append("  <table>\n")
                .append("    <tr><th>Name</th><th>Size</th><th>Last Modified</th></tr>\n");
        // Parent dir
        if (!requestPath.equals("/")) {
            String parent = new File(requestPath).getParent();
            if (parent == null || parent.isEmpty()) parent = "/";
            html.append("<tr><td><a href=\"").append(parent).append("\">../</a></td><td></td><td></td></tr>");
        }

        for (File f : files) {
            String name = f.getName();
            String href = requestPath.endsWith("/") ? requestPath + name : requestPath + "/" + name;
            String displayName = f.isDirectory() ? name + "/" : name;

            String size = f.isFile() ? humanReadableSize(f.length()) : "-";
            String lastModified = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date(f.lastModified()));

            html.append("<tr>")
                    .append("<td><a href=\"").append(href).append("\">").append(displayName).append("</a></td>")
                    .append("<td>").append(size).append("</td>")
                    .append("<td>").append(lastModified).append("</td>")
                    .append("</tr>");
        }

        html.append("</table></body></html>");
        return HttpResponse.ok(html.toString().getBytes(StandardCharsets.UTF_8), "text/html");
    }

    private String humanReadableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }

}
