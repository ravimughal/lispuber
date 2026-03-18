package pt.ul.fc.ptir.webtaxi.dto;
import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

public record UtilizadorDto (Integer nif, String email, String senha, String nome, Genero genero) {}