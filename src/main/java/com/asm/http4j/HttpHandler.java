package com.asm.http4j;

import java.io.IOException;

@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request) throws IOException;
}