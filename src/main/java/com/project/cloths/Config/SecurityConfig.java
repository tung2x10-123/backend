package com.project.cloths.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JWTFilter jwtFilter;

    public SecurityConfig(JWTFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


    @Configuration
    public class securityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests()
//                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("**").permitAll()
//                .requestMatchers("/h2-console/**").permitAll()
//                .requestMatchers("/api/products/**").permitAll()
//                .requestMatchers("/api/categories/**").permitAll()
//                .requestMatchers("/api/orders/**").permitAll()
//                .requestMatchers("/api/cart/**").permitAll()
//                .requestMatchers("/swagger-ui/**").permitAll()
//                .requestMatchers("/v3/api-docs**").permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Không dùng session
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}