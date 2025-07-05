package com.asm.http4j;

@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request);
}
