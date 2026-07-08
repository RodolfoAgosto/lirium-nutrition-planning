package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.exception.CustomAccessDeniedHandler;
import com.lirium.nutrition.exception.CustomAuthenticationEntryPoint;
import com.lirium.nutrition.infrastructure.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
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
import com.lirium.nutrition.infrastructure.security.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

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
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/api/auth/**", "/oauth2/**","/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                        // Only ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/*/enabled").hasRole("ADMIN")
                        .requestMatchers("/api/users/*/validate-email").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/nutrition-plan-templates/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/foods/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/foods/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/foods/**").hasRole("ADMIN")

                        // ADMIN and NUTRITIONIST
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "NUTRITIONIST")
                        .requestMatchers("/api/restrictions/**").hasAnyRole("ADMIN", "NUTRITIONIST")
                        .requestMatchers("/api/plan-meals/**").hasAnyRole("ADMIN", "NUTRITIONIST")
                        .requestMatchers("/api/plan-food-portions/**").hasAnyRole("ADMIN", "NUTRITIONIST")
                        .requestMatchers("/api/nutrition-plan-templates/**").hasAnyRole("ADMIN", "NUTRITIONIST")
                        .requestMatchers(HttpMethod.POST, "/api/nutrition-plans/**").hasAnyRole("ADMIN", "NUTRITIONIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/nutrition-plans/**").hasAnyRole("ADMIN", "NUTRITIONIST")

                        // ADMIN, NUTRITIONIST and PATIENT (ownership in controller)
                        .requestMatchers("/patients/**").authenticated()
                        .requestMatchers("/api/nutrition-plans/**").authenticated()
                        .requestMatchers("/api/daily-records/**").authenticated()
                        .requestMatchers("/api/foods/**").authenticated()

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