package com.buildit.backend.dominio;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "documenti_contabili")
public abstract class DocumentoContabile extends Documento {

    @Column(nullable = false)
    private double importo;

    public double getImporto() { return importo; }
    public void setImporto(double importo) { this.importo = importo; }

    @Override
    public boolean validaEstensione(String nomeFile) {
        if (nomeFile == null) return false;
        return nomeFile.toLowerCase().endsWith(".pdf");
    }
}