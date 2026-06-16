package com.buildit.backend.repository;

import com.buildit.backend.dominio.FaseLavorativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FaseLavorativaRepository extends JpaRepository<FaseLavorativa, Long> {

    List<FaseLavorativa> findByCantiereId(Long cantiereId);

    // Restituisce le fasi di una squadra che si sovrappongono all'intervallo [inizio, fine],
    // escludendo la fase con id = excludeId (usare -1 per nuove fasi).
    @Query("SELECT f FROM FaseLavorativa f WHERE f.squadra.id = :squadraId AND f.id <> :excludeId " +
           "AND f.dataInizioPrevista <= :fine AND f.dataFinePrevista >= :inizio")
    List<FaseLavorativa> findOverlappingBySquadra(@Param("squadraId") Long squadraId,
                                                   @Param("excludeId") Long excludeId,
                                                   @Param("inizio") LocalDate inizio,
                                                   @Param("fine") LocalDate fine);
}