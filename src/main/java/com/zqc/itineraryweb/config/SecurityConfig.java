package com.zqc.itineraryweb.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf( csrf -> csrf
                        .ignoringRequestMatchers("/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .disable()
                )
                .authorizeHttpRequests( authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/user/**").permitAll()
                        .requestMatchers("/validate/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/neim/**").permitAll()
                        .requestMatchers("/tim/**").permitAll()
                        .requestMatchers("/oss/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(LogoutConfigurer::permitAll);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedOrigin("*");
        configuration.addExposedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}