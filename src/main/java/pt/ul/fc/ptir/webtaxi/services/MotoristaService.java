package pt.ul.fc.ptir.webtaxi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.ptir.webtaxi.dto.MotoristaDTO;
import pt.ul.fc.ptir.webtaxi.entity.Motorista;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Morada;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.MotoristaRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MotoristaService(MotoristaRepository motoristaRepository, PasswordEncoder passwordEncoder) {
        this.motoristaRepository = motoristaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<MotoristaDTO> listarMotoristas() {
        return motoristaRepository.findAll()
                .stream()
                .map(Mapper::converterParaMotoristaDTO)
                .toList();
    }

    public MotoristaDTO obterMotoristaPorId(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado com id: " + id));
        return Mapper.converterParaMotoristaDTO(motorista);
    }

    @Transactional
    public MotoristaDTO criarMotorista(MotoristaDTO dto) {
        validar(dto, null);

        Motorista motorista = Mapper.converterParaMotorista(dto);
        motorista.setSenha(passwordEncoder.encode(dto.senha()));
        motoristaRepository.save(motorista);
        return Mapper.converterParaMotoristaDTO(motorista);
    }

    @Transactional
    public MotoristaDTO atualizarMotorista(Long id, MotoristaDTO dto) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado com id: " + id));

        validar(dto, motorista);

        if (dto.nome() != null && !dto.nome().isBlank())
            motorista.setName(dto.nome());
        if (dto.email() != null && !dto.email().isBlank())
            motorista.setEmail(dto.email());
        if (dto.genero() != null)
            motorista.setGenero(dto.genero());
        if (dto.numCartaConducao() != null && !dto.numCartaConducao().isBlank())
            motorista.setNumCartaConducao(dto.numCartaConducao());
        if (dto.validadeCarta() != null)
            motorista.setValidadeCartaConducao(dto.validadeCarta());
        if (dto.rua() != null) {
            Morada morada = new Morada(dto.rua(), dto.numero(), dto.complemento(), dto.codigoPostal());
            motorista.setMorada(morada);
        }

        motoristaRepository.save(motorista);
        return Mapper.converterParaMotoristaDTO(motorista);
    }

    @Transactional
    public void removerMotorista(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motorista não encontrado com id: " + id));

        // Restrição da API: só pode remover se nunca tiver tido um turno
        if (!motorista.getTurnos().isEmpty()) {
            throw new IllegalStateException("Não é possível remover um motorista que já teve turnos.");
        }

        motoristaRepository.delete(motorista);
    }

    // validações partilhadas entre criar e atualizar
    // existingMotorista é null quando estamos a criar
    private void validar(MotoristaDTO dto, Motorista existingMotorista) {
        if (dto.validadeCarta() != null && dto.validadeCarta().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A carta de condução está expirada.");
        }
        if (dto.dataNascimento() != null && dto.dataNascimento().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("O motorista deve ter pelo menos 18 anos.");
        }

        // verificar email duplicado (ignorar o próprio em updates)
        if (dto.email() != null) {
            motoristaRepository.findByEmail(dto.email()).ifPresent(found -> {
                if (existingMotorista == null || !found.getId().equals(existingMotorista.getId())) {
                    throw new IllegalArgumentException("Este email já está registado.");
                }
            });
        }

        // verificar carta duplicada (ignorar o próprio em updates)
        if (dto.numCartaConducao() != null) {
            boolean cartaExiste = motoristaRepository.existsByNumCartaConducao(dto.numCartaConducao());
            boolean ePropriaCarta = existingMotorista != null &&
                    existingMotorista.getNumCartaConducao().equals(dto.numCartaConducao());
            if (cartaExiste && !ePropriaCarta) {
                throw new IllegalArgumentException("Este número de carta já existe no sistema.");
            }
        }
    }
}