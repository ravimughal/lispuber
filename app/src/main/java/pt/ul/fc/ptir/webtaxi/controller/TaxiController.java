package pt.ul.fc.ptir.webtaxi.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pt.ul.fc.ptir.webtaxi.dto.TaxiCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiResponseDTO;
import pt.ul.fc.ptir.webtaxi.services.TaxiService;

@RestController
@RequestMapping("/taxis")
public class TaxiController {

    private final TaxiService taxiService;

    public TaxiController(TaxiService taxiService) {
        this.taxiService = taxiService;
    }


    @GetMapping
    public ResponseEntity<List<TaxiResponseDTO>> listarTaxis() {
        return ResponseEntity.ok(taxiService.listarTaxis());
    }

    
    @PostMapping
    public ResponseEntity<TaxiResponseDTO> criarTaxi(@RequestBody TaxiCreateDTO TaxiCreateDTO) {
        return ResponseEntity.ok(taxiService.criarTaxi(TaxiCreateDTO));
    }
}
