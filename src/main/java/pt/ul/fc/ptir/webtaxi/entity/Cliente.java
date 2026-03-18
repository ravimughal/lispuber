package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.Entity;
import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

@Entity
public class Cliente extends Utilizador{

    public Cliente(){}

    public Cliente(Integer nif, String email, String senha, String nome, Genero genero){
        super(nif,email,senha,nome,genero);
    }

}
