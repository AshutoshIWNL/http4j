package com.asm.http4j.config;

import com.asm.http4j.HttpHandler;
import com.asm.http4j.HttpResponse;
import com.asm.http4j.Router;
import com.asm.http4j.util.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author ashutosh
 * @since 7/5/25
 */
public class ConfigRouteLoader {
    public static Router loadFrom(File configFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            RouteConfig config = mapper.readValue(configFile, RouteConfig.class);
            Router router = new Router();

            for (RouteConfig.Route route : config.routes) {
                HttpHandler handler = request -> {
                    byte[] bodyBytes;
                    if (route.response.body != null) {
                        bodyBytes = route.response.body.getBytes(StandardCharsets.UTF_8);
                    } else if (route.response.bodyFile != null) {
                        bodyBytes = Files.readAllBytes(Path.of(route.response.bodyFile));
                    } else {
                        bodyBytes = new byte[0];
                    }

                    HttpResponse response = HttpResponse.of(
                            route.response.status,
                            HttpStatus.reasonPhrase(route.response.status),
                            bodyBytes,
                            route.response.contentType
                    );

                    if (route.response.headers != null) {
                        response.getHeaders().putAll(route.response.headers);
                    }

                    return response;
                };

                router.addRoute(route.method, route.path, handler);
            }

            return router;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load routes: " + e.getMessage(), e);
        }
    }
}
