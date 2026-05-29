package br.com.arreda.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Habilita as configurações de segurança web
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desabilita proteção contra CSRF (comum em APIs REST)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permite QUALQUER requisição (Swagger, Controllers, etc.)
                )
                .formLogin(form -> form.disable()) // Desativa essa tela de login da imagem
                .httpBasic(basic -> basic.disable()); // Desativa o pop-up de login básico

        return http.build();
    }
}