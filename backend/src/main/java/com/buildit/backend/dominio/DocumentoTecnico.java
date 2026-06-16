package com.buildit.backend.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "documenti_tecnici")
public class DocumentoTecnico extends Documento {

   @Column(nullable = false)
private String tipologia;

public String getTipologia() { return tipologia; }
public void setTipologia(String tipologia) { this.tipologia = tipologia; }   
}