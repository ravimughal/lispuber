package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDate;

import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

public record GestorResponseDTO (Long id, String nif, String email, String nome,
                                 Genero genero, LocalDate dataNascimento) {}