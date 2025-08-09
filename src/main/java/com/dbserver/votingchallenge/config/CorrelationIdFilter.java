//package com.dbserver.votingchallenge.config;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.MDC;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class CorrelationIdFilter extends OncePerRequestFilter {
//    public static final String HEADER = "X-Correlation-Id";
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        String cid = request.getHeader(HEADER);
//        if (cid == null || cid.isBlank()) cid = Long.randomLong().toString();
//        MDC.put(HEADER, cid);
//        response.setHeader(HEADER, cid);
//        try {
//            filterChain.doFilter(request, response);
//        } finally {
//            MDC.remove(HEADER);
//        }
//    }
//}
