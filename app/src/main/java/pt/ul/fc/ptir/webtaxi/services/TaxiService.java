package pt.ul.fc.ptir.webtaxi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.repository.ConfortoRepository;
import pt.ul.fc.ptir.webtaxi.dto.TaxiCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Taxi;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.TaxiRepository;

@Service
public class TaxiService {

    private final TaxiRepository taxiRepository;
    private final ConfortoRepository confortoRepository;

    @Autowired
    public TaxiService(TaxiRepository taxiRepository, ConfortoRepository confortoRepository) {
        this.taxiRepository = taxiRepository;
        this.confortoRepository = confortoRepository;
    }


    // TODO: lançar exceção se não houver taxis registados
    public List<TaxiResponseDTO> listarTaxis() {
        List<Taxi> taxis = taxiRepository.findAll();
        if (taxis.isEmpty()) return List.of();
        return taxis.stream().map(Mapper::converterParaTaxiDTO).toList();
    }


    public TaxiResponseDTO criarTaxi(TaxiCreateDTO taxiCreateDTO) {
        Conforto conforto = confortoRepository.findById(taxiCreateDTO.confortoId())
            .orElseThrow(() -> new RuntimeException("Conforto não encontrado!"));

        Taxi taxi = Mapper.converterParaTaxi(taxiCreateDTO, conforto);

        // TODO: criar uma exceção personalizada
        if (taxiRepository.existsByMatricula(taxi.getMatricula()))
            throw new RuntimeException("Já existe um táxi com esta matrícula! (matrícula " + taxi.getMatricula() + ")");

        taxi = taxiRepository.save(taxi);
        return Mapper.converterParaTaxiDTO(taxi);
    }
}
