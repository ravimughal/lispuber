package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Morada;

import java.time.LocalDateTime;

@Entity
public class Viagem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Morada moradaEntrada;
    private Morada moradaSaida;

    private Duracao duracao;

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

    public Viagem(Morada moradaEntrada, Morada moradaSaida, Duracao duracao,Double kmPercorridos, Integer numPassageiro, Double preco) {
        this.moradaEntrada = moradaEntrada;
        this.moradaSaida = moradaSaida;
        this.duracao = duracao;
        this.kmPercorridos = kmPercorridos;
        this.numPassageiro = numPassageiro;
        this.preco = preco;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}
    public Morada getMoradaEntrada() {
        return moradaEntrada;
    }

    public void setMoradaEntrada(Morada moradaEntrada) {
        this.moradaEntrada = moradaEntrada;
    }

    public Morada getMoradaSaida() {
        return moradaSaida;
    }

    public void setMoradaSaida(Morada moradaSaida) {
        this.moradaSaida = moradaSaida;
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

    public Duracao getDuracao() {
        return duracao;
    }

    public void setDuracao(Duracao duracao) {
        this.duracao = duracao;
    }
}
