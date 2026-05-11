package pt.ul.fc.ptir.webtaxi.dto;


public record TaxiCreateDTO(String matricula, String modelo, String marca, 
                            String tipoMotor, Integer anoCompra, String nivelConforto, Double kilometros) {}
