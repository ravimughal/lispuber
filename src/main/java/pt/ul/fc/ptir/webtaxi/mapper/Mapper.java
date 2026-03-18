package pt.ul.fc.ptir.webtaxi.mapper;

import pt.ul.fc.ptir.webtaxi.dto.TaxiDTO;
import pt.ul.fc.ptir.webtaxi.dto.ViagemDTO;
import pt.ul.fc.ptir.webtaxi.entity.*;

public class Mapper {

    public static ViagemDTO converterParaViagemDTO(Viagem viagem) {
        return new ViagemDTO(
                viagem.getId(),
                viagem.getMoradaEntrada(),
                viagem.getMoradaSaida(),
                viagem.getDuracao(),
                viagem.getKmPercorridos(),
                viagem.getNumPassageiro(),
                viagem.getPreco(),
                viagem.getCliente(),
                viagem.getFatura()
        );
    }

    public static TaxiDTO converterParaTaxiDTO(Taxi taxi) {
        return new TaxiDTO(
                taxi.getId(),
                taxi.getMatricula(),
                taxi.getModelo(),
                taxi.getMarca(),
                taxi.getTipoMotor().name(),
                taxi.getAnoCompra(),
                taxi.getConforto().getNivelConforto().name()
        );
    }
}
