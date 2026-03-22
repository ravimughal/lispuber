package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.ptir.webtaxi.dto.MotoristaDTO;
import pt.ul.fc.ptir.webtaxi.services.MotoristaService;

import java.util.List;

@RestController
@RequestMapping("/motoristas")
public class MotoristaController {

    private final MotoristaService motoristaService;

    @Autowired
    public MotoristaController(MotoristaService motoristaService) {
        this.motoristaService = motoristaService;
    }

    @GetMapping
    public ResponseEntity<List<MotoristaDTO>> listarMotoristas() {
        return ResponseEntity.ok(motoristaService.listarMotoristas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MotoristaDTO> obterMotorista(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(motoristaService.obterMotoristaPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<MotoristaDTO> criarMotorista(@RequestBody MotoristaDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(motoristaService.criarMotorista(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT = atualização completa, PATCH = parcial — ambos usam o mesmo service
    // já que o service verifica null em cada campo
    @PutMapping("/{id}")
    public ResponseEntity<MotoristaDTO> atualizarMotorista(@PathVariable Long id, @RequestBody MotoristaDTO dto) {
        try {
            return ResponseEntity.ok(motoristaService.atualizarMotorista(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MotoristaDTO> atualizarParcialmente(@PathVariable Long id, @RequestBody MotoristaDTO dto) {
        try {
            return ResponseEntity.ok(motoristaService.atualizarMotorista(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerMotorista(@PathVariable Long id) {
        try {
            motoristaService.removerMotorista(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}