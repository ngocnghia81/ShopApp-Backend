package com.tripleng.shopappserver.filters;

import com.tripleng.shopappserver.Components.JwtTokenUltil;
import com.tripleng.shopappserver.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.internal.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${api.prefix}")
    private String prefixApi;

    private final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    private final JwtTokenUltil jwtTokenUltil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (isBypassToken(request)) {
                try {
                    logger.info("Begin bypass token: {}", request.getServletPath());
                    filterChain.doFilter(request, response);
                    logger.info("End bypass token: {}", request.getServletPath());
                    return;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                return;
            }

            final String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }

            String token = authorizationHeader.substring(7);
//            String requestPath = request.getServletPath();
//
//            // Nếu token hết hạn và đường dẫn là refresh-token thì cho phép tiếp tục để xử lý refresh token
//            if (requestPath.equals(prefixApi + "/users/refresh-token") && request.getMethod().equals("POST")) {
//                filterChain.doFilter(request, response);
//                return;
//            }

            // Kiểm tra nếu token chưa hết hạn thì thực hiện xác thực
            if (!jwtTokenUltil.isTokenExpired(token)) {
                String phoneNumber = jwtTokenUltil.extractPhoneNumber(token);
                if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = (User) userDetailsService.loadUserByUsername(phoneNumber);
                    if (jwtTokenUltil.isTokenValid(user, token)) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );
                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            } else {
                // Nếu token đã hết hạn và không phải là refresh-token endpoint, trả về lỗi
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }


    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        logger.info("Request path: " + request.getServletPath());

        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/healthcheck/health", prefixApi), "GET"),
                Pair.of(String.format("%s/actuator/**", prefixApi), "GET"),
                Pair.of(String.format("%s/users/refresh-token", prefixApi), "POST"),

                Pair.of(String.format("%s/roles", prefixApi), "GET"),
                Pair.of(String.format("%s/products", prefixApi), "GET"),
                Pair.of(String.format("%s/products/**", prefixApi), "GET"),
                Pair.of(String.format("%s/categories", prefixApi), "GET"),
                Pair.of(String.format("%s/users/register", prefixApi), "POST"),
                Pair.of(String.format("%s/users/details", prefixApi), "POST"),
                Pair.of(String.format("%s/users/login", prefixApi), "POST"),
//                Pair.of(String.format("%s/users/refresh-token", prefixApi), "POST"),

                Pair.of("/api-docs", "GET"),
                Pair.of("/api-docs/**", "GET"),
                Pair.of("/swagger-resources", "GET"),
                Pair.of("/swagger-resources/**", "GET"),
                Pair.of("/configuration/ui", "GET"),
                Pair.of("/configuration/security", "GET"),
                Pair.of("/swagger-ui/**", "GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/webjars/**", "GET"),
                Pair.of("/actuator/**", "GET"),
                Pair.of("/swapper-ui/index.html", "GET")
        );
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();
        if (requestPath.equals(String.format("%s/orders", prefixApi)) && requestMethod.equals("GET")) {
            return true;
        }

        for (Pair<String, String> bypassToken : bypassTokens) {
            String tokenPath = bypassToken.getLeft();
            String tokenMethod = bypassToken.getRight();

            if (tokenPath.contains("**")) {
                String regexPath = tokenPath.replace("**", ".*");

                Pattern pattern = Pattern.compile(regexPath);
                Matcher matcher = pattern.matcher(requestPath);
                if (matcher.matches() && requestMethod.equals(tokenMethod)) {
                    return true;
                }
            } else if (requestPath.equals(tokenPath) && requestMethod.equals(tokenMethod)) {
                return true;
            }
        }
        return false;
    }
}
