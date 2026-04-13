package pt.ul.fc.ptir.webtaxi.dto;

import java.time.LocalDate;

import pt.ul.fc.ptir.webtaxi.entity.enums.Genero;

public record MotoristaCreateDTO(String nif, String email, String senha, String nome,
                                 Genero genero, LocalDate dataNascimento, String rua, Integer ruaNumero, 
                                 String codigoPostal, String numCarta, LocalDate validadeCarta) {}
