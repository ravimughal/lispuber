package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Morada;

@Entity
public class Viagem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="rua", column=@Column(name="origem_rua")),
            @AttributeOverride(name="numero", column=@Column(name="origem_numero")),
            @AttributeOverride(name="complemento", column=@Column(name="origem_complemento")),
            @AttributeOverride(name="codigoPostal", column=@Column(name="origem_codigo_postal")),
    })
    private Morada moradaEntrada;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="rua", column=@Column(name="destino_rua")),
            @AttributeOverride(name="numero", column=@Column(name="destino_numero")),
            @AttributeOverride(name="complemento", column=@Column(name="destino_complemento")),
            @AttributeOverride(name="codigoPostal", column=@Column(name="destino_codigo_postal")),
    })
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

    @ManyToOne @JoinColumn
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "turno_id")
    private Turno turno;

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

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }


}
