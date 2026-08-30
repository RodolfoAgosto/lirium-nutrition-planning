package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.exception.CustomAccessDeniedHandler;
import com.lirium.nutrition.exception.CustomAuthenticationEntryPoint;
import com.lirium.nutrition.infrastructure.security.CustomOAuth2UserService;
import com.lirium.nutrition.infrastructure.security.JwtAuthenticationFilter;
import com.lirium.nutrition.infrastructure.security.OAuth2LoginSuccessHandler;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_NUTRITIONIST = "NUTRITIONIST";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers("/images/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Specific GET permissions (Allows PATIENT access before blocking write operations)
                        .requestMatchers(HttpMethod.GET, "/api/plan-food-portions/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/plan-meals/**").authenticated()

                        // Only ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole(ROLE_ADMIN)
                        .requestMatchers("/api/users/*/enabled").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE,"/api/nutrition-plan-templates/**").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/foods/**").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/foods/**").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/foods/**").hasRole(ROLE_ADMIN)

                        // ADMIN and NUTRITIONIST
                        .requestMatchers("/api/users/**").hasAnyRole(ROLE_ADMIN, ROLE_NUTRITIONIST)
                        .requestMatchers("/api/restrictions/**").hasAnyRole(ROLE_ADMIN, ROLE_NUTRITIONIST)
                        .requestMatchers("/api/plan-meals", "/api/plan-meals/**").hasAnyRole(ROLE_ADMIN, ROLE_NUTRITIONIST)
                        .requestMatchers("/api/nutrition-plan-templates/**").hasAnyRole(ROLE_ADMIN, ROLE_NUTRITIONIST)
                        .requestMatchers(HttpMethod.POST, "/api/nutrition-plans/**").hasAnyRole(ROLE_ADMIN, ROLE_NUTRITIONIST)
                        .requestMatchers(HttpMethod.PATCH, "/api/nutrition-plans/**").hasAnyRole(ROLE_ADMIN, ROLE_NUTRITIONIST)
                        .requestMatchers("/api/plan-food-portions/**").hasAnyRole(ROLE_ADMIN, ROLE_NUTRITIONIST)

                        // MULTI-ROLE READING (Properly authenticated in Controller/Service)
                        .requestMatchers("/api/patients/**").authenticated()
                        .requestMatchers("/api/nutrition-plans/**").authenticated()
                        .requestMatchers("/api/daily-records/**").authenticated()
                        .requestMatchers("/api/foods/**").authenticated()
                        .requestMatchers("/api/users/*/validate-email").authenticated()

                        .anyRequest().authenticated()

                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                   .userInfoEndpoint(userInfo -> userInfo
                              .userService(customOAuth2UserService)
                   )
                   .successHandler(oAuth2LoginSuccessHandler)
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsServiceImpl);
        return provider;

    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}