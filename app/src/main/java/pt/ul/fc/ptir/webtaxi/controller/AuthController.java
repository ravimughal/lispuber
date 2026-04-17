package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

import pt.ul.fc.ptir.webtaxi.dto.*;
import pt.ul.fc.ptir.webtaxi.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/register/client")
    public ResponseEntity<Map<String, Object>> registerNewClient(@RequestBody ClienteCreateDTO clienteCreateDTO) {
        Map<String, Object> response = authService.registerNewClient(clienteCreateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    
    @PostMapping("/register/gestor") 
    public ResponseEntity<Map<String, Object>> registerNewGestor(@RequestBody GestorCreateDTO gestorCreateDTO) {
        Map<String, Object> response = authService.registerNewGestor(gestorCreateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/register/motorista")
    public ResponseEntity<Map<String, Object>> registerNewMotorista(@RequestBody MotoristaCreateDTO motoristaCreateDTO){
        Map<String, Object> response = authService.registerNewMotorista(motoristaCreateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        Map<String, Object> response = authService.login(loginRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
