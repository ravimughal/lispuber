package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Viagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String moradaEntrada;
    private String moradaSaida;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSaida;
    private Double kmPercorridos;
    private Integer numPassageiro;
    private Double preco;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne(mappedBy = "viagem")
    private Fatura fatura;

    // TODO: 1 viagem deriva de 1 pedido.

    public Viagem() {}

    public Viagem(String moradaEntrada, String moradaSaida, LocalDateTime horaEntrada, LocalDateTime horaSaida,Double kmPercorridos, Integer numPassageiro, Double preco) {
        this.moradaEntrada = moradaEntrada;
        this.moradaSaida = moradaSaida;
        this.horaEntrada = horaEntrada;
        this.horaSaida = horaSaida;
        this.kmPercorridos = kmPercorridos;
        this.numPassageiro = numPassageiro;
        this.preco = preco;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}
    public String getMoradaEntrada() {
        return moradaEntrada;
    }

    public void setMoradaEntrada(String moradaEntrada) {
        this.moradaEntrada = moradaEntrada;
    }

    public String getMoradaSaida() {
        return moradaSaida;
    }

    public void setMoradaSaida(String moradaSaida) {
        this.moradaSaida = moradaSaida;
    }

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalDateTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalDateTime getHoraSaida() {
        return horaSaida;
    }

    public void setHoraSaida(LocalDateTime horaSaida) {
        this.horaSaida = horaSaida;
    }

    public Double getKmPercorridos() {
        return kmPercorridos;
    }

    public void setKmPercorridos(Double kmPercorridos) {
        this.kmPercorridos = kmPercorridos;
    }

    public Integer getNumPassageiro() {
        return numPassageiro;
    }

    public void setNumPassageiro(Integer numPassageiro) {
        this.numPassageiro = numPassageiro;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Fatura getFatura() {
        return fatura;
    }

    public void setFatura(Fatura fatura) {
        this.fatura = fatura;
    }
}
