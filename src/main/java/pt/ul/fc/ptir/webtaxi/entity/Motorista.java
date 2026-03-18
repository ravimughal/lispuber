package pt.ul.fc.ptir.webtaxi.entity;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Morada;
import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

@Entity
public class Motorista extends Utilizador {

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private Morada morada;

    @Column(nullable = false, unique = true)
    private String numCartaConducao;

    @Column(nullable = false)
    private LocalDate validadeCartaConducao;

    // TODO: ver se é necessário e/ou se faz sentido
    @OneToMany(mappedBy = "motorista")
    private Set<Turno> turnos;

    public Motorista(){}

    public Motorista(Integer nif, String email, String senha, String nome, Genero genero,
                     LocalDate dataNasc, Morada morada, String numCarta, LocalDate validadeCarta){
        super(nif, email, senha, nome, genero);

        this.dataNascimento = dataNasc;
        this.morada = morada;
        this.numCartaConducao = numCarta;
        this.validadeCartaConducao = validadeCarta;
        this.turnos = new HashSet<>();
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Morada getMorada() {
        return morada;
    }

    public void setMorada(Morada morada) {
        this.morada = morada;
    }

    public String getNumCartaConducao() {
        return numCartaConducao;
    }

    public void setNumCartaConducao(String numCartaConducao) {
        this.numCartaConducao = numCartaConducao;
    }

    public LocalDate getValidadeCartaConducao() {
        return validadeCartaConducao;
    }

    public void setValidadeCartaConducao(LocalDate validadeCartaConducao) {
        this.validadeCartaConducao = validadeCartaConducao;
    }

    public Set<Turno> getTurnos() {
        return turnos;
    }

    public void setTurnos(Set<Turno> turnos) {
        this.turnos = turnos;
    }
}
