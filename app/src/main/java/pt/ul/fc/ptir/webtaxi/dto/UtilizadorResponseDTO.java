package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDate;

import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;



public record UtilizadorResponseDTO (Long id, String nif, String email, String senha, String nome,
                                     Genero genero, LocalDate dataNascimento) {}