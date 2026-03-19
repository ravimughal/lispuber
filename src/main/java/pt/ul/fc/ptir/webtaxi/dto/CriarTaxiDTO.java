package pt.ul.fc.ptir.webtaxi.dto;

import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;

public record CriarTaxiDTO(String matricula, String marca, String modelo,
                           Integer anoCompra, Conforto conforto, TipoMotor tipoMotor) {

}
