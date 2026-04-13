package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;

public record TaxiCreateDTO(String matricula, String modelo, String marca, 
                            String tipoMotor, Integer anoCompra, String nivelConforto, Double kilometros) {}
