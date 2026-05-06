package pt.ul.fc.ptir.webtaxi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.ptir.webtaxi.dto.FaturaResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Fatura;
import pt.ul.fc.ptir.webtaxi.entity.Viagem;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.FaturaRepository;
import pt.ul.fc.ptir.webtaxi.repository.ViagemRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FaturaService {

    private final FaturaRepository faturaRepository;
    private final ViagemRepository viagemRepository;
    private final Mapper mapper;

    public FaturaService(
            FaturaRepository faturaRepository,
            ViagemRepository viagemRepository,
            Mapper mapper
    ) {
        this.faturaRepository = faturaRepository;
        this.viagemRepository = viagemRepository;
        this.mapper = mapper;
    }

    @Transactional
    public FaturaResponseDTO emitirFatura(Long viagemId) {
        Viagem viagem = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Viagem não encontrada"));

        if (viagem.getDuracao().getFim() == null) {
            throw new IllegalStateException("A viagem ainda não terminou.");
        }

        if (viagem.getPreco() == null || viagem.getPreco().doubleValue() <= 0) {
            throw new IllegalStateException("A viagem não tem valor válido para faturação.");
        }

        if (viagem.getPedido().getCliente() == null || viagem.getPedido().getCliente().getNif() == null) {
            throw new IllegalStateException("A viagem não tem cliente com NIF válido.");
        }

        if (faturaRepository.existsByViagemId(viagemId)) {
            throw new IllegalStateException("Já existe uma fatura para esta viagem.");
        }

        Fatura fatura = new Fatura();
        fatura.setViagem(viagem);
        fatura.setData(LocalDateTime.now());
        fatura.setNumSequencial(gerarProximoNumeroSequencial());

        Fatura guardada = faturaRepository.save(fatura);

        return mapper.converterParaFaturaDTO(guardada);
    }

    public List<FaturaResponseDTO> listarFaturasDoMotorista(Long motoristaId) {
        return faturaRepository
                .findByViagemTurnoMotoristaIdOrderByDataDesc(motoristaId)
                .stream()
                .map(mapper::converterParaFaturaDTO)
                .toList();
    }

    private Long gerarProximoNumeroSequencial() {
        Long ultimo = faturaRepository.findMaxNumSequencial();
        return ultimo == null ? 1L : ultimo + 1;
    }
}