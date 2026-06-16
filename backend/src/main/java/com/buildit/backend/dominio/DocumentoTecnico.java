package com.buildit.backend.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "documenti_tecnici")
public class DocumentoTecnico extends Documento {

   @Column(nullable = false)
private String tipologia;

public String getTipologia() { return tipologia; }
public void setTipologia(String tipologia) { this.tipologia = tipologia; }

@Override
public boolean validaEstensione(String nomeFile) {
    if (nomeFile == null) return false;
    String lower = nomeFile.toLowerCase();
    return lower.endsWith(".pdf") || lower.endsWith(".jpg") || lower.endsWith(".png");
}
}