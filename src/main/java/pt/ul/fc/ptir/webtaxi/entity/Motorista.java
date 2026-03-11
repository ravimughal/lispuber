package pt.ul.fc.ptir.webtaxi.entity;


import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class Motorista extends Utilizador {

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String morada;

    @Column(nullable = false, unique = true)
    private String numCartaConducao;

    @Column(nullable = false)
    private LocalDate validadeCartaConducao;

    @OneToMany(mappedBy = "motorista")
    private Set<Turno> turnos;

    public Motorista(){}

    public Motorista(Integer nif, String email, String senha, String nome, Genero genero,
                     LocalDate dataNasc, String morada, String numCarta, LocalDate validadeCarta){
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

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
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
