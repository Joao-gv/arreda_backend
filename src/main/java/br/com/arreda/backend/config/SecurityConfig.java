package br.com.arreda.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    // @Bean avisa ao Spring: "Sempre que alguém pedir um PasswordEncoder, use este método"
    @Bean
    public PasswordEncoder passwordEncoder() {
        // O BCrypt é o algoritmo padrão de mercado para criar o Hash da senha
        return new BCryptPasswordEncoder();
    }
}