package com.asm.http4j.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ashutosh
 * @since 7/5/25
 */
public class MimeTypes {
    private static final Map<String, String> MIME_MAP = new HashMap<>();
    static {
        MIME_MAP.put("html", "text/html");
        MIME_MAP.put("css", "text/css");
        MIME_MAP.put("js", "application/javascript");
        MIME_MAP.put("json", "application/json");
        MIME_MAP.put("png", "image/png");
        MIME_MAP.put("jpg", "image/jpeg");
        MIME_MAP.put("jpeg", "image/jpeg");
        MIME_MAP.put("gif", "image/gif");
        MIME_MAP.put("txt", "text/plain");
        MIME_MAP.put("ico", "image/x-icon");
    }

    public static String guessMimeType(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx == -1) return "application/octet-stream";
        return MIME_MAP.getOrDefault(fileName.substring(idx + 1).toLowerCase(), "application/octet-stream");
    }
}
