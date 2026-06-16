package com.buildit.backend.repository;

import com.buildit.backend.dominio.DocumentoContabile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentoContabileRepository extends JpaRepository<DocumentoContabile, Long> {
    List<DocumentoContabile> findByCantiereId(Long cantiereId);
}