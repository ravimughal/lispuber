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
        try {
            return ResponseEntity.ok(viagemService.obterViagemPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping
    public ResponseEntity<ViagemResponseDTO> criarViagem(@RequestBody ViagemCreateDTO dto) {
        ViagemResponseDTO viagemDTO = viagemService.criarViagem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(viagemDTO);
    }


    // @PostMapping("/{id}/finalizar")
    // public ResponseEntity<ViagemDTO> finalizarViagem(@PathVariable Long id, @RequestBody ViagemDTO dto) {
    //     return ResponseEntity.ok(viagemService.finalizarViagem(id, dto));
    // }
}
