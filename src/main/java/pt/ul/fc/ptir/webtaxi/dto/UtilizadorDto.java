package pt.ul.fc.ptir.webtaxi.dto;
import pt.ul.fc.ptir.webtaxi.entity.Genero;;
public abstract class UtilizadorDto {

    private Long id;

    private Integer nif;

    private String email;

    private String senha;

    private String nome;

    private Genero genero;

    public UtilizadorDto(){}

    public UtilizadorDto(Integer nif, String email, String senha, String nome, Genero genero){
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