package org.example.apigateway.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.Enumeration;

public class HeaderUtil {

    public static HttpHeaders fromRequest(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.addAll(name, Collections.list(request.getHeaders(name)));
        }
        return headers;
    }
}