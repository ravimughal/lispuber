package pt.ul.fc.ptir.webtaxi.entity;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

@Entity
@Table(name = "motorista")
public class Motorista extends Utilizador {

    @Column(nullable = false, length = 255)
    private String rua;

    @Column(name = "rua_numero", nullable = false)
    private Integer ruaNumero;

    @Column(name = "codigo_postal", nullable = false, length = 15)
    private String codigoPostal;

    @Column(name = "num_carta_conducao", nullable = false, unique = true, length = 31)
    private String numCartaConducao;

    @Column(name = "validade_carta_conducao", nullable = false)
    private LocalDate validadeCartaConducao;



    public Motorista(){}

    public Motorista(String nif, String email, String senha, String nome,
                     Genero genero, LocalDate dataNascimento, String rua, Integer ruaNumero, 
                     String codigoPostal, String numCarta, LocalDate validadeCarta){
        super(nif, email, senha, nome, genero, dataNascimento);

        this.rua = rua;
        this.ruaNumero = ruaNumero;
        this.codigoPostal = codigoPostal;
        this.numCartaConducao = numCarta;
        this.validadeCartaConducao = validadeCarta;
    }



    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }


    public Integer getRuaNumero() {
        return ruaNumero;
    }

    public void setRuaNumero(Integer ruaNumero) {
        this.ruaNumero = ruaNumero;
    }


    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
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
}
