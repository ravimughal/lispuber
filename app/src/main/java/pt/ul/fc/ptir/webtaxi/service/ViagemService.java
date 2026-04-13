package pt.ul.fc.ptir.webtaxi.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import pt.ul.fc.ptir.webtaxi.dto.ViagemCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.ViagemResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Cliente;
import pt.ul.fc.ptir.webtaxi.entity.Pedido;
import pt.ul.fc.ptir.webtaxi.entity.Turno;
import pt.ul.fc.ptir.webtaxi.entity.Viagem;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;
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
        if (viagemRepository.existsByPedidoId(ViagemDTO.pedidoId())) {
        throw new IllegalStateException("Erro: Já existe uma viagem iniciada para este pedido.");
    }

        Cliente cliente = clienteRepository.findById(ViagemDTO.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro: Cliente não encontrado!"));
        Turno turno = turnoRepository.findById(ViagemDTO.turnoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro: Turno não encontrado!"));
        Pedido pedido = pedidoRepository.findById(ViagemDTO.pedidoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro: Pedido não encontrado!"));

        if(pedido.getCliente().getId()!=ViagemDTO.clienteId()){
            throw new IllegalStateException("Erro: O cliente associado à viagem não é o mesmo associado ao pedido.");
        }

        if(pedido.getEstadoPedido()!=EstadoPedido.EM_CURSO || pedido.getMotoristaQueAceitou()==null){
            throw new IllegalStateException("Erro: O pedido associado à viagem tem de estar com estado: EM_CURSO");
        }


        Viagem viagem = new Viagem();
        viagem.setCliente(cliente);
        viagem.setTurno(turno);
        viagem.setPedido(pedido);
        viagem.setDuracao(new Duracao(ViagemDTO.dataInicio(),null));
        viagem.setKilometros(ViagemDTO.kilometros());

        viagem = viagemRepository.save(viagem);
        return Mapper.converterParaViagemDTO(viagem);
    }


    @Transactional
    public ViagemResponseDTO finalizarViagem(Long viagemId) { 
        
        // Verificar se motorista logged é o motorista da viagem/pedido??

        if(viagemId==null||viagemId<=0){
            throw new IllegalArgumentException("Erro: O id da viagem tem ser um número inteiro maior do que 0.");
        }
        
        Viagem viagem = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new RuntimeException("Erro: Viagem não encontrada"));

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(viagem.getPedido().getId());

        if(!pedidoOpt.isPresent()){
            throw new IllegalStateException("Erro: Pedido não existe");
        }
        Pedido pedido=pedidoOpt.get();

        pedido.setEstadoPedido(EstadoPedido.CONCLUIDO);

        LocalDateTime data_final=LocalDateTime.now();

        viagem.setDuracao(new Duracao(viagem.getDuracao().getInicio(),data_final));

        pedidoRepository.save(pedido);
        viagem = viagemRepository.save(viagem);

        return Mapper.converterParaViagemDTO(viagem);
    }


    private void validarViagemParaCriacao(ViagemCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Erro: O DTO da viagem não pode ser null.");
        }
        if(dto.clienteId()==null||dto.clienteId()<=0){
            throw new IllegalArgumentException("Erro: O clientId tem de ser um número inteiro maior do que 0.");
        }

        if(dto.turnoId()==null||dto.turnoId()<=0){
            throw new IllegalArgumentException("Erro: O turnoId tem de ser um número inteiro maior do que 0.");
        }

        if(dto.pedidoId()==null||dto.pedidoId()<=0){
            throw new IllegalArgumentException("Erro: O pedidoId tem de ser um número inteiro maior do que 0.");
        }

        if(dto.kilometros()==null||dto.kilometros()<=0){
            throw new IllegalArgumentException("Erro: O número de kilometros tem de ser maior do que 0.");
        }

        if (dto.numPassageiro() == null || dto.numPassageiro() <= 0|| dto.numPassageiro()>4) {
            throw new IllegalArgumentException("Erro: O número de passageiros deve ser entre 1 e 4.");
        }
    }

}
