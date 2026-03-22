package pt.ul.fc.ptir.webtaxi.mapper;

import pt.ul.fc.ptir.webtaxi.dto.CriarTaxiDTO;
import pt.ul.fc.ptir.webtaxi.dto.MotoristaDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiDTO;
import pt.ul.fc.ptir.webtaxi.dto.ViagemDTO;
import pt.ul.fc.ptir.webtaxi.entity.*;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Morada;

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

    public static Taxi converterParaTaxi(CriarTaxiDTO criarTaxiDTO) {
        Taxi taxi = new Taxi();
        taxi.setMatricula(criarTaxiDTO.matricula());
        taxi.setMarca(criarTaxiDTO.marca());
        taxi.setModelo(criarTaxiDTO.modelo());
        taxi.setAnoCompra(criarTaxiDTO.anoCompra());
        taxi.setConforto(criarTaxiDTO.conforto());
        taxi.setTipoMotor(criarTaxiDTO.tipoMotor());

        return taxi;
    }

    // MOTORISTA
    public static MotoristaDTO converterParaMotoristaDTO(Motorista motorista) {
        Morada m = motorista.getMorada();
        return new MotoristaDTO(
            motorista.getNif(),
            motorista.getEmail(),
            null, // n expor a senha
            motorista.getNome(),
            motorista.getGenero(),
            motorista.getDataNascimento(),
            m,
            motorista.getNumCartaConducao(),
            motorista.getValidadeCartaConducao(),
            m != null ? m.getRua() : null,
            m != null ? m.getNumero() : null,
            m != null ? m.getComplemento() : null,
            m != null ? m.getCodigoPostal() : null
        );
    }

    public static Motorista converterParaMotorista(MotoristaDTO dto) {
        Morada morada = new Morada(
            dto.rua(),
            dto.numero(),
            dto.complemento(),
            dto.codigoPostal()
        );
        return new Motorista(
            dto.nif(),
            dto.email(),
            dto.senha(),
            dto.nome(),
            dto.genero(),
            dto.dataNascimento(),
            morada,
            dto.numCartaConducao(),
            dto.validadeCarta()
        );
    }
}
