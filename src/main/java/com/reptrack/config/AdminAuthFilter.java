package com.reptrack.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Protects all /api/sync/** endpoints with a simple shared-secret header check.
 * These endpoints trigger external API calls (FEC, Congress.gov, Voteview) 
 * and write to the database (cannot be publicly accessible).
 */
@Component
public class AdminAuthFilter implements Filter {

    @Value("${admin.api.key}")
    private String adminApiKey;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String providedKey = request.getHeader("X-Admin-Key");
        if (providedKey == null || !providedKey.equals(adminApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: missing or invalid X-Admin-Key header");
            return;
        }

        chain.doFilter(req, res);
    }
}