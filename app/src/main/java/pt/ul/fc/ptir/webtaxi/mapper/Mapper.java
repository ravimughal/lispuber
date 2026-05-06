package pt.ul.fc.ptir.webtaxi.mapper;

import org.springframework.stereotype.Component;
import pt.ul.fc.ptir.webtaxi.dto.*;
import pt.ul.fc.ptir.webtaxi.entity.*;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;
import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;

@Component
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
            m.getValidadeCartaConducao(),
            m.getCriadoEm(),
            m.getAtualizadoEm()
        );
    }


    // CONFORTO
    public static ConfortoDTO converterParaConfortoDTO(Conforto c) {
        return new ConfortoDTO(c.getPreco(), c.getNivelConforto());
    }


    // TAXI
    public static Taxi converterParaTaxi(TaxiCreateDTO dto, Conforto conforto) {
        return new Taxi(
                dto.matricula(),
                dto.modelo(),
                dto.marca(),
                TipoMotor.valueOf(dto.tipoMotor()),
                dto.anoCompra(),
                conforto,
                dto.kilometros()
        );
    }

    public static TaxiResponseDTO converterParaTaxiDTO(Taxi t) {
        return new TaxiResponseDTO(
            t.getId(),
            t.getMatricula(),
            t.getModelo(),
            t.getMarca(),
            t.getTipoMotor(),
            t.getAnoCompra(),
            converterParaConfortoDTO(t.getConforto()),
            t.getKilometros(),
            t.getCriadoEm(),
            t.getAtualizadoEm()
        );
    }


    // TURNO
    public static Turno converterParaTurno(TurnoCreateDTO dto, Motorista motorista, Taxi taxi) {
        return new Turno(new Duracao(dto.inicio(),dto.fim()), motorista, taxi);
    }

    public static TurnoResponseDTO converterParaTurnoResponseDTO(Turno turno) {
        return new TurnoResponseDTO(
            turno.getId(),
            turno.getMotorista().getId(),
            turno.getTaxi() != null ? turno.getTaxi().getId() : null,
            turno.getDuracao().getInicio(),
            turno.getDuracao().getFim()
        );
    }


    // PEDIDO
    public static Pedido converterParaPedido(PedidoCreateDTO dto, Cliente cliente, Conforto conforto) {
        return new Pedido(
            cliente,
            conforto,
            dto.numPassageiros(),
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
            p.getRuaOrigem(),
            p.getLatitudeOrigem(),
            p.getLongitudeOrigem(),
            p.getRuaDestino(),
            p.getLatitudeDestino(),
            p.getLongitudeDestino(),
            p.getEstadoPedido(),
            (p.getMotoristaQueAceitou() != null ? p.getMotoristaQueAceitou().getId() : null),
            p.getDataSolicitacao()
        );

    }


    // VIAGEM
    public static Viagem converterParaViagem(ViagemCreateDTO dto, Cliente cliente, Turno turno, Pedido pedido) {
        Conforto conforto = pedido.getConforto();
        double precoCalculado = conforto.getPreco() * pedido.getNumPassageiros();

        return new Viagem(
            cliente,
            turno,
            pedido,
            new Duracao(dto.dataInicio(),null),
            dto.kilometros(),
            precoCalculado
        );
    }

    public static ViagemResponseDTO converterParaViagemDTO(Viagem viagem) {
        return new ViagemResponseDTO(
            viagem.getId(),
            converterParaClienteDTO(viagem.getPedido().getCliente()), 
            converterParaTurnoResponseDTO(viagem.getTurno()),     
            converterParaPedidoDTO(viagem.getPedido()),
            viagem.getDuracao().getInicio(),              
            viagem.getDuracao().getFim(),                
            viagem.getKilometros(),
            viagem.getPreco()
        );
    }


    // FATURA
    public static Fatura converterParaFatura(FaturaCreateDTO dto, Viagem viagem, Long numSequencial) {
        return new Fatura(numSequencial, java.time.LocalDateTime.now(), viagem);
    }


    public FaturaResponseDTO converterParaFaturaDTO(Fatura fatura) {
        return new FaturaResponseDTO(
                fatura.getId(),
                fatura.getNumSequencial(),
                fatura.getData(),
                fatura.getViagem().getId(),
                fatura.getViagem().getPreco()
        );
    }


    // REABASTECIMENTO
    public static Reabastecimento converterParaReabastecimento(Turno turno) {
        return new Reabastecimento(
                0.0,
                0.0,
                0.0,
                new Duracao(java.time.LocalDateTime.now(), null),
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

    // UTILIZADOR
    public static UtilizadorCreateDTO converterParaUtilizadorResponseDTO(MotoristaCreateDTO dto) {
        return new UtilizadorCreateDTO(
            dto.nif(),
            dto.email(),
            dto.senha(),
            dto.nome(),
            dto.genero(),
            dto.dataNascimento()
        );
    }

    public static UtilizadorCreateDTO converterParaUtilizadorResponseDTO(ClienteCreateDTO dto) {
        return new UtilizadorCreateDTO(
            dto.nif(),
            dto.email(),
            dto.senha(),
            dto.nome(),
            dto.genero(),
            dto.dataNascimento()
        );
    }

    public static UtilizadorCreateDTO converterParaUtilizadorResponseDTO(GestorCreateDTO dto) {
        return new UtilizadorCreateDTO(
            dto.nif(),
            dto.email(),
            dto.senha(),
            dto.nome(),
            dto.genero(),
            dto.dataNascimento()
        );
    }

    // PAGAMENTO ----------------------
    public static PagamentoEstadoDTO converterParaPagamentoEstadoDTO(Pagamento pagamento) {
        return new PagamentoEstadoDTO(
            pagamento.getId(),
            pagamento.getViagem().getId(),
            pagamento.getEstado().name()
        );
    }

    public static PagamentoIniciarResponseDTO converterParaPagamentoIniciarResponseDTO(
            Pagamento pagamento,
            String clientSecret,
            String publishableKey) {
        return new PagamentoIniciarResponseDTO(
            pagamento.getId(),
            pagamento.getEstado().name(),
            clientSecret,
            publishableKey
        );
    }
}