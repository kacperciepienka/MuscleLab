package pl.kacper.musclelab.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/h2-console/**").permitAll()

                        // publiczne
                        .requestMatchers(HttpMethod.POST, "/api/users/clients").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/coaches").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users/coaches").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/training-slots/client").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/training-slots/client/**").permitAll()

                        // tylko COACH
                        .requestMatchers(HttpMethod.GET, "/api/users/clients").hasRole("COACH")

                        .requestMatchers(HttpMethod.POST, "/api/training-slots").hasRole("COACH")
                        .requestMatchers(HttpMethod.PUT, "/api/training-slots/cancel").hasRole("COACH")
                        .requestMatchers(HttpMethod.PUT, "/api/training-slots/date").hasRole("COACH")
                        .requestMatchers(HttpMethod.GET, "/api/training-slots/coach").hasRole("COACH")

                        .requestMatchers(HttpMethod.GET, "/api/reservations/coach").hasRole("COACH")
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/coach/cancel").hasRole("COACH")
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/coach/complete").hasRole("COACH")

                        // tylko CLIENT
                        .requestMatchers(HttpMethod.POST, "/api/reservations").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/client").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/client/cancel").hasRole("CLIENT")


                        // reszta wymaga zalogowania
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider())
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:63342",
                "http://127.0.0.1:63342"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}