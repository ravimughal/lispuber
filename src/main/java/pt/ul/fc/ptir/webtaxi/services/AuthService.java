package pt.ul.fc.ptir.webtaxi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.ptir.webtaxi.dto.*;
import pt.ul.fc.ptir.webtaxi.repository.*;
import pt.ul.fc.ptir.webtaxi.entity.*;
import pt.ul.fc.ptir.webtaxi.entity.embeddable.Morada;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private final JwtUtility jwtUtility;
    private final ClienteRepository clienteRep;
    private final PasswordEncoder passwordEncoder;
    private final MotoristaRepository motoristaRep;
    private final UtilizadorRepository utilizadorRep;

    @Autowired
    public AuthService(ClienteRepository clienteRep, JwtUtility jwtUtility, PasswordEncoder passwordEncoder,
        MotoristaRepository motoristaRep, UtilizadorRepository utilizadorRep
        ) {
        this.clienteRep = clienteRep;
        this.jwtUtility=jwtUtility;
        this.passwordEncoder = passwordEncoder; 
        this.motoristaRep=motoristaRep;
        this.utilizadorRep=utilizadorRep;
    }
    
    @Transactional
    public String registerNewClient(ClienteDTO clienteDto) {

        if (clienteDto.email() == null || clienteDto.email().isBlank()) {
            throw new IllegalArgumentException("Erro: O email do cliente não pode ser null/vazio");
        }

        if (clienteDto.nome() == null || clienteDto.nome().isBlank()) {
            throw new IllegalArgumentException("Erro: O nome do cliente não pode ser null/vazio");
        }

        if (clienteDto.genero() == null) {
            throw new IllegalArgumentException("Erro: O género do cliente não pode ser null");
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
        cliente.setGenero(clienteDto.genero());
        cliente.setName(clienteDto.nome());
        cliente.setNif(clienteDto.nif());

        String senhaEncriptada = passwordEncoder.encode(clienteDto.senha()); // encode à password
        cliente.setSenha(senhaEncriptada);

        cliente.setEmail(clienteDto.email());

        clienteRep.save(cliente);

        long seteDias = 7 * 24 * 60 * 60 * 1000L;
        return jwtUtility.generateToken(new java.util.HashMap<>(), cliente.getEmail(), seteDias);
    }


    @Transactional
    public String registerNewMotorista(MotoristaDTO motoristaDto) {

        validarDadosUtilizador(motoristaDto);

        if (motoristaDto.rua() == null || motoristaDto.rua().isBlank()) {
            throw new IllegalArgumentException("Erro: A rua da morada é obrigatória.");
        }
        if (motoristaDto.numero() == null || motoristaDto.numero().isBlank()) {
            throw new IllegalArgumentException("Erro: O número da porta é obrigatório.");
        }
        if (motoristaDto.codigoPostal() == null || motoristaDto.codigoPostal().isBlank()) {
            throw new IllegalArgumentException("Erro: O código postal é obrigatório.");
        }

        if (motoristaDto.numCartaConducao() == null || motoristaDto.numCartaConducao().isBlank()) {
            throw new IllegalArgumentException("Erro: O número da carta de condução é obrigatório.");
        }
        
        if (motoristaDto.validadeCarta() == null || motoristaDto.validadeCarta().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Erro: A carta de condução está expirada ou a data é inválida.");
        }

        if (motoristaDto.dataNascimento() == null || motoristaDto.dataNascimento().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Erro: O motorista deve ter pelo menos 18 anos.");
        }

        if (motoristaRep.findByEmail(motoristaDto.email()).isPresent()) {
            throw new IllegalArgumentException("Erro: Este email já está registado.");
        }

        if (motoristaRep.existsByNumCartaConducao(motoristaDto.numCartaConducao())) {
            throw new IllegalArgumentException("Erro: Este número de carta já existe no sistema.");
        }

        Morada morada = new Morada(
            motoristaDto.rua(),
            motoristaDto.numero(),
            motoristaDto.complemento(),
            motoristaDto.codigoPostal()
        );

        Motorista motorista = new Motorista();
        motorista.setName(motoristaDto.nome());
        motorista.setEmail(motoristaDto.email());
        motorista.setGenero(motoristaDto.genero());
        motorista.setNif(motoristaDto.nif());
        motorista.setDataNascimento(motoristaDto.dataNascimento());
        motorista.setNumCartaConducao(motoristaDto.numCartaConducao());
        motorista.setValidadeCartaConducao(motoristaDto.validadeCarta());
        motorista.setMorada(morada);

        String senhaEncriptada = passwordEncoder.encode(motoristaDto.senha()); // encode à password
        motorista.setSenha(senhaEncriptada);

        motoristaRep.save(motorista);

        long seteDias = 7 * 24 * 60 * 60 * 1000L;
        return jwtUtility.generateToken(new java.util.HashMap<>(), motorista.getEmail(), seteDias);
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

        long seteDias = 604800000L;
        
        return jwtUtility.generateToken(new java.util.HashMap<>(), utilizador.getEmail(), seteDias);
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

    private void validarDadosUtilizador(MotoristaDTO dto) {
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
