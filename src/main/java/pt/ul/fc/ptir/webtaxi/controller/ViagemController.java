package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.ptir.webtaxi.dto.CriarViagemDTO;
import pt.ul.fc.ptir.webtaxi.dto.FinalizarViagemDTO;
import pt.ul.fc.ptir.webtaxi.dto.ViagemDTO;
import pt.ul.fc.ptir.webtaxi.services.ViagemService;

import java.util.List;

@RestController
@RequestMapping("/viagens")
public class ViagemController {
    private final ViagemService viagemService;

   @Autowired
   public ViagemController(ViagemService viagemService) {
       this.viagemService = viagemService;
   }

   @GetMapping
    public ResponseEntity<List<ViagemDTO>> listarViagens() {
       return ResponseEntity.ok(viagemService.listarViagens());
   }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemDTO> obterViagem(@PathVariable Long id) {
       try {
           return ResponseEntity.ok(viagemService.obterViagemPorId(id));
       } catch (RuntimeException e) {
           return ResponseEntity.notFound().build();
       }
    }

    @PostMapping
    public ResponseEntity<ViagemDTO> criarViagem(@RequestBody CriarViagemDTO dto) {
       ViagemDTO viagemDTO = viagemService.criarViagem(dto);
       return ResponseEntity.status(HttpStatus.CREATED).body(viagemDTO);
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ViagemDTO> finalizarViagem(
            @PathVariable Long id,
            @RequestBody FinalizarViagemDTO dto) {

        return ResponseEntity.ok(viagemService.finalizarViagem(id, dto));
    }
}
