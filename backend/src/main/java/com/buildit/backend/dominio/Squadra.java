package com.buildit.backend.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "squadre")
public class Squadra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private int numeroComponenti;

    @Column(nullable = false)
    private String nomeReferente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Specializzazione specializzazione;

    @OneToMany(mappedBy = "squadra")
    private List<FaseLavorativa> fasiDelCantiere;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getNumeroComponenti() { return numeroComponenti; }
    public void setNumeroComponenti(int numeroComponenti) { this.numeroComponenti = numeroComponenti; }

    public String getNomeReferente() { return nomeReferente; }
    public void setNomeReferente(String nomeReferente) { this.nomeReferente = nomeReferente; }

    public Specializzazione getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(Specializzazione specializzazione) { this.specializzazione = specializzazione; }

    public List<FaseLavorativa> getFasiDelCantiere() { return fasiDelCantiere; }
    public void setFasiDelCantiere(List<FaseLavorativa> fasi) { this.fasiDelCantiere = fasi; }

    public boolean isDisponibile(LocalDate richiestaInizio, LocalDate richiesteFine) {
        if (fasiDelCantiere == null) return true;
        return fasiDelCantiere.stream()
            .filter(f -> f.getStato() != StatoFase.TERMINATA)
            .noneMatch(f ->
                !richiestaInizio.isAfter(f.getDataFinePrevista()) &&
                !richiesteFine.isBefore(f.getDataInizioPrevista())
            );
    }
}