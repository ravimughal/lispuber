package pt.ul.fc.ptir.webtaxi.entity;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilizador {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true,nullable=false)
    private Integer nif;

    @Column(unique = true, nullable=false)
    private String email;

    @Column(nullable=false)
    private String senha;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Genero genero;


    public Utilizador(){}

    public Utilizador(Integer nif, String email, String senha, String nome, Genero genero){
        this.nif=nif;
        this.email=email;
        this.senha=senha;
        this.nome=nome;
        this.genero=genero;
        
    }


    public Long getId() {
        return this.id;
    }
    public Integer getNif() {
        return this.nif;
    }
    public String getEmail(){
        return this.email;
    }

    public String getSenha(){
        return this.senha;
    }

    public String getNome(){
        return this.nome;
    }
    
    public Genero getGenero(){
        return this.genero;
    }


    public void setId(Long id) {
        this.id=id;
    }

    public void setNif(Integer newNif) {
        this.nif = newNif;
    }

    public void setEmail(String newEmail) {
        this.email = newEmail;
    }   

    public void setSenha(String newSenha) {
        this.senha = newSenha;
    }

    public void setName(String newNome) {
        this.nome = newNome;
    }

    public void setGenero(Genero newGenero){
        this.genero=newGenero;
    }

}