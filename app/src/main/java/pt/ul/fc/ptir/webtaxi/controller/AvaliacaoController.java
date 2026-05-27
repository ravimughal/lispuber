package pt.ul.fc.ptir.webtaxi.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import pt.ul.fc.ptir.webtaxi.entity.Utilizador;
import pt.ul.fc.ptir.webtaxi.dto.AvaliacaoRequestDTO;
import pt.ul.fc.ptir.webtaxi.dto.AvaliacaoMediaDTO;
import pt.ul.fc.ptir.webtaxi.dto.AvaliacaoResponseDTO;
import pt.ul.fc.ptir.webtaxi.service.AvaliacaoService;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @Autowired
    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<AvaliacaoResponseDTO> criar(
            @Valid @RequestBody AvaliacaoRequestDTO dto,
            @AuthenticationPrincipal Utilizador user) {

        AvaliacaoResponseDTO response = avaliacaoService.criar(dto, user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/viagem/{viagemId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'MOTORISTA', 'GESTOR')")
    public ResponseEntity<AvaliacaoResponseDTO> buscarPorViagem(@PathVariable Long viagemId) {
        return ResponseEntity.ok(avaliacaoService.buscarPorViagem(viagemId));
    }

    @GetMapping("/motorista/{motoristaId}")
    @PreAuthorize("hasAnyRole('MOTORISTA', 'GESTOR')")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarPorMotorista(@PathVariable Long motoristaId) {
        return ResponseEntity.ok(avaliacaoService.listarPorMotorista(motoristaId));
    }

    @GetMapping("/motorista/{motoristaId}/media")
    @PreAuthorize("hasAnyRole('CLIENTE', 'MOTORISTA', 'GESTOR')")
    public ResponseEntity<AvaliacaoMediaDTO> mediaMotorista(@PathVariable Long motoristaId) {
        return ResponseEntity.ok(avaliacaoService.mediaMotorista(motoristaId));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'GESTOR')")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(avaliacaoService.listarPorCliente(clienteId));
    }
}