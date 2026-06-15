package com.buildit.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildit.backend.dominio.Cantiere;
import com.buildit.backend.dominio.StatoCantiere;

@Repository
public interface CantiereRepository extends JpaRepository<Cantiere, Long> {
    List<Cantiere> findByEmailCliente(String emailCliente);
    List<Cantiere> findByStato(StatoCantiere stato);
}