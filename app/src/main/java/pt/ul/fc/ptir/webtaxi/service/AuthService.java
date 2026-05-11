package pt.ul.fc.ptir.webtaxi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
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
    public Map<String, Object> registerNewClient(ClienteCreateDTO clienteCreateDTO) {

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

        String token = jwtUtility.generateToken(claims, cliente.getEmail(), validade);

        ClienteResponseDTO clienteDTO = new ClienteResponseDTO(
            null,
            cliente.getNif(),
            cliente.getEmail(),
            cliente.getNome(),
            cliente.getGenero(),
            cliente.getDataNascimento()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("cliente", clienteDTO);
        response.put("role", UserRole.CLIENTE.name());
        return response;
    }

    @Transactional
    public Map<String, Object> registerNewGestor(GestorCreateDTO gestorCreateDTO) {

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

        String token = jwtUtility.generateToken(claims, gestor.getEmail(), validade);

        GestorResponseDTO gestorDTO = new GestorResponseDTO(
            null,
            gestor.getNif(),
            gestor.getEmail(),
            gestor.getNome(),
            gestor.getGenero(),
            gestor.getDataNascimento()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("gestor", gestorDTO);
        response.put("role", UserRole.GESTOR.name());
        return response;
    }

    @Transactional
    public Map<String, Object> registerNewMotorista(MotoristaCreateDTO motoristaCreateDTO) {

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

        if(!isCartaConducaoValid(motoristaCreateDTO.numCarta())){
            throw new IllegalArgumentException("Erro: O formato do número da carta de condução tem de ser do tipo: AA-000000");
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
        
        String token = jwtUtility.generateToken(claims, motorista.getEmail(), validade);

        MotoristaResponseDTO motoristaDTO = new MotoristaResponseDTO(
            motorista.getId(),
            motorista.getNif(),
            motorista.getEmail(),
            motorista.getNome(),
            motorista.getGenero(),
            motorista.getDataNascimento(),
            motorista.getRua(),
            motorista.getRuaNumero(),
            motorista.getCodigoPostal(),
            motorista.getNumCartaConducao(),
            motorista.getValidadeCartaConducao(),
            motorista.getCriadoEm(),
            motorista.getAtualizadoEm()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("motorista", motoristaDTO);
        response.put("role", UserRole.MOTORISTA.name());
        return response;
    }

    @Transactional
    public Map<String, Object> login(LoginRequestDTO loginRequestDTO) {

        String email = loginRequestDTO.email();
        String password = loginRequestDTO.password();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Erro: O email não pode ser vazio.");
        }

        Utilizador utilizador = utilizadorRepository.findByEmail(email)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(password, utilizador.getSenha())) {
            throw new RecursoNaoEncontradoException("Email ou senha inválidos");
        }

        long validade = jwtUtility.TOKEN_EXPIRATION_INTERVAL;
        Map<String, String> claims = new HashMap<>();

        Map<String, Object> response = new HashMap<>();
        String role = null;
        Object userDTO = null;

        if (utilizador instanceof Cliente cliente) {
            claims.put("role", UserRole.CLIENTE.name());
            role = UserRole.CLIENTE.name();

            userDTO = new ClienteResponseDTO(
                null,
                cliente.getNif(),
                cliente.getEmail(),
                cliente.getNome(),
                cliente.getGenero(),
                cliente.getDataNascimento()
            );

        } else if (utilizador instanceof Motorista motorista) {
            claims.put("role", UserRole.MOTORISTA.name());
            role = UserRole.MOTORISTA.name();

            userDTO = new MotoristaResponseDTO(
                motorista.getId(),
                motorista.getNif(),
                motorista.getEmail(),
                motorista.getNome(),
                motorista.getGenero(),
                motorista.getDataNascimento(),
                motorista.getRua(),
                motorista.getRuaNumero(),
                motorista.getCodigoPostal(),
                motorista.getNumCartaConducao(),
                motorista.getValidadeCartaConducao(),
                motorista.getCriadoEm(),
                motorista.getAtualizadoEm()
            );

        } else if (utilizador instanceof Gestor gestor) {
            claims.put("role", UserRole.GESTOR.name());
            role = UserRole.GESTOR.name();

            userDTO = new GestorResponseDTO(
                null,
                gestor.getNif(),
                gestor.getEmail(),
                gestor.getNome(),
                gestor.getGenero(),
                gestor.getDataNascimento()
            );

        } else {
            throw new TipoDeUtilizadorInvalidoException();
        }

        String token = jwtUtility.generateToken(claims, utilizador.getEmail(), validade);
        response.put("token", token);
        response.put("user", userDTO);
        response.put("role", role);
        return response;
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
    
    private boolean isCartaConducaoValid(String cartaConducao){
        String regex = "[a-zA-Z]{2}-[0-9]{6}";

        if (cartaConducao.matches(regex)) {
            return true;
        } 
        return false;   
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
