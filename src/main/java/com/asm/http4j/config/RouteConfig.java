package com.asm.http4j.config;

import java.util.List;
import java.util.Map;

/**
 * @author ashutosh
 * @since 7/5/25
 */
public class RouteConfig {
    public List<Route> routes;

    public static class Route {
        public String method;
        public String path;
        public Response response;
    }

    public static class Response {
        public int status;
        public String contentType;
        public String body;
        public String bodyFile;
        public Map<String, String> headers;
    }
}
