package pt.ul.fc.ptir.webtaxi.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ul.fc.ptir.webtaxi.dto.ReabastecimentoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.ReabastecimentoFinalizarDTO;
import pt.ul.fc.ptir.webtaxi.dto.ReabastecimentoResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Reabastecimento;
import pt.ul.fc.ptir.webtaxi.entity.Turno;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.ReabastecimentoRepository;
import pt.ul.fc.ptir.webtaxi.repository.TurnoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReabastecimentoService {

    private final ReabastecimentoRepository reabastecimentoRepository;
    private final TurnoRepository turnoRepository;

    @Autowired
    public ReabastecimentoService(ReabastecimentoRepository reabastecimentoRepository,
                                  TurnoRepository turnoRepository) {
        this.reabastecimentoRepository = reabastecimentoRepository;
        this.turnoRepository = turnoRepository;
    }

    public List<ReabastecimentoResponseDTO> listarReabastecimento() {
        List<Reabastecimento> reabastecimentos = reabastecimentoRepository.findAll();
        if (reabastecimentos.isEmpty()) return List.of();
        return reabastecimentos.stream()
                .map(Mapper::converterParaReabastecimentoDTO)
                .toList();
    }

    public ReabastecimentoResponseDTO obterReabastecimentoPorId(Long id) {
        Reabastecimento reabastecimento = reabastecimentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Reabastecimento não encontrado com id " + id)
                );

        return Mapper.converterParaReabastecimentoDTO(reabastecimento);
    }

    public ReabastecimentoResponseDTO iniciarReabastecimento(ReabastecimentoCreateDTO dto) {
        validarReabastecimentoParaCriacao(dto);

        Turno turno = turnoRepository.findById(dto.turnoId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Turno não encontrado com id " + dto.turnoId())
                );

        Reabastecimento reabastecimento = Mapper.converterParaReabastecimento(turno);

        Reabastecimento guardado = reabastecimentoRepository.save(reabastecimento);
        return Mapper.converterParaReabastecimentoDTO(guardado);
    }

    public ReabastecimentoResponseDTO finalizarReabastecimento(Long id, ReabastecimentoFinalizarDTO dto) {
        validarReabastecimentoParaFinalizacao(dto);

        Reabastecimento reabastecimento = reabastecimentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Reabastecimento não encontrado com id " + id)
                );

        if (reabastecimento.getDuracao() != null && reabastecimento.getDuracao().getFim() != null) {
            throw new IllegalStateException("Reabastecimento já finalizado.");
        }

        reabastecimento.setQuantidade(dto.quantidade());
        reabastecimento.setKilometros(dto.kilometros());
        reabastecimento.setEurosPagos(dto.eurosPagos());
        reabastecimento.getDuracao().setFim(LocalDateTime.now());

        Reabastecimento guardado = reabastecimentoRepository.save(reabastecimento);
        return Mapper.converterParaReabastecimentoDTO(guardado);
    }

    private void validarReabastecimentoParaCriacao(ReabastecimentoCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("O corpo do pedido é obrigatório.");
        }

        if (dto.turnoId() == null) {
            throw new IllegalArgumentException("O turnoId é obrigatório.");
        }
    }

    private void validarReabastecimentoParaFinalizacao(ReabastecimentoFinalizarDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("O corpo do pedido é obrigatório.");
        }

        if (dto.quantidade() == null || dto.quantidade() < 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior ou igual a 0.");
        }

        if (dto.kilometros() == null || dto.kilometros() < 0) {
            throw new IllegalArgumentException("Os kilometros devem ser maiores ou iguais a 0.");
        }

        if (dto.eurosPagos() == null || dto.eurosPagos() < 0) {
            throw new IllegalArgumentException("Os euros pagos devem ser maiores ou iguais a 0.");
        }
    }
}