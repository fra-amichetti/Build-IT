package com.buildit.backend.repository;

import com.buildit.backend.dominio.Squadra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SquadraRepository extends JpaRepository<Squadra, Long> {
    Optional<Squadra> findByNome(String nome);
}