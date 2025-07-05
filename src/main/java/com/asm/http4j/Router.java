package com.asm.http4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ashutosh
 * @since 7/5/25
 */
public class Router {
    private final Map<String, Map<String, HttpHandler>> routes = new HashMap<>();

    public void get(String path, HttpHandler handler) {
        addRoute("GET", path, handler);
    }

    public void post(String path, HttpHandler handler) {
        addRoute("POST", path, handler);
    }

    public void addRoute(String method, String path, HttpHandler handler) {
        routes
                .computeIfAbsent(method.toUpperCase(), m -> new HashMap<>())
                .put(path, handler);
    }

    public HttpHandler findHandler(String method, String path) {
        Map<String, HttpHandler> methodRoutes = routes.get(method.toUpperCase());
        if (methodRoutes == null) return null;
        return methodRoutes.getOrDefault(path, null);
    }

    public Set<String> allowedMethods(String path) {
        Set<String> methods = new HashSet<>();
        for (Map.Entry<String, Map<String, HttpHandler>> entry : routes.entrySet()) {
            String method = entry.getKey();
            if (entry.getValue().containsKey(path)) {
                methods.add(method.toUpperCase());
            }
        }
        return methods;
    }
}
