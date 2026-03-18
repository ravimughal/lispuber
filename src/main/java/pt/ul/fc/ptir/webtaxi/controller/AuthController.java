package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import pt.ul.fc.ptir.webtaxi.dto.*;
import pt.ul.fc.ptir.webtaxi.services.AuthService;
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/register") 
        public ResponseEntity<Void> registerNewClient(@RequestBody ClienteDTO clienteDTO) {
            authService.registerNewClient(clienteDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

    @PostMapping("/login")
    public ResponseEntity<Integer> login(
        @RequestParam String email, 
        @RequestParam String password
    ) {
        try {
            Integer sessionId = authService.login(email, password);
            return new ResponseEntity<>(sessionId, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
