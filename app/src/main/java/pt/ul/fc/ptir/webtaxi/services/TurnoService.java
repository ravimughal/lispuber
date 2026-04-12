package pt.ul.fc.ptir.webtaxi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.ptir.webtaxi.dto.TurnoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.TurnoResponseDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiDTO;
import pt.ul.fc.ptir.webtaxi.entity.Motorista;
import pt.ul.fc.ptir.webtaxi.entity.Taxi;
import pt.ul.fc.ptir.webtaxi.entity.Turno;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Duracao;
import pt.ul.fc.ptir.webtaxi.entity.enums.Motor;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.MotoristaRepository;
import pt.ul.fc.ptir.webtaxi.repository.TaxiRepository;
import pt.ul.fc.ptir.webtaxi.repository.TurnoRepository;

import java.util.List;

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
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado: " + dto.motoristaId()));

        // Restrição 6 — motorista já tem turno nesse período
        if (!turnoRepository.findTurnosMotoristaNoIntervalo(dto.motoristaId(), dto.inicio(), dto.fim()).isEmpty()) {
            throw new IllegalStateException("O motorista já tem um turno neste período.");
        }

        Turno turno = new Turno();
        turno.setMotorista(motorista);
        turno.setDuracao(new Duracao(dto.inicio(), dto.fim()));
        // táxi fica null até selecionar-taxi ser chamado

        turnoRepository.save(turno);
        return Mapper.converterParaTurnoResponseDTO(turno);
    }

    public List<TaxiDTO> listarTaxisDisponiveis(Long turnoId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RuntimeException("Turno não encontrado: " + turnoId));

        var inicio = turno.getDuracao().getInicio();
        var fim = turno.getDuracao().getFim();

        return taxiRepository.findAll().stream()
                .filter(taxi -> {
                    // Restrição 6 — táxi já está noutro turno nesse período
                    boolean taxiOcupado = !turnoRepository
                            .findTurnosTaxiNoIntervalo(taxi.getId(), inicio, fim).isEmpty();
                    if (taxiOcupado) return false;

                    // Restrição 11 — táxi elétrico a carregar no início do turno
                    boolean eEletrico = taxi.getMotor() == Motor.ELETRICO;
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
                .orElseThrow(() -> new RuntimeException("Turno não encontrado: " + turnoId));

        if (turno.getTaxi() != null) {
            throw new IllegalStateException("Este turno já tem um táxi associado.");
        }

        Taxi taxi = taxiRepository.findById(taxiId)
                .orElseThrow(() -> new RuntimeException("Táxi não encontrado: " + taxiId));

        var inicio = turno.getDuracao().getInicio();
        var fim = turno.getDuracao().getFim();

        // Restrição 6 — táxi já está noutro turno nesse período
        if (!turnoRepository.findTurnosTaxiNoIntervalo(taxiId, inicio, fim).isEmpty()) {
            throw new IllegalStateException("O táxi já está associado a outro turno neste período.");
        }

        // Restrição 11 — táxi elétrico a carregar no início do turno
        if (taxi.getMotor() == Motor.ELETRICO && turnoRepository.isTaxiACarregarEm(taxiId, inicio)) {
            throw new IllegalStateException("O táxi elétrico está a carregar no início do turno.");
        }

        turno.setTaxi(taxi);
        turnoRepository.save(turno);
        return Mapper.converterParaTurnoResponseDTO(turno);
    }
}