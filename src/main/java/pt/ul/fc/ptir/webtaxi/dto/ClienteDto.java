package pt.ul.fc.ptir.webtaxi.dto;
import pt.ul.fc.ptir.webtaxi.entity.Genero;
public class ClienteDto extends UtilizadorDto{

    public ClienteDto(){}

    public ClienteDto(Integer nif, String email, String senha, String nome, Genero genero){
        super(nif,email,senha,nome,genero);
    }

}