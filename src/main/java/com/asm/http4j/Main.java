package com.asm.http4j;

import com.asm.http4j.config.ConfigRouteLoader;
import com.asm.http4j.util.ServerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.File;

/**
 * @author ashutosh
 * @since 7/5/25
 * Entry point of htt4j
 */
@CommandLine.Command(
        name = "http4j",
        mixinStandardHelpOptions = true,
        version = "http4j 1.0",
        description = "Starts a lightweight HTTP server"
)
public class Main implements Runnable {

    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Port to run the HTTP server on
     */
    @CommandLine.Option(names = {"--port"},
            description = "Port to listen on. Default is ${DEFAULT-VALUE}.",
            defaultValue = "8080")
    private int port;

    /**
     * Path to serve files from
     */
    @CommandLine.Option(names = {"--static-root"},
            description = "Directory to serve static files from"
    )
    private File staticRoot;

    /**
     * Enable debug logging for HTTP server
     */
    @CommandLine.Option(names = {"--debug"},
            description = "Enable debug logging",
            defaultValue = "false")
    private boolean debug;

    /**
     * Routes file path
     */
    @CommandLine.Option(names = {"--routes"},
            description = "Path for routes file",
            defaultValue = "./routes.json")
    private File routeFile;

    @Override
    public void run() {
        configureLogging();
        validateOptions();

        logger.info("Starting http4j on port {}", port);

        Router router = ConfigRouteLoader.loadFrom(routeFile);
        HttpServer server = new HttpServer(port, router, staticRoot);
        server.start();
    }

    private void configureLogging() {
        ServerInfo.DEBUG_ENABLED = debug;
        logger.info("Log level set to {}", debug ? "DEBUG" : "INFO");
    }

    private void validateOptions() {
        if (port < 1 || port > 65535) {
            String error = "Invalid port: " + port + ". Must be between 1 and 65535";
            logger.error(error);
            throw new IllegalArgumentException(error);
        }
        if (staticRoot != null && (!staticRoot.exists() || !staticRoot.isDirectory())) {
            throw new IllegalArgumentException("Invalid static root: " + staticRoot);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
