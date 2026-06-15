package com.buildit.backend.dominio;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fasi_lavorative")
public class FaseLavorativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descrizione;

    @Column(nullable = false)
    private LocalDate dataInizioPrevista;

    private LocalDate dataInizioEffettiva;

    @Column(nullable = false)
    private LocalDate dataFinePrevista;

    private LocalDate dataFineEffettiva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoFase stato;

    @ManyToOne
    @JoinColumn(name = "cantiere_id", nullable = false)
    private Cantiere cantiere;

    @ManyToOne
    @JoinColumn(name = "squadra_id")
    private Squadra squadra;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public LocalDate getDataInizioPrevista() { return dataInizioPrevista; }
    public void setDataInizioPrevista(LocalDate d) { this.dataInizioPrevista = d; }

    public LocalDate getDataInizioEffettiva() { return dataInizioEffettiva; }
    public void setDataInizioEffettiva(LocalDate d) { this.dataInizioEffettiva = d; }

    public LocalDate getDataFinePrevista() { return dataFinePrevista; }
    public void setDataFinePrevista(LocalDate d) { this.dataFinePrevista = d; }

    public LocalDate getDataFineEffettiva() { return dataFineEffettiva; }
    public void setDataFineEffettiva(LocalDate d) { this.dataFineEffettiva = d; }

    public StatoFase getStato() { return stato; }
    public void setStato(StatoFase stato) { this.stato = stato; }

    public Cantiere getCantiere() { return cantiere; }
    public void setCantiere(Cantiere cantiere) { this.cantiere = cantiere; }

    public Squadra getSquadra() { return squadra; }
    public void setSquadra(Squadra squadra) { this.squadra = squadra; }

    public void avviaFase() {
        this.stato = StatoFase.IN_CORSO;
        this.dataInizioEffettiva = LocalDate.now();
    }

    public void terminaFase() {
        this.stato = StatoFase.TERMINATA;
        this.dataFineEffettiva = LocalDate.now();
    }
}