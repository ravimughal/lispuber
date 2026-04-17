package pt.ul.fc.ptir.webtaxi.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import pt.ul.fc.ptir.webtaxi.dto.ViagemCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.ViagemResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Cliente;
import pt.ul.fc.ptir.webtaxi.entity.Motorista;
import pt.ul.fc.ptir.webtaxi.entity.Pedido;
import pt.ul.fc.ptir.webtaxi.entity.Turno;
import pt.ul.fc.ptir.webtaxi.entity.Utilizador;
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Utilizador utilizador = (Utilizador) authentication.getPrincipal();

        if (!(utilizador instanceof Cliente cliente)) {
            throw new IllegalArgumentException("Apenas clientes podem criar pedidos.");
        }

        Turno turno = turnoRepository.findById(ViagemDTO.turnoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro: Turno não encontrado!"));
        Pedido pedido = pedidoRepository.findById(ViagemDTO.pedidoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erro: Pedido não encontrado!"));

        if(pedido.getEstadoPedido()!=EstadoPedido.EM_CURSO || pedido.getMotoristaQueAceitou()==null){
            System.out.println("Estado do pedido: "+pedido.getEstadoPedido());
            System.out.println("Motorista que aceitou: "+pedido.getMotoristaQueAceitou());
            throw new IllegalStateException("Erro: O pedido associado à viagem tem de estar com estado: EM_CURSO");
        }


        LocalDateTime turno_inicio=turno.getDuracao().getInicio();
        LocalDateTime turno_fim=turno.getDuracao().getFim();


        //Restrição 3 do enunciado
        if(ViagemDTO.dataInicio().isAfter(turno_inicio)&& ViagemDTO.dataInicio().isAfter(turno_fim)){
            throw new IllegalArgumentException("Erro: A data de inicio da viagem tem de estar dentro do periodo do turno.");
        }


        Motorista motorista = pedido.getMotoristaQueAceitou();
        long motoristaId= motorista.getId();
        boolean motoristaOcupado = viagemRepository.isMotoristaOcupado(motoristaId,ViagemDTO.dataInicio());
        if(motoristaOcupado){
            throw new IllegalStateException("Erro: O motorista já está numa viagem");
        }

        long taxiId=turno.getTaxi().getId();
        boolean taxiOcupado = viagemRepository.isTaxiOcupado(taxiId, turno_inicio);

        if(taxiOcupado){
            throw new IllegalStateException("Erro: O táxi com id: "+taxiId+" já está a ser usado numa viagem que está a decorrer.");
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

        Turno turno = viagem.getTurno();

        LocalDateTime turno_inicio=turno.getDuracao().getInicio();
        LocalDateTime turno_fim=turno.getDuracao().getFim();

        LocalDateTime data_final=LocalDateTime.now();

        //Restrição 3 do enunciado
        if(data_final.isAfter(turno_inicio) && data_final.isAfter(turno_fim) && data_final.isAfter(viagem.getDuracao().getInicio())){
            throw new IllegalArgumentException("Erro: A data de fim da viagem tem de estar dentro do periodo do turno e ser antes da"+
                                                "data de ínicio.");
        }

        viagem.setDuracao(new Duracao(viagem.getDuracao().getInicio(),data_final));

        pedidoRepository.save(pedido);
        viagem = viagemRepository.save(viagem);

        return Mapper.converterParaViagemDTO(viagem);
    }



    public List<ViagemResponseDTO> obterViagensByCliente(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof Cliente clienteLogado)) {
            throw new org.springframework.security.access.AccessDeniedException("Apenas clientes podem criar pedidos.");
        }    
        Long clientId=clienteLogado.getId();

        List<Viagem> viagens= viagemRepository.findByClienteId(clientId);
        List<ViagemResponseDTO> res = new ArrayList<ViagemResponseDTO>();
        for(Viagem v:viagens){
            res.add(Mapper.converterParaViagemDTO(v));
        }
        return res;
    }


    
    private void validarViagemParaCriacao(ViagemCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Erro: O DTO da viagem não pode ser null.");
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

    }

}
