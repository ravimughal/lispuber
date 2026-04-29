package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;

@Entity
@Table(name = "reabastecimento")
public class Reabastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "inicio", column = @Column(name = "data_inicio", nullable = false)),
        @AttributeOverride(name = "fim", column = @Column(name = "data_fim", nullable = true))
    })
    private Duracao duracao;

    @Column(nullable = false)
    private Double quantidade;

    @Column(name = "euros_pagos", nullable = false)
    private Double eurosPagos;

    @Column(nullable = false)
    private Double kilometros;



    public Reabastecimento() {}

    public Reabastecimento(Double quantidade, Double kilometros, Double eurosPagos,
                           Duracao duracao, Turno turno) {
        this.quantidade = quantidade;
        this.kilometros = kilometros;
        this.eurosPagos = eurosPagos;
        this.duracao = duracao;
        this.turno = turno;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }


    public Double getKilometros() {
        return kilometros;
    }

    public void setKilometros(Double kilometros) {
        this.kilometros = kilometros;
    }


    public Double getEurosPagos() {
        return eurosPagos;
    }

    public void setEurosPagos(Double eurosPagos) {
        this.eurosPagos = eurosPagos;
    }


    public Duracao getDuracao() {
        return duracao;
    }

    public void setDuracao(Duracao duracao) {
        this.duracao = duracao;
    }


    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }
}
