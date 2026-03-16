package pt.ul.fc.ptir.webtaxi.entity;

import jakarta.persistence.Entity;

@Entity
public class Gestor extends Utilizador{

    public Gestor(){}

    public Gestor(Integer nif, String email, String senha, String nome, Genero genero){
        super(nif,email,senha,nome,genero);
    }

}
