package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Morada;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="rua", column=@Column(name="origem_rua")),
            @AttributeOverride(name="numero", column=@Column(name="origem_numero")),
            @AttributeOverride(name="complemento", column=@Column(name="origem_complemento")),
            @AttributeOverride(name="codigoPostal", column=@Column(name="origem_codigo_postal")),
    })
    private Morada moradaInicial;

    @Column(nullable = false)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="rua", column=@Column(name="destino_rua")),
            @AttributeOverride(name="numero", column=@Column(name="destino_numero")),
            @AttributeOverride(name="complemento", column=@Column(name="destino_complemento")),
            @AttributeOverride(name="codigoPostal", column=@Column(name="destino_codigo_postal")),
    })
    private Morada moradaFinal;

    @Column(nullable = false)
    private Integer numPassageiros;

    @ManyToOne @JoinColumn(nullable = false)
    private Conforto conforto;

    @ManyToOne @JoinColumn
    private Cliente cliente;

    public Pedido() {}

    public Pedido(Long id, Morada moradaInicial, Morada moradaFinal, Integer numPassageiros, Conforto conforto, Cliente cliente) {
        this.id = id;
        this.moradaInicial = moradaInicial;
        this.moradaFinal = moradaFinal;
        this.numPassageiros = numPassageiros;
        this.conforto = conforto;
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Morada getMoradaInicial() {
        return moradaInicial;
    }

    public void setMoradaInicial(Morada moradaInicial) {
        this.moradaInicial = moradaInicial;
    }

    public Morada getMoradaFinal() {
        return moradaFinal;
    }

    public void setMoradaFinal(Morada moradaFinal) {
        this.moradaFinal = moradaFinal;
    }

    public Integer getNumPassageiros() {
        return numPassageiros;
    }

    public void setNumPassageiros(Integer numPassageiros) {
        this.numPassageiros = numPassageiros;
    }

    public Conforto getConforto() {
        return conforto;
    }

    public void setConforto(Conforto conforto) {
        this.conforto = conforto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
