package com.buildit.backend.config;

import com.buildit.backend.dominio.Amministratore;
import com.buildit.backend.repository.UtenteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(UtenteRepository utenteRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (!utenteRepository.existsByEmail("admin1@buildit.it")) {
                Amministratore admin = new Amministratore();
                admin.setNome("Marco");
                admin.setCognome("Rossi");
                admin.setEmail("admin1@buildit.it");
                admin.setHashPassword(passwordEncoder.encode("Admin1234!"));
                admin.setNomeAzienda("BuildIT2 Srl");
                utenteRepository.save(admin);
                System.out.println("Admin creato!");
            }
        };
    }
}