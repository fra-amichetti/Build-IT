package com.buildit.backend.dominio;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cantieri")
public class Cantiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String indirizzo;

    @Column(nullable = false)
    private LocalDate dataInizioPrevista;

    private LocalDate dataInizioEffettiva;

    @Column(nullable = false)
    private LocalDate dataFinePrevista;

    private LocalDate dataFineEffettiva;

    private String emailCliente;

   

     @Enumerated(EnumType.STRING)
   @Column(nullable = false)
private StatoCantiere stato;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public LocalDate getDataInizioPrevista() { return dataInizioPrevista; }
    public void setDataInizioPrevista(LocalDate d) { this.dataInizioPrevista = d; }

    public LocalDate getDataInizioEffettiva() { return dataInizioEffettiva; }
    public void setDataInizioEffettiva(LocalDate d) { this.dataInizioEffettiva = d; }

    public LocalDate getDataFinePrevista() { return dataFinePrevista; }
    public void setDataFinePrevista(LocalDate d) { this.dataFinePrevista = d; }

    public LocalDate getDataFineEffettiva() { return dataFineEffettiva; }
    public void setDataFineEffettiva(LocalDate d) { this.dataFineEffettiva = d; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }

    
    // 2. Aggiorna Getter e Setter
    public StatoCantiere getStato() { return stato; }
    public void setStato(StatoCantiere stato) { this.stato = stato; }

   
   
    public void iniziaLavori() {
    this.stato = StatoCantiere.IN_CORSO;
    this.dataInizioEffettiva = LocalDate.now();
}

public void terminaCantiere() {
    this.stato = StatoCantiere.TERMINATO;
    this.dataFineEffettiva = LocalDate.now();
}

public boolean verificaRitardo() {
    if (StatoCantiere.IN_CORSO != this.stato) return false;
    return LocalDate.now().isAfter(this.dataFinePrevista);
}
}