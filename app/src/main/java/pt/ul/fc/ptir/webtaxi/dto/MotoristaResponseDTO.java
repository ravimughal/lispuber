package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDate;

import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;



public record MotoristaResponseDTO(Long id, String nif, String email, String nome, Genero genero,
                                   LocalDate dataNascimento, String rua, Integer ruaNumero, 
                                   String codigoPostal, String numCarta, LocalDate validadeCarta) {}
