package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.*;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;

import java.util.Set;

@Entity
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Embedded
    private Duracao duracao;

    @ManyToOne @JoinColumn(nullable = false)
    private Motorista motorista;

    @ManyToOne @JoinColumn(nullable = false)
    private Taxi taxi;

    // TODO: ver se é necessário e/ou se faz sentido
    @OneToMany(mappedBy = "turno")
    private Set<Reabastecimento> reabastecimentos;

    // TODO: ver se é necessário e/ou se faz sentido
    @OneToMany(mappedBy = "turno")
    private Set<Viagem> viagens;

    public Turno() {}

    public Turno(Long id, Duracao duracao, Motorista motorista, Taxi taxi, Set<Reabastecimento> reabastecimentos, Set<Viagem> viagens) {
        this.id = id;
        this.duracao = duracao;
        this.motorista = motorista;
        this.taxi = taxi;
        this.reabastecimentos = reabastecimentos;
        this.viagens = viagens;
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

    public Set<Reabastecimento> getReabastecimentos() {
        return reabastecimentos;
    }

    public void setReabastecimentos(Set<Reabastecimento> reabastecimentos) {
        this.reabastecimentos = reabastecimentos;
    }

    public Set<Viagem> getViagens() {
        return viagens;
    }

    public void setViagens(Set<Viagem> viagens) {
        this.viagens = viagens;
    }
}
