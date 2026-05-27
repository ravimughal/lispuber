package pt.ul.fc.ptir.webtaxi.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pt.ul.fc.ptir.webtaxi.dto.AvaliacaoRequestDTO;
import pt.ul.fc.ptir.webtaxi.dto.AvaliacaoResponseDTO;
import pt.ul.fc.ptir.webtaxi.dto.AvaliacaoMediaDTO;
import pt.ul.fc.ptir.webtaxi.entity.Avaliacao;
import pt.ul.fc.ptir.webtaxi.entity.Motorista;
import pt.ul.fc.ptir.webtaxi.entity.Cliente;
import pt.ul.fc.ptir.webtaxi.entity.Viagem;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.ViagemRepository;
import pt.ul.fc.ptir.webtaxi.repository.AvaliacaoRepository;
import pt.ul.fc.ptir.webtaxi.repository.ClienteRepository;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ViagemRepository viagemRepository;
    private final ClienteRepository clienteRepository;

    @Autowired
    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, 
                            ViagemRepository viagemRepository, 
                            ClienteRepository clienteRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.viagemRepository = viagemRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public AvaliacaoResponseDTO criar(AvaliacaoRequestDTO dto, String emailAutenticado) {

        Cliente cliente = clienteRepository.findByEmail(emailAutenticado)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        Viagem viagem = viagemRepository.findById(dto.viagemId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viagem não encontrada"));

        if (!viagem.getCliente().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só o cliente da viagem pode avaliá-la");
        }

        if (viagem.getDuracao().getFim() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível avaliar uma viagem ainda em curso");
        }

        if (avaliacaoRepository.existsByViagemId(dto.viagemId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta viagem já foi avaliada");
        }

        Motorista motorista = viagem.getTurno().getMotorista();

        Avaliacao avaliacao = Mapper.converterParaAvaliacao(dto, viagem, cliente, motorista);
        avaliacao = avaliacaoRepository.save(avaliacao);

        return Mapper.converterParaAvaliacaoDTO(avaliacao);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponseDTO buscarPorViagem(Long viagemId) {
        Avaliacao avaliacao = avaliacaoRepository.findByViagemId(viagemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avaliação não encontrada para esta viagem"));
        return Mapper.converterParaAvaliacaoDTO(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> listarPorMotorista(Long motoristaId) {
        return avaliacaoRepository.findAllByMotoristaId(motoristaId)
            .stream()
            .map(Mapper::converterParaAvaliacaoDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public AvaliacaoMediaDTO mediaMotorista(Long motoristaId) {
        return avaliacaoRepository.findMediaByMotoristaId(motoristaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma avaliação encontrada para este motorista"));
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> listarPorCliente(Long clienteId) {
        return avaliacaoRepository.findAllByClienteId(clienteId)
            .stream()
            .map(Mapper::converterParaAvaliacaoDTO)
            .toList();
    }
}