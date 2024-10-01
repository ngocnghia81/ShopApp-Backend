package com.tripleng.shopappserver.configs;

import com.tripleng.shopappserver.filters.JwtTokenFilter;
import com.tripleng.shopappserver.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> {
                    requests
                            .requestMatchers(
                                    String.format("%s/users/register", apiPrefix),
                                    String.format("%s/users/refresh-token", apiPrefix),
                                    String.format("%s/users/login", apiPrefix),
                                    String.format("%s/users/details", apiPrefix),
                                    String.format("%s/healthcheck/**", apiPrefix),
                                    String.format("%s/actuator/**", apiPrefix),

                                    "/api-docs",
                                    "/api-docs/**",
                                    "/swagger-resources/**",
                                    "/swagger-resources",
                                    "/configuration/ui",
                                    "/configuration/security",
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/webjars/**",
                                    "/actuator/**",
                                    "/swapper-ui/index.html"

                            )
                            .permitAll()

                            .requestMatchers(HttpMethod.GET,
                                    String.format("%s/roles**", apiPrefix)).permitAll()

                            .requestMatchers(HttpMethod.GET,
                                    String.format("%s/categories**", apiPrefix)).permitAll()

                            .requestMatchers(HttpMethod.POST,
                                    String.format("%s/categories/**", apiPrefix)).hasAnyRole(Role.ADMIN)

//                            .requestMatchers(HttpMethod.POST,
//                                    String.format("%s/users/refresh-token", apiPrefix)).permitAll()


                            .requestMatchers(HttpMethod.PUT,
                                    String.format("%s/categories/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                            .requestMatchers(HttpMethod.DELETE,
                                    String.format("%s/categories/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                            .requestMatchers(HttpMethod.GET,
                                    String.format("%s/products**", apiPrefix)).permitAll()
                            .requestMatchers(HttpMethod.GET,
                                    String.format("%s/products/**", apiPrefix)).permitAll()

                            .requestMatchers(HttpMethod.GET,
                                    String.format("%s/products/images/**", apiPrefix)).permitAll()

                            .requestMatchers(HttpMethod.POST,
                                    String.format("%s/products**", apiPrefix)).hasAnyRole(Role.ADMIN)

                            .requestMatchers(HttpMethod.PUT,
                                    String.format("%s/products/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                            .requestMatchers(HttpMethod.DELETE,
                                    String.format("%s/products/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                            .requestMatchers(HttpMethod.POST,
                                    String.format("%s/orders/**", apiPrefix)).hasAnyRole(Role.USER)

                            .requestMatchers(HttpMethod.GET,
                                    String.format("%s/orders/**", apiPrefix)).permitAll()

                            .requestMatchers(HttpMethod.PUT,
                                    String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)

                            .requestMatchers(HttpMethod.DELETE,
                                    String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)

                            .requestMatchers(HttpMethod.GET,
                                    String.format("%s/orders/get-orders-by-keyword", apiPrefix)).permitAll()

                            .requestMatchers(HttpMethod.POST,
                                    String.format("%s/order_details/**", apiPrefix)).hasAnyRole(Role.USER)

                            .requestMatchers(HttpMethod.GET,
                                    String.format("%s/order_details/**", apiPrefix)).permitAll()

                            .requestMatchers(HttpMethod.PUT,
                                    String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)

                            .requestMatchers(HttpMethod.DELETE,
                                    String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)

                            .anyRequest().authenticated();
                });

//        http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
//            @Override
//            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
//                CorsConfiguration corsConfiguration = new CorsConfiguration();
//                corsConfiguration.setAllowedOrigins(
//                        List.of("http://localhost:4200", "**")
//                );
//                corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                corsConfiguration.setAllowCredentials(true);
//                corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-type", "x-auth-token"));
//                corsConfiguration.setExposedHeaders(List.of("x-auth-token"));
//                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//                source.registerCorsConfiguration("/**", corsConfiguration);
//                httpSecurityCorsConfigurer.configurationSource(source);
//            }
//        });
        return http.build();
    }
}
