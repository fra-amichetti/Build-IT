package com.buildit.backend.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "amministratori")
public class Amministratore extends Utente {

    @Column
    private String nomeAzienda;

    public String getNomeAzienda() { return nomeAzienda; }
    public void setNomeAzienda(String nomeAzienda) { this.nomeAzienda = nomeAzienda; }

      @Override
    public String getRuolo() {
        return "AMMINISTRATORE";
    }

}