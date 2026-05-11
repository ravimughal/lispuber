package pt.ul.fc.ptir.webtaxi.entity;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

@Entity
@Table(name = "gestor")
public class Gestor extends Utilizador{

    public Gestor(){}

    public Gestor(String nif, String email, String senha, String nome, Genero genero, LocalDate dataNascimento){
        super(nif, email, senha, nome, genero, dataNascimento);
    }
}
