package pt.ul.fc.ptir.webtaxi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.entity.Turno;
import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;
import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEditavelException;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.repository.ConfortoRepository;
import pt.ul.fc.ptir.webtaxi.dto.TaxiCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Taxi;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.TaxiRepository;
import pt.ul.fc.ptir.webtaxi.repository.TurnoRepository;
import pt.ul.fc.ptir.webtaxi.repository.ViagemRepository;

@Service
public class TaxiService {

    private final TaxiRepository taxiRepository;
    private final ConfortoRepository confortoRepository;
    private final TurnoRepository turnoRepository;
    private final ViagemRepository viagemRepository;

    @Autowired
    public TaxiService(TaxiRepository taxiRepository, ConfortoRepository confortoRepository, TurnoRepository turnoRepository, ViagemRepository viagemRepository) {
        this.taxiRepository = taxiRepository;
        this.confortoRepository = confortoRepository;
        this.turnoRepository = turnoRepository;
        this.viagemRepository = viagemRepository;
    }

    public List<TaxiResponseDTO> listarTaxis() {
        List<Taxi> taxis = taxiRepository.findAll();
        if (taxis.isEmpty())
            throw new RecursoNaoEncontradoException("Não há táxis registados.");
        return taxis.stream().map(Mapper::converterParaTaxiDTO).toList();
    }

    @Transactional
    public TaxiResponseDTO criarTaxi(TaxiCreateDTO taxiCreateDTO) {
        Conforto conforto = confortoRepository.findByNivelConforto(NivelConforto.valueOf(taxiCreateDTO.nivelConforto()))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conforto não encontrado."));

        Taxi taxi = Mapper.converterParaTaxi(taxiCreateDTO, conforto);

        if (taxiRepository.existsByMatricula(taxi.getMatricula()))
            throw new IllegalStateException("Já existe um táxi com esta matrícula. (matrícula " + taxi.getMatricula() + ")");

        taxi = taxiRepository.save(taxi);
        return Mapper.converterParaTaxiDTO(taxi);
    }

    @Transactional
    public TaxiResponseDTO obterTaxi(Long id) {
        Taxi taxi = taxiRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Táxi não encontrado com id: " + id));
        return Mapper.converterParaTaxiDTO(taxi);
    }

    // TODO: depois mudar isso para não precisar dos ifs (alterar lógica de patch)
    @Transactional
    public TaxiResponseDTO editarTaxi(Long id, TaxiCreateDTO taxiCreateDTO) {
        validar(taxiCreateDTO);
        Taxi taxi = taxiRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Táxi não encontrado com id: " + id));

        if (taxiCreateDTO.matricula() != null && !taxiCreateDTO.matricula().isBlank())
            taxi.setMatricula(taxiCreateDTO.matricula());
        if (taxiCreateDTO.modelo() != null && !taxiCreateDTO.modelo().isBlank())
            taxi.setModelo(taxiCreateDTO.modelo());
        if (taxiCreateDTO.marca() != null && !taxiCreateDTO.marca().isBlank())
            taxi.setMarca(taxiCreateDTO.marca());
        if (taxiCreateDTO.tipoMotor() != null && !taxiCreateDTO.tipoMotor().isBlank())
            taxi.setTipoMotor(TipoMotor.valueOf(taxiCreateDTO.tipoMotor()));
        if (taxiCreateDTO.anoCompra() != null)
            taxi.setAnoCompra(taxiCreateDTO.anoCompra());

        // só permite editar o conforto caso este táxi nunca tenha feito uma viagem
        if (taxiCreateDTO.nivelConforto() != null && !taxiCreateDTO.nivelConforto().isBlank() && !viagemRepository.existeTaxiEmViagens(id)) {
            Conforto conforto = confortoRepository.findByNivelConforto(NivelConforto.valueOf(taxiCreateDTO.nivelConforto()))
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Nível de conforto não existe: " + taxiCreateDTO.nivelConforto()));
            taxi.setConforto(conforto);
        }
        if (taxiCreateDTO.kilometros() != null) {
            taxi.setKilometros(taxiCreateDTO.kilometros());
        }
        return Mapper.converterParaTaxiDTO(taxi);
    }

    @Transactional
    public void removerTaxi(Long id) {
        // verificar se o taxi já foi requisitado para algum turno
        if (turnoRepository.existeTaxiEmTurnos(id))
            throw new RecursoNaoEditavelException("Táxi já foi requisitado em algum turno.");
        Taxi taxi = taxiRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Táxi não encontrado com id: " + id));
        taxiRepository.delete(taxi);
    }

    public void validar(TaxiCreateDTO taxiCreateDTO) {
        // TODO: implementar regras de validação dos dados. Lançar exceção caso não seja válido
    }
}
