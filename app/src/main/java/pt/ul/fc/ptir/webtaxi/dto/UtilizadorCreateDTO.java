package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDate;

import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

public record UtilizadorCreateDTO (String nif, String email, String senha, String nome,
                                   Genero genero, LocalDate dataNascimento) {}