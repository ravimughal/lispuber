package pt.ul.fc.ptir.webtaxi.mapper;

import pt.ul.fc.ptir.webtaxi.dto.*;
import pt.ul.fc.ptir.webtaxi.entity.*;

public class Mapper {

    // CLIENTE
    public static Cliente converterParaCliente(ClienteCreateDTO dto) {
        return new Cliente(dto.nif(), dto.email(), dto.senha(), dto.nome(), dto.genero(), dto.dataNascimento());
    }

    public static ClienteResponseDTO converterParaClienteDTO(Cliente cliente) {
        return new ClienteResponseDTO(
            cliente.getId(),
            cliente.getNif(),
            cliente.getEmail(),
            cliente.getNome(),
            cliente.getGenero(),
            cliente.getDataNascimento()
        );
    }


    // GESTOR
    public static Gestor converterParaGestor(GestorCreateDTO dto) {
        return new Gestor(dto.nif(), dto.email(), dto.senha(), dto.nome(), dto.genero(), dto.dataNascimento());
    }

    public static GestorResponseDTO converterParaGestorDTO(Gestor gestor) {
        return new GestorResponseDTO(
            gestor.getId(),
            gestor.getNif(),
            gestor.getEmail(),
            gestor.getNome(),
            gestor.getGenero(),
            gestor.getDataNascimento()
        );
    }


    // MOTORISTA
    public static Motorista converterParaMotorista(MotoristaCreateDTO dto) {
        return new Motorista(
            dto.nif(),
            dto.email(),
            dto.senha(),
            dto.nome(),
            dto.genero(),
            dto.dataNascimento(),
            dto.rua(),
            dto.ruaNumero(),
            dto.codigoPostal(),
            dto.numCarta(),
            dto.validadeCarta()
        );
    }

    public static MotoristaResponseDTO converterParaMotoristaDTO(Motorista m) {
        return new MotoristaResponseDTO(
            m.getId(),
            m.getNif(),
            m.getEmail(),
            m.getNome(),
            m.getGenero(),
            m.getDataNascimento(),
            m.getRua(),
            m.getRuaNumero(),
            m.getCodigoPostal(),
            m.getNumCartaConducao(),
            m.getValidadeCartaConducao()
        );
    }


    // CONFORTO
    public static ConfortoDTO converterParaConfortoDTO(Conforto c) {
        return new ConfortoDTO(c.getPreco(), c.getNivelConforto());
    }


    // TAXI
    public static Taxi converterParaTaxi(TaxiCreateDTO dto, Conforto conforto) {
        return new Taxi(dto.matricula(), dto.modelo(), dto.marca(), dto.tipoMotor(), dto.anoCompra(), conforto);
    }

    public static TaxiResponseDTO converterParaTaxiDTO(Taxi t) {
        return new TaxiResponseDTO(
            t.getId(),
            t.getMatricula(),
            t.getModelo(),
            t.getMarca(),
            t.getTipoMotor(),
            t.getAnoCompra(),
            converterParaConfortoDTO(t.getConforto())
        );
    }


    // TURNO
    public static Turno converterParaTurno(TurnoCreateDTO dto, Motorista motorista, Taxi taxi) {
        return new Turno(dto.duracao(), motorista, taxi);
    }

    public static TurnoResponseDTO converterParaTurnoDTO(Turno t) {
        return new TurnoResponseDTO(
            t.getId(),
            t.getDuracao(),
            converterParaMotoristaDTO(t.getMotorista()),
            converterParaTaxiDTO(t.getTaxi())
        );
    }


    // PEDIDO
    public static Pedido converterParaPedido(PedidoCreateDTO dto, Cliente cliente, Conforto conforto) {
        double precoCalculado = conforto.getPreco() * dto.numPassageiros();

        return new Pedido(
            cliente,
            conforto,
            dto.numPassageiros(),
            precoCalculado,
            dto.ruaOrigem(),
            dto.latitudeOrigem(),
            dto.longitudeOrigem(),
            dto.ruaDestino(),
            dto.latitudeDestino(),
            dto.longitudeDestino()
        );
    }

    public static PedidoResponseDTO converterParaPedidoDTO(Pedido p) {
        return new PedidoResponseDTO(
            p.getId(),
            converterParaClienteDTO(p.getCliente()),
            converterParaConfortoDTO(p.getConforto()),
            p.getNumPassageiros(),
            p.getPreco(),
            p.getRuaOrigem(),
            p.getLatitudeOrigem(),
            p.getLongitudeOrigem(),
            p.getRuaDestino(),
            p.getLatitudeDestino(),
            p.getLongitudeDestino()
        );
    }


    // VIAGEM
    public static Viagem converterParaViagem(ViagemCreateDTO dto, Cliente cliente, Turno turno, Pedido pedido) {
        return new Viagem(
            cliente,
            turno,
            pedido,
            dto.duracao(),
            dto.kilometros(),
            dto.numPassageiro()
        );
    }

    public static ViagemResponseDTO converterParaViagemDTO(Viagem v) {
        return new ViagemResponseDTO(
            v.getId(),
            converterParaClienteDTO(v.getCliente()),
            converterParaTurnoDTO(v.getTurno()),
            converterParaPedidoDTO(v.getPedido()),
            v.getDuracao(),
            v.getKilometros(),
            v.getNumPassageiro()
        );
    }


    // FATURA
    public static Fatura converterParaFatura(FaturaCreateDTO dto, Viagem viagem, Long numSequencial) {
        return new Fatura(numSequencial, java.time.LocalDateTime.now(), viagem);
    }

    public static FaturaResponseDTO converterParaFaturaDTO(Fatura f) {
        return new FaturaResponseDTO(
            f.getId(),
            f.getNumSequencial(),
            f.getData(),
            f.getViagem().getId()
        );
    }


    // REABASTECIMENTO
    public static Reabastecimento converterParaReabastecimento(ReabastecimentoCreateDTO dto, Turno turno) {
        return new Reabastecimento(
            dto.quantidade(),
            dto.kilometros(),
            dto.eurosPagos(),
            dto.duracao(),
            turno
        );
    }

    public static ReabastecimentoResponseDTO converterParaReabastecimentoDTO(Reabastecimento r) {
        return new ReabastecimentoResponseDTO(
            r.getId(),
            r.getTurno().getId(),
            r.getQuantidade(),
            r.getKilometros(),
            r.getEurosPagos(),
            r.getDuracao()
        );
    }
}