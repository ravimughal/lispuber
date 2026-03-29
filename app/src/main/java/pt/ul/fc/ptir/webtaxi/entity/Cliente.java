package pt.ul.fc.ptir.webtaxi.entity;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;



@Entity
@Table(name = "cliente")
public class Cliente extends Utilizador{

    public Cliente(){}

    public Cliente(String nif, String email, String senha, String nome, Genero genero, LocalDate dataNascimento){
        super(nif, email, senha, nome, genero, dataNascimento);
    }
}
