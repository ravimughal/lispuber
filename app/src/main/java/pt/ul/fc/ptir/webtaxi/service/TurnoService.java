package pt.ul.fc.ptir.webtaxi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.ptir.webtaxi.dto.TurnoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.TurnoResponseDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Motorista;
import pt.ul.fc.ptir.webtaxi.entity.Taxi;
import pt.ul.fc.ptir.webtaxi.entity.Turno;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;
import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.MotoristaRepository;
import pt.ul.fc.ptir.webtaxi.repository.TaxiRepository;
import pt.ul.fc.ptir.webtaxi.repository.TurnoRepository;

import java.util.List;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final MotoristaRepository motoristaRepository;
    private final TaxiRepository taxiRepository;

    @Autowired
    public TurnoService(TurnoRepository turnoRepository, MotoristaRepository motoristaRepository,
                        TaxiRepository taxiRepository) {
        this.turnoRepository = turnoRepository;
        this.motoristaRepository = motoristaRepository;
        this.taxiRepository = taxiRepository;
    }

    @Transactional
    public TurnoResponseDTO criarTurno(TurnoCreateDTO dto) {
        Motorista motorista = motoristaRepository.findById(dto.motoristaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista não encontrado: " + dto.motoristaId()));

        if (dto.inicio().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("O início do turno não pode ser no passado.");
        }

        if (!dto.fim().isAfter(dto.inicio())) {
            throw new IllegalArgumentException("O fim do turno tem de ser depois do início do mesmo.");
        } else if (Duration.between(dto.inicio(), dto.fim()).compareTo(Duration.ofHours(8)) > 0) { // Se o turno tiver mais de 8 horas
            throw new IllegalArgumentException("O turno não pode ter mais de 8 horas.");
        }

        // Restrição 6 — motorista já tem turno nesse período
        if (!turnoRepository.findTurnosMotoristaNoIntervalo(dto.motoristaId(), dto.inicio(), dto.fim()).isEmpty()) {
            throw new IllegalStateException("Erro: O motorista já tem um turno neste período.");
        }

        Turno turno = new Turno();
        turno.setMotorista(motorista);
        turno.setDuracao(new Duracao(dto.inicio(), dto.fim()));

        // táxi fica null até selecionar-taxi ser chamado

        turnoRepository.save(turno);
        return Mapper.converterParaTurnoResponseDTO(turno);
    }

    @Transactional
    public List<TurnoResponseDTO> listarTurnos() {
        List<Turno> turnos = turnoRepository.findAll();

        return turnos.stream()
                .map(Mapper::converterParaTurnoResponseDTO)
                .toList();
    }

    @Transactional
    public List<TurnoResponseDTO> listarTurnosByMotorista(Long motoristaId) {
        List<Turno> turnos = turnoRepository.findByMotoristaId(motoristaId);

        return turnos.stream()
                .map(Mapper::converterParaTurnoResponseDTO)
                .toList();
    }

    public List<TaxiResponseDTO> listarTaxisDisponiveis(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turno não encontrado: " + turnoId));

        var inicio = turno.getDuracao().getInicio();
        var fim = turno.getDuracao().getFim();

        // TODO: talvez seja melhor fazer a filtragem na bd (repository)?
        return taxiRepository.findAll().stream()
                .filter(taxi -> {
                    // Restrição 6 — táxi já está noutro turno nesse período
                    boolean taxiOcupado = !turnoRepository
                            .findTurnosTaxiNoIntervalo(taxi.getId(), inicio, fim).isEmpty();
                    if (taxiOcupado) return false;

                    // Restrição 11 — táxi elétrico a carregar no início do turno
                    boolean eEletrico = taxi.getTipoMotor() == TipoMotor.ELETRICO;
                    if (eEletrico && turnoRepository.isTaxiACarregarEm(taxi.getId(), inicio)) {
                        return false;
                    }

                    return true;
                })
                .map(Mapper::converterParaTaxiDTO)
                .toList();
    }

    @Transactional
    public TurnoResponseDTO selecionarTaxi(Long turnoId, Long taxiId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turno não encontrado: " + turnoId));

        if (turno.getTaxi() != null) {
            throw new IllegalStateException("Este turno já tem um táxi associado.");
        }

        Taxi taxi = taxiRepository.findById(taxiId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Táxi não encontrado: " + taxiId));

        var inicio = turno.getDuracao().getInicio();
        var fim = turno.getDuracao().getFim();

        // Restrição 6 — táxi já está noutro turno nesse período
        if (!turnoRepository.findTurnosTaxiNoIntervalo(taxiId, inicio, fim).isEmpty()) {
            throw new IllegalStateException("O táxi já está associado a outro turno neste período.");
        }

        // Restrição 11 — táxi elétrico a carregar no início do turno
        if (taxi.getTipoMotor() == TipoMotor.ELETRICO && turnoRepository.isTaxiACarregarEm(taxiId, inicio)) {
            throw new IllegalStateException("O táxi elétrico está a carregar no início do turno.");
        }

        turno.setTaxi(taxi);
        turnoRepository.save(turno);
        return Mapper.converterParaTurnoResponseDTO(turno);
    }

    @Transactional
    public TurnoResponseDTO atualizarFimTurno(Long turnoId, LocalDateTime novoFim) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turno não encontrado: " + turnoId));

        if (novoFim.isBefore(turno.getDuracao().getInicio())) {
            throw new IllegalArgumentException("O novo fim não pode ser antes do início do turno.");
        }

        // make sure no existing viagem goes beyond the new end
        if (turnoRepository.existeViagemAlemDeFim(turnoId, novoFim)) {
            throw new IllegalStateException("Existem viagens registadas além do novo fim do turno.");
        }

        // re-check restrição 6 — another taxi/motorista turn might now conflict with extended period
        var inicio = turno.getDuracao().getInicio();
        if (!turnoRepository.findTurnosMotoristaNoIntervalo(turno.getMotorista().getId(), inicio, novoFim)
                .stream().filter(t -> !t.getId().equals(turnoId)).toList().isEmpty()) {
            throw new IllegalStateException("O motorista já tem outro turno que interseta o novo período.");
        }
        if (turno.getTaxi() != null &&
                !turnoRepository.findTurnosTaxiNoIntervalo(turno.getTaxi().getId(), inicio, novoFim)
                .stream().filter(t -> !t.getId().equals(turnoId)).toList().isEmpty()) {
            throw new IllegalStateException("O táxi já está noutro turno que interseta o novo período.");
        }

        turno.setDuracao(new Duracao(inicio, novoFim));
        turnoRepository.save(turno);
        return Mapper.converterParaTurnoResponseDTO(turno);
    }
}