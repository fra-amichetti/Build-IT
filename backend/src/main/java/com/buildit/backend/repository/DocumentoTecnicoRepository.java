package com.buildit.backend.repository;

import com.buildit.backend.dominio.DocumentoTecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentoTecnicoRepository extends JpaRepository<DocumentoTecnico, Long> {
    List<DocumentoTecnico> findByCantiereId(Long cantiereId);
}