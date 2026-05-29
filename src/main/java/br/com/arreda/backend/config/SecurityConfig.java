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

    // Este Bean diz ao Spring quais rotas são públicas e quais são bloqueadas
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Desativa proteção CSRF (necessário para APIs REST)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/usuarios").permitAll() // Libera geral a sua rota de cadastro
                        .anyRequest().authenticated() // Bloqueia todo o resto
                );
        return http.build();
    }
}