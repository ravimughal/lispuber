package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;

@Entity
@Table(name = "viagem")
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    private Duracao duracao;

    @Column(name = "km_percorridos", nullable = false)
    private Double kilometros;



    public Viagem() {}

    public Viagem(Cliente cliente, Turno turno, Pedido pedido,
                  Duracao duracao, Double kilometros, Integer numPassageiro) {

        this.cliente = cliente;
        this.turno = turno;
        this.pedido = pedido;
        this.duracao = duracao;
        this.kilometros = kilometros;
    }



    public Long getId() {return id;}

    public void setId(Long id) {
        this.id = id;
    }
    

    public Double getKilometros() {
        return kilometros;
    }

    public void setKilometros(Double kilometros) {
        this.kilometros = kilometros;
    }


    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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
