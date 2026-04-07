package com.grummans.noyblog.configuration;

import com.grummans.noyblog.repository.UsersRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll() // public for login
                        .requestMatchers("/c/**").permitAll() // public for client
                        .requestMatchers("/a/**").authenticated() // authenticated for admin
                        .anyRequest().authenticated() // all other requests require authentication
        ).formLogin(form -> form
                .loginProcessingUrl("/auth/login")
                .successHandler((request, response, authentication) ->
                        response.setStatus(HttpServletResponse.SC_OK))
                .failureHandler((request, response, exception) ->
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                .permitAll()
        ).logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        ).exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        ).sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
        ).csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsersRepository usersRepository) {
        return username -> {
            var user = usersRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles("ADMIN")
                    .build();
        };
    }
}
