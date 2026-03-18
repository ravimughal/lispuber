package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ul.fc.ptir.webtaxi.dto.ViagemDto;
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
    public ResponseEntity<List<ViagemDto>> listarViagens() {
       return ResponseEntity.ok(viagemService.listarTodasAsViagens());
   }
}
