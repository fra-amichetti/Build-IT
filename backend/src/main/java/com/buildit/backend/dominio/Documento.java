package com.buildit.backend.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "documenti")
public abstract class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "cantiere_id", nullable = false)
    private Cantiere cantiere;

    @ManyToOne
    @JoinColumn(name = "fase_id")
    private FaseLavorativa fase;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Cantiere getCantiere() { return cantiere; }
    public void setCantiere(Cantiere cantiere) { this.cantiere = cantiere; }

    public FaseLavorativa getFase() { return fase; }
    public void setFase(FaseLavorativa fase) { this.fase = fase; }

    // Polimorfismo: ogni sottoclasse definisce i formati ammessi.
    public abstract boolean validaEstensione(String nomeFile);
}