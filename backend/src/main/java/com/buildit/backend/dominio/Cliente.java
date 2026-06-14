package com.buildit.backend.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "clienti")
public class Cliente extends Utente {

      @Override
    public String getRuolo() {
        return "CLIENTE";
    }

}