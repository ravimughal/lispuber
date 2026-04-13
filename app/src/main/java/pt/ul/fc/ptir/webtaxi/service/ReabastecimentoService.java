package pt.ul.fc.ptir.webtaxi.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ul.fc.ptir.webtaxi.dto.ReabastecimentoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.ReabastecimentoResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Reabastecimento;
import pt.ul.fc.ptir.webtaxi.entity.Turno;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.ReabastecimentoRepository;
import pt.ul.fc.ptir.webtaxi.repository.TurnoRepository;

import java.util.List;

@Service
@Transactional
public class ReabastecimentoService {

    private final ReabastecimentoRepository reabastecimentoRepository;
    private final TurnoRepository turnoRepository;


    @Autowired
    public ReabastecimentoService(ReabastecimentoRepository reabastecimentoRepository, TurnoRepository turnoRepository) {
        this.reabastecimentoRepository = reabastecimentoRepository;
        this.turnoRepository = turnoRepository;
    }


    public List<ReabastecimentoResponseDTO> listarReabastecimento() {
        List<Reabastecimento> reabastecimentos = reabastecimentoRepository.findAll();
        if (reabastecimentos.isEmpty()) return List.of();
        return reabastecimentos.stream().map(Mapper::converterParaReabastecimentoDTO).toList();
    }

    public ReabastecimentoResponseDTO obterReabastecimentoPorId(Long id) {
        Reabastecimento reabastecimento = reabastecimentoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Reabastecimento não encontrado com id " + id));
        return Mapper.converterParaReabastecimentoDTO(reabastecimento);
    }

    @Transactional
    public ReabastecimentoResponseDTO iniciarReabastecimento(ReabastecimentoCreateDTO dto) {
        validarReabastecimentoParaCriacao(dto);

        Turno turno = turnoRepository.findById(dto.turnoId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Turno não encontrado com id " + dto.turnoId())
                );

        Duracao duracao;
        try {
            duracao = new Duracao(dto.duracao().getInicio(), dto.duracao().getFim());
        } catch (Exception e) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de fim.");
        }

        Reabastecimento reabastecimento = new Reabastecimento();
        reabastecimento.setQuantidade(dto.quantidade());
        reabastecimento.setKilometros(dto.kilometros());
        reabastecimento.setEurosPagos(dto.eurosPagos());
        reabastecimento.setDuracao(duracao);
        reabastecimento.setTurno(turno);

        Reabastecimento guardado = reabastecimentoRepository.save(reabastecimento);

        return Mapper.converterParaReabastecimentoDTO(guardado);
    }

    @Transactional
    public ReabastecimentoResponseDTO finalizarReabastecimento(Long id) {
        Reabastecimento reabastecimento = reabastecimentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Reabastecimento não encontrado com id " + id)
                );

        return Mapper.converterParaReabastecimentoDTO(reabastecimentoRepository.save(reabastecimento));
    }

    private void validarReabastecimentoParaCriacao(ReabastecimentoCreateDTO dto) {
        // TODO: validar excepções;
        return;
    }
}
