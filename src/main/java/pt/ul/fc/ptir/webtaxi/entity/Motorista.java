package pt.ul.fc.ptir.webtaxi.entity;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Motorista extends Utilizador{

    @Column(nullable=false)
    private Date data_nascimento;

    @Column(nullable=false)
    private String morada;

    @Column(nullable=false)
    private String num_carta_conduc;

    @Column(nullable=false)
    private Date validade_carta_conduc;


    public Motorista(){}

    public Motorista(Integer nif, String email, String senha, String nome, Genero genero,
                    Date data_nasc, String morada, String num_carta, Date validade_carta){
        super(nif,email,senha,nome,genero);

        this.data_nascimento=data_nasc;
        this.morada=morada;
        this.num_carta_conduc=num_carta;    
        this.validade_carta_conduc=validade_carta;
    }
    
    public Date getDataNascimento(){
        return this.data_nascimento;
    }

    public String getMorada(){
        return this.morada;
    }

    public String getNumCarta(){
        return this.num_carta_conduc;
    }
    public Date getValidadeCarta(){
        return this.validade_carta_conduc;
    }


    public void setDataNascimento(Date newData){
        this.data_nascimento=newData;
    }

    public void getMorada(String newMorada){
        this.morada=newMorada;
    }

    public void getNumCarta(String newNumCarta){
        this.num_carta_conduc=newNumCarta;
    }

    public void setValidadeCarta(Date newValidade){
        this.validade_carta_conduc=newValidade;
    }
}
