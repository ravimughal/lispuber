package pt.ul.fc.ptir.webtaxi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.ptir.webtaxi.dto.*;
import pt.ul.fc.ptir.webtaxi.repository.*;
import pt.ul.fc.ptir.webtaxi.entity.*;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {
    

    @Autowired
    private ClienteRepository clienteRep;


    @Transactional
    public Integer registerNewClient(ClienteDto clienteDto){
        if(clienteDto.getEmail()!=null || clienteDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Erro: O email do cliente não pode ser null/vazio");
        }  //fazer uma melhor verificação do email

        if(clienteDto.getNome()!=null || clienteDto.getNome().isBlank()){
            throw new IllegalArgumentException("Erro: O nome do cliente não pode ser null/vazio");
        }
        if(clienteDto.getGenero()!=null){
            throw new IllegalArgumentException("Erro: O género do cliente não pode ser null");
        }
        if(!isNIFValid(clienteDto.getNif())){
            throw new IllegalArgumentException("Erro: O NIF do cliente é inválido");
        }
        if(!isPasswordValid(clienteDto.getSenha())){
            throw new IllegalArgumentException("Erro: A password do cliente é invalida.");
        }

        Cliente cliente = new Cliente();
        cliente.setGenero(clienteDto.getGenero());
        cliente.setName(clienteDto.getNome());
        cliente.setNif(clienteDto.getNif());
        cliente.setSenha(clienteDto.getSenha());
        cliente.setEmail(clienteDto.getEmail());

        clienteRep.save(cliente);
        return this.generateSessionCode();  //implementar isto com o HTTPSESSION 
                                            //https://www.geeksforgeeks.org/advance-java/spring-boot-session-management/
        
    }

    @Transactional
    public Integer login(String email, String password)throws Exception{
        if(isEmailValid(email)){
            throw new IllegalArgumentException("Erro: O email do cliente não pode ser null/vazio");
        }  

        Optional<Cliente> clienteOpt= clienteRep.findByEmail(email);
        if(clienteOpt.isEmpty()){
            throw new Exception("Erro: O cliente não foi encontrado.");
        }
        Cliente cliente = clienteOpt.get();

        if(!(cliente.getSenha().equals(password))){
            throw new Exception("Erro: A senha do cliente está incorreta.");
        }
        
        return this.generateSessionCode();
        
    }

    private boolean isNIFValid(Integer nif_number) {

        String nif = nif_number.toString();
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

    private Integer generateSessionCode(){
        return ThreadLocalRandom.current().nextInt(100000, 100000000);
    }

    private boolean isEmailValid(String email){
        if(email==null || email.isBlank()){
          return false;
        }  
        return true;

    }
}
