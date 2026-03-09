package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

@Entity
public class Reabastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Turno turno;

    @Column(nullable = false)
    private int litrosCombustivel;

    @Column(nullable = false)
    private int kmTaxi;

    @Embedded
    private Duracao duracao;

    @Column(nullable = false)
    private double eurosPagos;

    public Reabastecimento() {}

    public Reabastecimento(Long id, Turno turno, int litrosCombustivel, int kmTaxi, Duracao duracao, double eurosPagos) throws Exception {
        if (eurosPagos < 0) throw new Exception(); // TODO: mudar para exceção personalizada

        this.id = id;
        this.turno = turno;
        this.litrosCombustivel = litrosCombustivel;
        this.kmTaxi = kmTaxi;
        this.duracao = duracao;
        this.eurosPagos = eurosPagos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public int getLitrosCombustivel() {
        return litrosCombustivel;
    }

    public void setLitrosCombustivel(int litrosCombustivel) {
        this.litrosCombustivel = litrosCombustivel;
    }

    public int getKmTaxi() {
        return kmTaxi;
    }

    public void setKmTaxi(int kmTaxi) {
        this.kmTaxi = kmTaxi;
    }

    public Duracao getDuracao() {
        return duracao;
    }

    public void setDuracao(Duracao duracao) {
        this.duracao = duracao;
    }

    public double getEurosPagos() {
        return eurosPagos;
    }

    public void setEurosPagos(double eurosPagos) {
        this.eurosPagos = eurosPagos;
    }
}

