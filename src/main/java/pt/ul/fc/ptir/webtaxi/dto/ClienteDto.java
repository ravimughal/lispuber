package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.Genero;

public record ClienteDto (Integer nif, String email, String senha, String nome, Genero genero){}
