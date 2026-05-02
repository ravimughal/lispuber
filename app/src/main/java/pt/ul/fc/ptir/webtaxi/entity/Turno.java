package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;

import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;

@Entity
@Table(name = "turno")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Motorista motorista;

    @ManyToOne
    @JoinColumn
    private Taxi taxi;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "inicio", column = @Column(name = "inicio", nullable = false)),
        @AttributeOverride(name = "fim", column = @Column(name = "fim", nullable = false))
    })
    private Duracao duracao;



    public Turno() {}

    public Turno(Duracao duracao, Motorista motorista, Taxi taxi) {
        this.duracao = duracao;
        this.motorista = motorista;
        this.taxi = taxi;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Duracao getDuracao() {
        return duracao;
    }

    public void setDuracao(Duracao duracao) {
        this.duracao = duracao;
    }


    public Motorista getMotorista() {
        return motorista;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }


    public Taxi getTaxi() {
        return taxi;
    }

    public void setTaxi(Taxi taxi) {
        this.taxi = taxi;
    }
}
