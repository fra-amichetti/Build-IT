package com.buildit.backend.repository;

import com.buildit.backend.dominio.FaseLavorativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FaseLavorativaRepository extends JpaRepository<FaseLavorativa, Long> {
    List<FaseLavorativa> findByCantiereId(Long cantiereId);
}