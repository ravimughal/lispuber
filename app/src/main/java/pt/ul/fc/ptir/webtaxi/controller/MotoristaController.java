package pt.ul.fc.ptir.webtaxi.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pt.ul.fc.ptir.webtaxi.dto.MotoristaResponseDTO;
import pt.ul.fc.ptir.webtaxi.dto.MotoristaCreateDTO;
import pt.ul.fc.ptir.webtaxi.service.MotoristaService;

@RestController
@RequestMapping("/motoristas")
public class MotoristaController {

    private final MotoristaService motoristaService;

    @Autowired
    public MotoristaController(MotoristaService motoristaService) {
        this.motoristaService = motoristaService;
    }


    @GetMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<List<MotoristaResponseDTO>> listarMotoristas() {
        return ResponseEntity.ok(motoristaService.listarMotoristas());
    }


    @GetMapping("/{id}")
    public ResponseEntity<MotoristaResponseDTO> obterMotorista(@PathVariable Long id) {
        return ResponseEntity.ok(motoristaService.obterMotoristaPorId(id));
    }


    @PostMapping
    public ResponseEntity<MotoristaResponseDTO> criarMotorista(@RequestBody MotoristaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(motoristaService.criarMotorista(dto));
    }

    // PUT = atualização completa, PATCH = parcial — ambos usam o mesmo service
    // já que o service verifica null em cada campo
    @PutMapping("/{id}")
    public ResponseEntity<MotoristaResponseDTO> atualizarMotorista(@PathVariable Long id, @RequestBody MotoristaCreateDTO dto) {
        return ResponseEntity.ok(motoristaService.atualizarMotorista(id, dto));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<MotoristaResponseDTO> atualizarParcialmente(@PathVariable Long id, @RequestBody MotoristaCreateDTO dto) {
        return ResponseEntity.ok(motoristaService.atualizarMotorista(id, dto));
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerMotorista(@PathVariable Long id) {
        motoristaService.removerMotorista(id);
        return ResponseEntity.noContent().build();
    }
}