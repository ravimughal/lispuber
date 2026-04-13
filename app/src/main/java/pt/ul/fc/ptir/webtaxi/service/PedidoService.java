package pt.ul.fc.ptir.webtaxi.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import pt.ul.fc.ptir.webtaxi.dto.PedidoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.PedidoResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Cliente;
import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.entity.Motorista;
import pt.ul.fc.ptir.webtaxi.entity.Pedido;
import pt.ul.fc.ptir.webtaxi.entity.enums.EstadoPedido;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.ClienteRepository;
import pt.ul.fc.ptir.webtaxi.repository.ConfortoRepository;
import pt.ul.fc.ptir.webtaxi.repository.MotoristaRepository;
import pt.ul.fc.ptir.webtaxi.repository.PedidoRepository;


@Service
@Transactional
public class PedidoService {

    private final ClienteRepository clienteRepository;
    private final ConfortoRepository confortoRepository;
    private final PedidoRepository pedidoRepository;
    private final MotoristaRepository motoristaRepository;

    @Autowired
    public PedidoService(ClienteRepository clienteRepository, ConfortoRepository confortoRepository, PedidoRepository pedidoRepository,
         MotoristaRepository motoristaRepository) {
        this.clienteRepository=clienteRepository;
        this.confortoRepository=confortoRepository;
        this.pedidoRepository=pedidoRepository;
        this.motoristaRepository=motoristaRepository;
    }


    @Transactional
    public PedidoResponseDTO criarPedido(PedidoCreateDTO pedidoDto){
        if(pedidoDto.clienteId()==null||pedidoDto.clienteId()<=0){
            throw new IllegalArgumentException("Erro: O atributo clientId tem de ser um número inteiro maior do que 0.");
        }
        if(pedidoDto.confortoId()==null||pedidoDto.confortoId()<=0){
        throw new IllegalArgumentException("Erro: O atributo confortoId tem de ser um número inteiro maior do que 0.");
        }


        if(pedidoDto.latitudeDestino()==null||pedidoDto.latitudeOrigem()==null || pedidoDto.longitudeDestino()==null||pedidoDto.longitudeOrigem()==null){
            throw new IllegalArgumentException("Erro: Os atributos latitude Origem/Destino e longitude Origem/Destino não podem ser null");
        }
        if(pedidoDto.latitudeOrigem()>90||pedidoDto.latitudeOrigem()<-90){
            throw new IllegalArgumentException("Erro: Latitude origem fora do range válido.");

        }

        if(pedidoDto.latitudeDestino()>90||pedidoDto.latitudeDestino()<-90){
            throw new IllegalArgumentException("Erro: Latitude destino fora do range válido.");

        }
        if(pedidoDto.longitudeOrigem()>180||pedidoDto.longitudeOrigem()<-180){
            throw new IllegalArgumentException("Erro: Longitude origem fora do range válido.");

        }

        if(pedidoDto.longitudeDestino()>180||pedidoDto.longitudeDestino()<-180){
            throw new IllegalArgumentException("Erro: Longitude origem fora do range válido.");

        }

        if(pedidoDto.numPassageiros()==null||pedidoDto.numPassageiros()<1||pedidoDto.numPassageiros()>4){
            throw new IllegalArgumentException("Erro: O número de passageiros tem de er um número inteiro entre 1 e 4.");
        }
        if(pedidoDto.ruaOrigem()==null||pedidoDto.ruaOrigem().isBlank()){
            throw new IllegalArgumentException("Erro: A rua origem não pode ser null/blank");
        }
        if(pedidoDto.ruaDestino()==null||pedidoDto.ruaDestino().isBlank()){
            throw new IllegalArgumentException("Erro: A rua destino não pode ser null/blank");
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof Cliente clienteLogado)) {
            throw new org.springframework.security.access.AccessDeniedException("Apenas clientes podem criar pedidos.");
        }

        if (!pedidoDto.clienteId().equals(clienteLogado.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Erro: O ID do cliente no pedido não corresponde ao utilizador logado.");
        }

        Pedido newPedido = new Pedido();

        Cliente cliente = clienteRepository.findById(pedidoDto.clienteId())
        .orElseThrow(() -> new RecursoNaoEncontradoException("Erro: Cliente não encontrado!"));

        Conforto conforto = confortoRepository.findById(pedidoDto.confortoId())
        .orElseThrow(() -> new RecursoNaoEncontradoException("Erro: Conforto não encontrado!"));

        newPedido.setCliente(cliente);
        newPedido.setConforto(conforto);
        newPedido.setLatitudeDestino(pedidoDto.latitudeDestino());
        newPedido.setLatitudeOrigem(pedidoDto.latitudeOrigem());
        newPedido.setLongitudeDestino(pedidoDto.longitudeDestino());
        newPedido.setLongitudeOrigem(pedidoDto.latitudeOrigem());
        newPedido.setNumPassageiros(pedidoDto.numPassageiros());
        newPedido.setRuaDestino(pedidoDto.ruaDestino());
        newPedido.setRuaOrigem(pedidoDto.ruaOrigem());
        newPedido.setPreco(90.0);  //MUDAR POSTERIORMENTE
        newPedido.setEstadoPedido(EstadoPedido.PENDENTE);
        newPedido.setDataSolicitacao(LocalDateTime.now());
        pedidoRepository.save(newPedido);

    return Mapper.converterParaPedidoDTO(newPedido);
    }

public List<PedidoResponseDTO> getPedidosPendentes(){
    
    List<Pedido> pedidosEncontrados=pedidoRepository.findByEstadoPedido(EstadoPedido.PENDENTE);
    List<PedidoResponseDTO> pedidosDtoResponse = new ArrayList<>();
    for(Pedido p:pedidosEncontrados){
        pedidosDtoResponse.add(Mapper.converterParaPedidoDTO(p));
    }
    return pedidosDtoResponse;
}


@Transactional
public PedidoResponseDTO aceitarPedido(Long pedidoId) {  //Para motoristas

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
    if (!(principal instanceof Motorista motoristaLogado)) {
        throw new org.springframework.security.access.AccessDeniedException("Apenas motoristas podem aceitar pedidos.");
    }
    System.out.println("O userID do motorista É: "+motoristaLogado.getId()+" ,EMAIL: "+motoristaLogado.getEmail());
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));

    //ADICIONAR VERIFICAÇÃO DE MOTORISTA JÁ ESTÁ NUMA VIAGEM/OU NUM PEDIDO CONFIRMADO (EM_CURSO)

    if (pedido.getEstadoPedido() != EstadoPedido.PENDENTE) {
        throw new IllegalStateException("O pedido não está elegivel para ser aceite, visto que não está pendente.");
    }

    pedido.setMotoristaQueAceitou(motoristaLogado);
    pedido.setEstadoPedido(EstadoPedido.ACEITE_POR_MOTORISTA);

    Pedido pedidoGuardado = pedidoRepository.save(pedido);

    return Mapper.converterParaPedidoDTO(pedidoGuardado);
}


@Transactional
public PedidoResponseDTO confirmarMotorista(Long pedidoId) {  //Para clientes
    
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
    if (!(principal instanceof Cliente clienteLogado)) {
        throw new org.springframework.security.access.AccessDeniedException("Apenas motoristas podem aceitar pedidos.");
    }

    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));


    if ((pedido.getEstadoPedido() != EstadoPedido.ACEITE_POR_MOTORISTA ) && (pedido.getMotoristaQueAceitou()!=null)) {
        throw new IllegalStateException("Erro: O pedido não está elegivel para ser confirmado, visto que não foi aceite por um motorista.");
    }

    if(pedido.getCliente().getId()!=clienteLogado.getId()){
        throw new IllegalStateException("Erro: O cliente que está a confirmar não foi o que criou o pedido.");
    }

    pedido.setEstadoPedido(EstadoPedido.EM_CURSO);
    //começar viagem????
    Pedido pedidoGuardado = pedidoRepository.save(pedido);

    return Mapper.converterParaPedidoDTO(pedidoGuardado);
}

@Transactional
public PedidoResponseDTO rejeitarMotorista(Long pedidoId) { //Para clientes

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
    if (!(principal instanceof Cliente clienteLogado)) {
        throw new org.springframework.security.access.AccessDeniedException("Apenas motoristas podem aceitar pedidos.");
    }

    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));


    if ((pedido.getEstadoPedido() != EstadoPedido.ACEITE_POR_MOTORISTA ) && (pedido.getMotoristaQueAceitou()!=null)) {
        throw new IllegalStateException("Erro: O pedido não está elegivel para ser rejeitado, visto que não foi aceite por um motorista.");
    }

    if(pedido.getCliente().getId()!=clienteLogado.getId()){
        throw new IllegalStateException("Erro: O cliente que está a rejeitar não foi o que criou o pedido.");
    }

    pedido.setEstadoPedido(EstadoPedido.PENDENTE); //voltar para pendente, para que outro motorista possa aceitar
    pedido.setMotoristaQueAceitou(null);

    Pedido pedidoGuardado = pedidoRepository.save(pedido);

    return Mapper.converterParaPedidoDTO(pedidoGuardado);
}


@Transactional
public PedidoResponseDTO cancelarPedido(Long pedidoId) { //Para clientes

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
    if (!(principal instanceof Cliente clienteLogado)) {
        throw new org.springframework.security.access.AccessDeniedException("Apenas clientes podem cancelar pedidos.");
    }

    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));


    if ((pedido.getEstadoPedido() != EstadoPedido.PENDENTE)) {
        throw new IllegalStateException("Erro: O pedido não está elegivel para ser cancelado, visto que não está pendente.");
    }

    if(pedido.getCliente().getId()!=clienteLogado.getId()){
        throw new IllegalStateException("Erro: O cliente que está a cancelar, não foi o que criou o pedido.");
    }

    pedido.setEstadoPedido(EstadoPedido.CANCELADO); //cancelar pedido (a ser eliminado futuramente)

    Pedido pedidoGuardado = pedidoRepository.save(pedido);

    return Mapper.converterParaPedidoDTO(pedidoGuardado);
}



}