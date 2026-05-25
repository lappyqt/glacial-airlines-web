package com.lappyqt.glacialairlines.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.remember-me-key}")
    private String rememberMeKey;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers("/dist/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/search", "/auth/create", "/booking/return-flight", "/loyalty-program").permitAll()
                .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/auth/login").permitAll()
                        .defaultSuccessUrl("/"))
                .rememberMe(rememberMe -> rememberMe
                        .key(rememberMeKey)
                        .tokenValiditySeconds(86400 * 7)
                        .alwaysRemember(true))
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }
}
