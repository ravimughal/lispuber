package pt.ul.fc.ptir.webtaxi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ul.fc.ptir.webtaxi.dto.CriarTaxiDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiDTO;
import pt.ul.fc.ptir.webtaxi.entity.Taxi;
import pt.ul.fc.ptir.webtaxi.entity.Viagem;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.TaxiRepository;

import java.util.List;

@Service
public class TaxiService {

    private final TaxiRepository taxiRepository;

    @Autowired
    public TaxiService(TaxiRepository taxiRepository) {
        this.taxiRepository = taxiRepository;
    }

    // TODO: lançar exceção se não houver taxis registados
    public List<TaxiDTO> listarTaxis() {
        List<Taxi> taxis = taxiRepository.findAll();
        if (taxis.isEmpty()) return List.of();
        return taxis.stream().map(Mapper::converterParaTaxiDTO).toList();
    }

    public TaxiDTO criarTaxi(CriarTaxiDTO criarTaxiDTO) {
        Taxi taxi = Mapper.converterParaTaxi(criarTaxiDTO);

        // TODO: criar uma exceção personalizada
        if (taxiRepository.existsByMatricula(taxi.getMatricula()))
            throw new RuntimeException("Já existe um táxi com esta matrícula! (matrícula " + taxi.getMatricula() + ")");

        taxi = taxiRepository.save(taxi);
        return Mapper.converterParaTaxiDTO(taxi);
    }

}
