package pt.ul.fc.ptir.webtaxi.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.ptir.webtaxi.dto.ViagemCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.ViagemResponseDTO;
import pt.ul.fc.ptir.webtaxi.services.ViagemService;

@RestController
@RequestMapping("/viagens")
public class ViagemController {

    private final ViagemService viagemService;

    @Autowired
    public ViagemController(ViagemService viagemService) {
        this.viagemService = viagemService;
    }


    @GetMapping
    public ResponseEntity<List<ViagemResponseDTO>> listarViagens() {
        return ResponseEntity.ok(viagemService.listarViagens());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ViagemResponseDTO> obterViagem(@PathVariable Long id) {
        return ResponseEntity.ok(viagemService.obterViagemPorId(id));
    }

    @GetMapping("/byCliente")
    public ResponseEntity<List<ViagemResponseDTO>> obterViagensByCliente(){
        return ResponseEntity.ok(viagemService.obterViagensByCliente());
    }

    @PostMapping
    public ResponseEntity<ViagemResponseDTO> criarViagem(@RequestBody ViagemCreateDTO dto) {
        ViagemResponseDTO viagemDTO = viagemService.criarViagem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(viagemDTO);
    }

    
    @PatchMapping("/{id}/finalizar") 
    public ResponseEntity<ViagemResponseDTO> finalizarViagem(@PathVariable Long id) {
        ViagemResponseDTO viagemFinalizada = viagemService.finalizarViagem(id);
        return ResponseEntity.ok(viagemFinalizada);
    }
}
