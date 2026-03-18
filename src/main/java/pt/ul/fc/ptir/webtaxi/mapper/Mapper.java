package pt.ul.fc.ptir.webtaxi.mapper;

import pt.ul.fc.ptir.webtaxi.dto.ViagemDto;
import pt.ul.fc.ptir.webtaxi.entity.*;

public class Mapper {

    public static ViagemDto converterParaViagemDto(Viagem viagem) {
        return new ViagemDto(
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
}
