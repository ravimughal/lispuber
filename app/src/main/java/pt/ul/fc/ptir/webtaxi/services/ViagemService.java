package pt.ul.fc.ptir.webtaxi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import pt.ul.fc.ptir.webtaxi.dto.ViagemCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.ViagemResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Cliente;
import pt.ul.fc.ptir.webtaxi.entity.Pedido;
import pt.ul.fc.ptir.webtaxi.entity.Turno;
import pt.ul.fc.ptir.webtaxi.entity.Viagem;
import pt.ul.fc.ptir.webtaxi.entity.enums.EstadoPedido;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.ViagemRepository;
import pt.ul.fc.ptir.webtaxi.repository.ClienteRepository;
import pt.ul.fc.ptir.webtaxi.repository.TurnoRepository;
import pt.ul.fc.ptir.webtaxi.repository.PedidoRepository;

@Service
@Transactional
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final ClienteRepository clienteRepository;
    private final TurnoRepository turnoRepository;
    private final PedidoRepository pedidoRepository;

    @Autowired
    public ViagemService(ViagemRepository viagemRepository, ClienteRepository clienteRepository, TurnoRepository turnoRepository, PedidoRepository pedidoRepository) {
        this.viagemRepository = viagemRepository;
        this.clienteRepository = clienteRepository;
        this.turnoRepository = turnoRepository;
        this.pedidoRepository = pedidoRepository;
    }


    public List<ViagemResponseDTO> listarViagens() {
        List<Viagem> viagens = viagemRepository.findAll();
        if (viagens.isEmpty()) return List.of();
        return viagens.stream().map(Mapper::converterParaViagemDTO).toList();
    }


    public ViagemResponseDTO obterViagemPorId(Long id) {
        Viagem viagem = viagemRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Viagem não encontrada com id " + id));
        return Mapper.converterParaViagemDTO(viagem);
    }


    @Transactional
    public ViagemResponseDTO criarViagem(ViagemCreateDTO ViagemDTO) {
        validarViagemParaCriacao(ViagemDTO);

        Cliente cliente = clienteRepository.findById(ViagemDTO.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado!"));
        Turno turno = turnoRepository.findById(ViagemDTO.turnoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turno não encontrado!"));
        Pedido pedido = pedidoRepository.findById(ViagemDTO.pedidoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado!"));

        if(pedido.getCliente().getId()!=ViagemDTO.clienteId()){
            throw new IllegalStateException("Erro: O cliente associado à viagem não é o mesmo associado ao pedido.");
        }
        if(pedido.getEstadoPedido()!=EstadoPedido.EM_CURSO || pedido.getMotoristaQueAceitou()==null){
            throw new IllegalStateException("Erro: O pedido associado à viagem tem de estar com estado: EM_CURSO");
        }


        Viagem viagem = Mapper.converterParaViagem(ViagemDTO, cliente, turno, pedido);

        viagem = viagemRepository.save(viagem);
        return Mapper.converterParaViagemDTO(viagem);
    }


    // @Transactional
    // public ViagemDTO finalizarViagem(Long id, ViagemDTO dto) {

    //     Viagem viagem = viagemRepository.findById(id)
    //             .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));

    //     validarViagemParaFinalizar(viagem, dto);

    //     Mapper.atualizarComFinalizacao(viagem, dto);

    //     viagem = viagemRepository.save(viagem);

    //     return Mapper.converterParaViagemDTO(viagem);
    // }


    private void validarViagemParaCriacao(ViagemCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("O DTO da viagem não pode ser null.");
        }

        if (dto.numPassageiro() == null || dto.numPassageiro() <= 0) {
            throw new IllegalArgumentException("O número de passageiros deve ser maior que 0.");
        }
    }


    // private void validarViagemParaFinalizar(Viagem viagem, ViagemDTO dto) {
    //     if (dto.kilometros() == null || dto.kilometros() <= 0) {
    //         throw new IllegalArgumentException("Distância inválida.");
    //     }

    //     // if (dto.preco() == null || dto.preco() < 0) {
    //     //     throw new IllegalArgumentException("Preço inválido.");
    //     // }
    // }
}
