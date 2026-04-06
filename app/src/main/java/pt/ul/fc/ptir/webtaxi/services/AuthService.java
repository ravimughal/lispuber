package pt.ul.fc.ptir.webtaxi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import pt.ul.fc.ptir.webtaxi.dto.*;
import pt.ul.fc.ptir.webtaxi.entity.enums.UserRole;
import pt.ul.fc.ptir.webtaxi.exception.TipoDeUtilizadorInvalidoException;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.*;
import pt.ul.fc.ptir.webtaxi.entity.*;

@Service
public class AuthService {

    private final ClienteRepository clienteRepository;
    private final GestorRepository gestorRepository;
    private final MotoristaRepository motoristaRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtility;

    @Autowired
    public AuthService(ClienteRepository clienteRepository, GestorRepository gestorRepository, JwtUtility jwtUtility,
                       PasswordEncoder passwordEncoder, MotoristaRepository motoristaRepository,
                       UtilizadorRepository utilizadorRepository) {
        this.clienteRepository = clienteRepository;
        this.gestorRepository = gestorRepository;
        this.jwtUtility=jwtUtility;
        this.passwordEncoder = passwordEncoder; 
        this.motoristaRepository = motoristaRepository;
        this.utilizadorRepository = utilizadorRepository;
    }

    @Transactional
    public String registerNewClient(ClienteCreateDTO clienteCreateDTO) {

        validarDadosUtilizador(Mapper.converterParaUtilizadorResponseDTO(clienteCreateDTO));

        if (clienteRepository.findByEmail(clienteCreateDTO.email()).isPresent()) {
            throw new IllegalArgumentException("Erro: Este email já está existe.");
        }

        Cliente cliente = new Cliente();
        cliente.setNif(clienteCreateDTO.nif());
        cliente.setEmail(clienteCreateDTO.email());
        cliente.setName(clienteCreateDTO.nome());
        cliente.setDataNascimento(clienteCreateDTO.dataNascimento());
        cliente.setGenero(clienteCreateDTO.genero());

        String senhaEncriptada = passwordEncoder.encode(clienteCreateDTO.senha());
        cliente.setSenha(senhaEncriptada);

        clienteRepository.save(cliente);

        long validade = jwtUtility.TOKEN_EXPIRATION_INTERVAL;
        Map<String, String> claims = new HashMap<>();
        claims.put("role", UserRole.CLIENTE.name());
        return jwtUtility.generateToken(claims, cliente.getEmail(), validade);
    }

    @Transactional
    public String registerNewGestor(GestorCreateDTO gestorCreateDTO) {

        validarDadosUtilizador(Mapper.converterParaUtilizadorResponseDTO(gestorCreateDTO));

        if (gestorRepository.findByEmail(gestorCreateDTO.email()).isPresent()) {
            throw new IllegalArgumentException("Erro: Este email já está existe.");
        }

        Gestor gestor = new Gestor();
        gestor.setDataNascimento(gestorCreateDTO.dataNascimento());
        gestor.setGenero(gestorCreateDTO.genero());
        gestor.setName(gestorCreateDTO.nome());
        gestor.setNif(gestorCreateDTO.nif());
        gestor.setEmail(gestorCreateDTO.email());

        String senhaEncriptada = passwordEncoder.encode(gestorCreateDTO.senha());
        gestor.setSenha(senhaEncriptada);

        gestorRepository.save(gestor);

        long validade = jwtUtility.TOKEN_EXPIRATION_INTERVAL;
        Map<String, String> claims = new HashMap<>();
        claims.put("role", UserRole.GESTOR.name());
        return jwtUtility.generateToken(claims, gestor.getEmail(), validade);
    }

    @Transactional
    public String registerNewMotorista(MotoristaCreateDTO motoristaCreateDTO) {

        validarDadosUtilizador(Mapper.converterParaUtilizadorResponseDTO(motoristaCreateDTO));

        if (motoristaCreateDTO.rua() == null || motoristaCreateDTO.rua().isBlank()) {
            throw new IllegalArgumentException("Erro: A rua da morada é obrigatória.");
        }
        if (motoristaCreateDTO.ruaNumero() == null || motoristaCreateDTO.ruaNumero() <= 0) {
            throw new IllegalArgumentException("Erro: O número da porta é obrigatório.");
        }
        if (motoristaCreateDTO.codigoPostal() == null || motoristaCreateDTO.codigoPostal().isBlank()) {
            throw new IllegalArgumentException("Erro: O código postal é obrigatório.");
        }

        if (motoristaCreateDTO.numCarta() == null || motoristaCreateDTO.numCarta().isBlank()) {
            throw new IllegalArgumentException("Erro: O número da carta de condução é obrigatório.");
        }
        
        if (motoristaCreateDTO.validadeCarta() == null || motoristaCreateDTO.validadeCarta().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Erro: A carta de condução está expirada ou a data é inválida.");
        }

        if (motoristaCreateDTO.dataNascimento() == null || motoristaCreateDTO.dataNascimento().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Erro: O motorista deve ter pelo menos 18 anos.");
        }

        if (motoristaRepository.findByEmail(motoristaCreateDTO.email()).isPresent()) {
            throw new IllegalArgumentException("Erro: Este email já está registado.");
        }

        if (motoristaRepository.existsByNumCartaConducao(motoristaCreateDTO.numCarta())) {
            throw new IllegalArgumentException("Erro: Este número de carta já existe no sistema.");
        }

        Motorista motorista = Mapper.converterParaMotorista(motoristaCreateDTO);
        String senhaEncriptada = passwordEncoder.encode(motoristaCreateDTO.senha()); // encode à password
        motorista.setSenha(senhaEncriptada);

        motoristaRepository.save(motorista);

        long validade = jwtUtility.TOKEN_EXPIRATION_INTERVAL;
        Map<String, String> claims = new HashMap<>();
        claims.put("role", UserRole.MOTORISTA.name());
        return jwtUtility.generateToken(claims, motorista.getEmail(), validade);
    }

    @Transactional
    public String login(LoginRequestDTO loginRequestDTO) {
        String email = loginRequestDTO.email();
        String password = loginRequestDTO.password();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Erro: O email não pode ser vazio.");
        }

        Optional<Utilizador> utilizadorOpt = utilizadorRepository.findByEmail(email);

        // não existe este email na db
        if (utilizadorOpt.isEmpty()) {
            throw new RecursoNaoEncontradoException("Email ou senha inválidos");
        }

        Utilizador utilizador = utilizadorOpt.get();

        // não existe esta senha na db
        if (!passwordEncoder.matches(password,utilizador.getSenha())) {
            throw new RecursoNaoEncontradoException("Email ou senha inválidos");
        }

        long validade = jwtUtility.TOKEN_EXPIRATION_INTERVAL;
        Map<String, String> claims = new HashMap<>();

        if (utilizador instanceof Cliente) {
            claims.put("role", UserRole.CLIENTE.name());
        } else if (utilizador instanceof Motorista) {
            claims.put("role", UserRole.MOTORISTA.name());
        } else if (utilizador instanceof Gestor) {
            claims.put("role", UserRole.GESTOR.name());
        } else {
            throw new TipoDeUtilizadorInvalidoException();
        }

        return jwtUtility.generateToken(claims, utilizador.getEmail(), validade);
    }
    
    private boolean isNIFValid(String nif) {
        if (nif == null || nif.length() != 9 || !nif.matches("\\d{9}")) {
            return false;
        }

        char firstChar = nif.charAt(0);
        if (!"1235689".contains(String.valueOf(firstChar))) {
            String prefix2 = nif.substring(0, 2);
            if (!prefix2.equals("45") && !prefix2.startsWith("7")) {
                return false;
            }
        }
        return true;
    }

    private boolean isPasswordValid(String password){
        if(password == null || password.isBlank()){
            return false;
        }

        if(password.length()<6){
            return false;
        }

        return password.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).+$");
    }

    private boolean isEmailValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + 
                            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void validarDadosUtilizador(UtilizadorCreateDTO utilizadorDTO) {
        if (utilizadorDTO.email() == null || utilizadorDTO.email().isBlank() || !isEmailValid(utilizadorDTO.email()))
            throw new IllegalArgumentException("Erro: Email inválido.");

        if (utilizadorDTO.nome() == null || utilizadorDTO.nome().isBlank())
            throw new IllegalArgumentException("Erro: Nome inválido.");

        if (!isNIFValid(utilizadorDTO.nif()))
            throw new IllegalArgumentException("Erro: NIF inválido.");

        if (!isPasswordValid(utilizadorDTO.senha()))
            throw new IllegalArgumentException("Erro: A password deve ter pelo menos 6 caracteres, letras e números.");

        if (utilizadorDTO.dataNascimento() == null)
            throw new IllegalArgumentException("Erro: Data de nascimento inválida.");

        if (utilizadorDTO.genero() == null)
            throw new IllegalArgumentException("Erro: Género inválido.");
    }
}
