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
import pt.ul.fc.ptir.webtaxi.repository.*;
import pt.ul.fc.ptir.webtaxi.entity.*;



@Service
public class AuthService {

    private final JwtUtility jwtUtility;
    private final ClienteRepository clienteRep;
    private final GestorRepository gestorRep;
    private final PasswordEncoder passwordEncoder;
    private final MotoristaRepository motoristaRep;
    private final UtilizadorRepository utilizadorRep;


    @Autowired
    public AuthService(ClienteRepository clienteRep, GestorRepository gestorRep, JwtUtility jwtUtility, 
                       PasswordEncoder passwordEncoder, MotoristaRepository motoristaRep,
                       UtilizadorRepository utilizadorRep) {
        this.clienteRep = clienteRep;
        this.gestorRep = gestorRep;
        this.jwtUtility=jwtUtility;
        this.passwordEncoder = passwordEncoder; 
        this.motoristaRep=motoristaRep;
        this.utilizadorRep=utilizadorRep;
    }
    

    @Transactional
    public String registerNewClient(ClienteCreateDTO clienteDto) {

        if (clienteDto.email() == null || clienteDto.email().isBlank()) {
            throw new IllegalArgumentException("Erro: O email do cliente não pode ser null/vazio");
        }

        if (clienteDto.nome() == null || clienteDto.nome().isBlank()) {
            throw new IllegalArgumentException("Erro: O nome do cliente não pode ser null/vazio");
        }

        if (clienteDto.genero() == null) {
            throw new IllegalArgumentException("Erro: O género do cliente não pode ser null");
        }

        if (clienteDto.dataNascimento() == null) {
            throw new IllegalArgumentException("Erro: A data de nascimento do cliente não pode ser null");
        }

        if (!isNIFValid(clienteDto.nif())) {
            throw new IllegalArgumentException("Erro: O NIF do cliente é inválido");
        }

        if (!isPasswordValid(clienteDto.senha())) {
            throw new IllegalArgumentException("Erro: A password deve ter pelo menos 6 caracteres, letras e números.");
        }
        if(!isEmailValid(clienteDto.email())){
            throw new IllegalArgumentException("Erro: O email não é valido.");
        }
        if (clienteRep.findByEmail(clienteDto.email()).isPresent()) {
            throw new IllegalArgumentException("Erro: Este email já está existe.");
        }

        Cliente cliente = new Cliente();
        cliente.setDataNascimento(clienteDto.dataNascimento());
        cliente.setGenero(clienteDto.genero());
        cliente.setName(clienteDto.nome());
        cliente.setNif(clienteDto.nif());
        cliente.setEmail(clienteDto.email());

        String senhaEncriptada = passwordEncoder.encode(clienteDto.senha());
        cliente.setSenha(senhaEncriptada);

        clienteRep.save(cliente);

        long validade = 1 * 24 * 60 * 60 * 1000L;
        Map<String, String> claims = new HashMap<>();
        claims.put("role", "CLIENTE");
        return jwtUtility.generateToken(claims, cliente.getEmail(), validade);
    }


    @Transactional
    public String registerNewGestor(GestorCreateDTO gestorDto) {

        if (gestorDto.email() == null || gestorDto.email().isBlank()) {
            throw new IllegalArgumentException("Erro: O email do gestor não pode ser null/vazio");
        }

        if (gestorDto.nome() == null || gestorDto.nome().isBlank()) {
            throw new IllegalArgumentException("Erro: O nome do gestor não pode ser null/vazio");
        }

        if (gestorDto.genero() == null) {
            throw new IllegalArgumentException("Erro: O género do gestor não pode ser null");
        }

        if (gestorDto.dataNascimento() == null) {
            throw new IllegalArgumentException("Erro: A data de nascimento do gestor não pode ser null");
        }

        if (!isNIFValid(gestorDto.nif())) {
            throw new IllegalArgumentException("Erro: O NIF do gestor é inválido");
        }

        if (!isPasswordValid(gestorDto.senha())) {
            throw new IllegalArgumentException("Erro: A password deve ter pelo menos 6 caracteres, letras e números.");
        }
        if(!isEmailValid(gestorDto.email())){
            throw new IllegalArgumentException("Erro: O email não é valido.");
        }
        if (gestorRep.findByEmail(gestorDto.email()).isPresent()) {
            throw new IllegalArgumentException("Erro: Este email já está existe.");
        }

        Gestor gestor = new Gestor();
        gestor.setDataNascimento(gestorDto.dataNascimento());
        gestor.setGenero(gestorDto.genero());
        gestor.setName(gestorDto.nome());
        gestor.setNif(gestorDto.nif());
        gestor.setEmail(gestorDto.email());

        String senhaEncriptada = passwordEncoder.encode(gestorDto.senha());
        gestor.setSenha(senhaEncriptada);

        gestorRep.save(gestor);

        long validade = 1 * 24 * 60 * 60 * 1000L;
        Map<String, String> claims = new HashMap<>();
        claims.put("role", "GESTOR");
        return jwtUtility.generateToken(claims, gestor.getEmail(), validade);
    }


    @Transactional
    public String registerNewMotorista(MotoristaCreateDTO motoristaCreateDto) {

        validarDadosUtilizador(motoristaCreateDto);

        if (motoristaCreateDto.rua() == null || motoristaCreateDto.rua().isBlank()) {
            throw new IllegalArgumentException("Erro: A rua da morada é obrigatória.");
        }
        if (motoristaCreateDto.ruaNumero() == null || motoristaCreateDto.ruaNumero() <= 0) {
            throw new IllegalArgumentException("Erro: O número da porta é obrigatório.");
        }
        if (motoristaCreateDto.codigoPostal() == null || motoristaCreateDto.codigoPostal().isBlank()) {
            throw new IllegalArgumentException("Erro: O código postal é obrigatório.");
        }

        if (motoristaCreateDto.numCarta() == null || motoristaCreateDto.numCarta().isBlank()) {
            throw new IllegalArgumentException("Erro: O número da carta de condução é obrigatório.");
        }
        
        if (motoristaCreateDto.validadeCarta() == null || motoristaCreateDto.validadeCarta().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Erro: A carta de condução está expirada ou a data é inválida.");
        }

        if (motoristaCreateDto.dataNascimento() == null || motoristaCreateDto.dataNascimento().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Erro: O motorista deve ter pelo menos 18 anos.");
        }

        if (motoristaRep.findByEmail(motoristaCreateDto.email()).isPresent()) {
            throw new IllegalArgumentException("Erro: Este email já está registado.");
        }

        if (motoristaRep.existsByNumCartaConducao(motoristaCreateDto.numCarta())) {
            throw new IllegalArgumentException("Erro: Este número de carta já existe no sistema.");
        }

        Motorista motorista = new Motorista();
        motorista.setName(motoristaCreateDto.nome());
        motorista.setEmail(motoristaCreateDto.email());
        motorista.setGenero(motoristaCreateDto.genero());
        motorista.setNif(motoristaCreateDto.nif());
        motorista.setDataNascimento(motoristaCreateDto.dataNascimento());
        motorista.setNumCartaConducao(motoristaCreateDto.numCarta());
        motorista.setValidadeCartaConducao(motoristaCreateDto.validadeCarta());
        motorista.setRua(motoristaCreateDto.rua());
        motorista.setRuaNumero(motoristaCreateDto.ruaNumero());
        motorista.setCodigoPostal(motoristaCreateDto.codigoPostal());

        String senhaEncriptada = passwordEncoder.encode(motoristaCreateDto.senha()); // encode à password
        motorista.setSenha(senhaEncriptada);

        motoristaRep.save(motorista);

        long validade = 1 * 24 * 60 * 60 * 1000L;
        Map<String, String> claims = new HashMap<>();
        claims.put("role", "MOTORISTA");
        return jwtUtility.generateToken(claims, motorista.getEmail(), validade);
    }


    @Transactional
    public String login(String email, String password) throws Exception {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Erro: O email não pode ser vazio.");
        }

        Optional<Utilizador> utilizadorOpt = utilizadorRep.findByEmail(email);

        if (utilizadorOpt.isEmpty()) {
            throw new Exception("Erro: Utilizador não encontrado.");
        }

        Utilizador utilizador = utilizadorOpt.get();

        if (!passwordEncoder.matches(password,utilizador.getSenha())) {
            throw new Exception("Erro: A senha está incorreta.");
        }

        long validade = 1 * 24 * 60 * 60 * 1000L;
        Map<String, String> claims = new HashMap<>();

        if (utilizador instanceof Cliente) {
            claims.put("role", "CLIENTE");
        } else if (utilizador instanceof Motorista) {
            claims.put("role", "MOTORISTA");
        } else if (utilizador instanceof Gestor) {
            claims.put("role", "GESTOR");
        } else {
            throw new Exception("Erro: Tipo de utilizador desconhecido.");
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
        if(password==null || password.isBlank()){
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


    private void validarDadosUtilizador(MotoristaCreateDTO dto) {
        if (dto.email() == null || dto.email().isBlank() || !isEmailValid(dto.email())) {
            throw new IllegalArgumentException("Erro: Email inválido.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Erro: Nome inválido.");
        }
        if (!isNIFValid(dto.nif())) {
            throw new IllegalArgumentException("Erro: NIF inválido.");
        }
        if (!isPasswordValid(dto.senha())) {
            throw new IllegalArgumentException("Erro: Password não cumpre os requisitos.");
        }
    }
}
