package com.buildit.backend.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "fatture")
public class Fattura extends DocumentoContabile {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoFattura statoPagamento;

    public StatoFattura getStatoPagamento() { return statoPagamento; }
    public void setStatoPagamento(StatoFattura statoPagamento) { this.statoPagamento = statoPagamento; }
}