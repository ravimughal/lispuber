package pt.ul.fc.ptir.webtaxi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ul.fc.ptir.webtaxi.dto.ViagemDto;
import pt.ul.fc.ptir.webtaxi.entity.Viagem;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.ViagemRepository;

import java.util.List;

@Service
@Transactional
public class ViagemService {
    private final ViagemRepository viagemRepository;

    @Autowired
    public ViagemService(ViagemRepository viagemRepository) {
        this.viagemRepository = viagemRepository;
    }

    public List<ViagemDto> listarTodasAsViagens() {
        List<Viagem> viagens = viagemRepository.findAll();
        if (viagens.isEmpty()) return List.of();
        return viagens.stream().map(Mapper::converterParaViagemDto).toList();
    }

}
