package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;

import java.time.LocalDateTime;

public record TaxiResponseDTO(Long id, String matricula, String modelo, String marca,
                              TipoMotor tipoMotor, Integer anoCompra, ConfortoDTO conforto, Double kilometros,
                              LocalDateTime criadoEm, LocalDateTime atualizadoEm) {}
