package com.buildit.backend.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "dipendenti")
public class Dipendente extends Utente {

    @Column
    private String incarico;

    public String getIncarico() { return incarico; }
    public void setIncarico(String incarico) { this.incarico = incarico; }

      @Override
    public String getRuolo() {
        return "DIPENDENTE";
    }

}