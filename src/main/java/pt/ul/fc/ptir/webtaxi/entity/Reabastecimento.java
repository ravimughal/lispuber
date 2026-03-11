package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

@Entity
public class Reabastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Double qtdCombustivel;

    @Column(nullable = false)
    private Double kmPercorridos;

    @Column(nullable = false)
    private Double eurosPagos;

    @Column(nullable = false)
    private Duracao duracao;

    @ManyToOne
    @JoinColumn(name = "turno_id")
    private Turno turno;

    public Reabastecimento() {}

    public Reabastecimento(Long id, Double qtdCombustivel, Double kmPercorridos, Double eurosPagos, Duracao duracao, Turno turno) {
        this.id = id;
        this.qtdCombustivel = qtdCombustivel;
        this.kmPercorridos = kmPercorridos;
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

    public Double getQtdCombustivel() {
        return qtdCombustivel;
    }

    public void setQtdCombustivel(Double qtdCombustivel) {
        this.qtdCombustivel = qtdCombustivel;
    }

    public Double getKmPercorridos() {
        return kmPercorridos;
    }

    public void setKmPercorridos(Double kmPercorridos) {
        this.kmPercorridos = kmPercorridos;
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
