package pt.ul.fc.ptir.webtaxi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

import pt.ul.fc.ptir.webtaxi.dto.MotoristaResponseDTO;
import pt.ul.fc.ptir.webtaxi.dto.MotoristaCreateDTO;
import pt.ul.fc.ptir.webtaxi.entity.Motorista;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.MotoristaRepository;

@Service
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MotoristaService(MotoristaRepository motoristaRepository, PasswordEncoder passwordEncoder) {
        this.motoristaRepository = motoristaRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<MotoristaResponseDTO> listarMotoristas() {
        return motoristaRepository.findAll()
                .stream()
                .map(Mapper::converterParaMotoristaDTO)
                .toList();
    }


    public MotoristaResponseDTO obterMotoristaPorId(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista não encontrado com id: " + id));
        return Mapper.converterParaMotoristaDTO(motorista);
    }


    @Transactional
    public MotoristaResponseDTO criarMotorista(MotoristaCreateDTO motoristaCreateDTO) {
        validar(motoristaCreateDTO, null);

        Motorista motorista = Mapper.converterParaMotorista(motoristaCreateDTO);
        motorista.setSenha(passwordEncoder.encode(motoristaCreateDTO.senha()));
        motoristaRepository.save(motorista);
        return Mapper.converterParaMotoristaDTO(motorista);
    }


    // TODO: depois mudar isso para não precisar dos ifs (alterar lógica de patch)
    @Transactional
    public MotoristaResponseDTO atualizarMotorista(Long id, MotoristaCreateDTO dto) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista não encontrado com id: " + id));

        validar(dto, motorista);

        if (dto.nome() != null && !dto.nome().isBlank())
            motorista.setName(dto.nome());
        if (dto.email() != null && !dto.email().isBlank())
            motorista.setEmail(dto.email());
        if (dto.genero() != null)
            motorista.setGenero(dto.genero());
        if (dto.numCarta() != null && !dto.numCarta().isBlank())
            motorista.setNumCartaConducao(dto.numCarta());
        if (dto.validadeCarta() != null)
            motorista.setValidadeCartaConducao(dto.validadeCarta());
        if (dto.rua() != null)
            motorista.setRua(dto.rua());
        if (dto.ruaNumero() != null)
            motorista.setRuaNumero(dto.ruaNumero());
        if (dto.codigoPostal() != null)
            motorista.setCodigoPostal(dto.codigoPostal());

        motoristaRepository.save(motorista);
        return Mapper.converterParaMotoristaDTO(motorista);
    }


    @Transactional
    public void removerMotorista(Long id) {
        Motorista motorista = motoristaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Motorista não encontrado com id: " + id));

        // Restrição da API: só pode remover se nunca tiver tido um turno
        // if (!motorista.getTurnos().isEmpty()) {
        //     throw new IllegalStateException("Não é possível remover um motorista que já teve turnos.");
        // }

        motoristaRepository.delete(motorista);
    }

    
    // TODO: rever se o parâmetro existingMotorista é realmente necessário
    // validações partilhadas entre criar e atualizar
    // existingMotorista é null quando estamos a criar
    private void validar(MotoristaCreateDTO dto, Motorista existingMotorista) {
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
        if (dto.numCarta() != null) {
            boolean cartaExiste = motoristaRepository.existsByNumCartaConducao(dto.numCarta());
            boolean ePropriaCarta = existingMotorista != null &&
                    existingMotorista.getNumCartaConducao().equals(dto.numCarta());
            if (cartaExiste && !ePropriaCarta) {
                throw new IllegalArgumentException("Este número de carta já existe no sistema.");
            }
        }
    }
}