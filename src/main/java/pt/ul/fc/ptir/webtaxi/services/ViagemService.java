package pt.ul.fc.ptir.webtaxi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ul.fc.ptir.webtaxi.dto.CriarViagemDTO;
import pt.ul.fc.ptir.webtaxi.dto.FinalizarViagemDTO;
import pt.ul.fc.ptir.webtaxi.dto.ViagemDTO;
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

    public List<ViagemDTO> listarViagens() {
        List<Viagem> viagens = viagemRepository.findAll();
        if (viagens.isEmpty()) return List.of();
        return viagens.stream().map(Mapper::converterParaViagemDTO).toList();
    }

    public ViagemDTO obterViagemPorId(Long id) {
        Viagem viagem = viagemRepository.findById(id).orElseThrow(() -> new RuntimeException("Viagem não encontrada com id " + id));
        return Mapper.converterParaViagemDTO(viagem);

    }

    @Transactional
    public ViagemDTO criarViagem(CriarViagemDTO criarViagemDTO) {
        validarViagemParaCriacao(criarViagemDTO);

        Viagem viagem = Mapper.converterParaViagem(criarViagemDTO);

        viagem = viagemRepository.save(viagem);
        return Mapper.converterParaViagemDTO(viagem);
    }

    @Transactional
    public ViagemDTO finalizarViagem(Long id, FinalizarViagemDTO dto) {

        Viagem viagem = viagemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada"));

        validarViagemParaFinalizar(viagem, dto);

        Mapper.atualizarComFinalizacao(viagem, dto);

        viagem = viagemRepository.save(viagem);

        return Mapper.converterParaViagemDTO(viagem);
    }

    private void validarViagemParaCriacao(CriarViagemDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("O DTO da viagem não pode ser null.");
        }

        if (dto.moradaEntrada() == null) {
            throw new IllegalArgumentException("A morada de entrada é obrigatória.");
        }

        if (dto.moradaSaida() == null) {
            throw new IllegalArgumentException("A morada de saída é obrigatória.");
        }

        if (dto.numPassageiro() == null || dto.numPassageiro() <= 0) {
            throw new IllegalArgumentException("O número de passageiros deve ser maior que 0.");
        }

        if (dto.cliente() == null) {
            throw new IllegalArgumentException("O cliente é obrigatório.");
        }
    }

    private void validarViagemParaFinalizar(Viagem viagem, FinalizarViagemDTO dto) {
        if (dto.kmPercorridos() == null || dto.kmPercorridos() <= 0) {
            throw new IllegalArgumentException("Distância inválida.");
        }

        if (dto.preco() == null || dto.preco() < 0) {
            throw new IllegalArgumentException("Preço inválido.");
        }
    }

}
