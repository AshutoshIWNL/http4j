package com.asm.http4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ashutosh
 * @since 7/5/25
 * HttpServer is responsible for accepting incoming connections
 * and dispatching them to virtual threads for handling.
 */
public class HttpServer {

    private static final Logger logger = LogManager.getLogger(HttpServer.class);

    private static final AtomicInteger THREAD_ID = new AtomicInteger(0);

    private static final int DEFAULT_SO_TIMEOUT_MS = 10_000;

    private final int port;
    private final Router router;
    private final File staticRoot;

    public HttpServer(int port, Router router, File staticRoot) {
        this.port = port;
        this.router = router;
        this.staticRoot = staticRoot;
    }

    /**
     * Starts the HTTP server on the configured port using virtual threads.
     */
    public void start() {
        logger.info("Attempting to start http4j on port {}", port);
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("http4j started successfully on port {}", port);
            acceptConnections(serverSocket);
        } catch (IOException ioException) {
            logger.error("Failed to start http4j: {}", ioException.getMessage());
            throw new RuntimeException("Failed to start http4j on port: " + port, ioException);
        }
    }

    /**
     * Accepts incoming client connections and delegates them to virtual threads.
     */
    private void acceptConnections(ServerSocket serverSocket) throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            clientSocket.setSoTimeout(DEFAULT_SO_TIMEOUT_MS); // Avoid hanging client connections
            String threadName = "http4j-" + THREAD_ID.getAndIncrement();
            Thread.ofVirtual().name(threadName).start(() -> new ClientHandler(clientSocket, router, staticRoot).handle());
        }
    }
}
