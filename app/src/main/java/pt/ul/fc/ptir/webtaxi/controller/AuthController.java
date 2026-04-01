package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

import pt.ul.fc.ptir.webtaxi.dto.*;
import pt.ul.fc.ptir.webtaxi.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/register/client") 
    public ResponseEntity<String> registerNewClient(@RequestBody ClienteCreateDTO clienteCreateDTO) {
        String token = authService.registerNewClient(clienteCreateDTO);
        return new ResponseEntity<>(token,HttpStatus.CREATED);
    }

    
    @PostMapping("/register/gestor") 
    public ResponseEntity<String> registerNewGestor(@RequestBody GestorCreateDTO gestorCreateDTO) {
        String token = authService.registerNewGestor(gestorCreateDTO);
        return new ResponseEntity<>(token,HttpStatus.CREATED);
    }


    @PostMapping("/register/motorista")
    public ResponseEntity<String> registerNewMotorista(@RequestBody MotoristaCreateDTO motoristaCreateDTO){
        String token = authService.registerNewMotorista(motoristaCreateDTO);
        return new ResponseEntity<>(token,HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(
        @RequestParam String email, 
        @RequestParam String password
    ) {
        String token = authService.login(email, password);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping("/debug")
    public ResponseEntity<?> whoAmI() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
            "principal", auth.getPrincipal(),
            "authorities", auth.getAuthorities()
        ));
    }
}
