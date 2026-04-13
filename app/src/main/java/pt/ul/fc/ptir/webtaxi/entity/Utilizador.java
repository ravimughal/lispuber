package pt.ul.fc.ptir.webtaxi.entity;

import java.time.LocalDate;
import jakarta.persistence.*;

import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

@Entity
@Table(name = "utilizador")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable=false, length = 9)
    private String nif;

    @Column(unique = true, nullable=false, length = 255)
    private String email;

    @Column(nullable=false, length = 255)
    private String senha;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Genero genero;


    public Utilizador(){}

    public Utilizador(String nif, String email, String senha, String nome, Genero genero, LocalDate dataNascimento){
        this.nif = nif;
        this.email = email;
        this.senha = senha;
        this.nome = nome;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id=id;
    }

    public String getNif() {
        return this.nif;
    }

    public void setNif(String newNif) {
        this.nif = newNif;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String newEmail) {
        this.email = newEmail;
    }   

    public String getSenha(){
        return this.senha;
    }

    public void setSenha(String newSenha) {
        this.senha = newSenha;
    }

    public String getNome(){
        return this.nome;
    }

    public void setName(String newNome) {
        this.nome = newNome;
    }
    
    public Genero getGenero(){
        return this.genero;
    }

    public void setGenero(Genero newGenero){
        this.genero=newGenero;
    }

    public LocalDate getDataNascimento() {
        return this.dataNascimento;
    }

    public void setDataNascimento(LocalDate newDataNascimento) {
        this.dataNascimento = newDataNascimento;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

}