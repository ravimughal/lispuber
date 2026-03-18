package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;

public record TaxiDTO(Long id, String matricula, String modelo,
                      String marca, String tipoMotor, Integer anoCompra,
                      String conforto) {
}
