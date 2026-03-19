package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.ptir.webtaxi.dto.CriarTaxiDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiDTO;
import pt.ul.fc.ptir.webtaxi.services.TaxiService;

import java.util.List;

@RestController
@RequestMapping("/taxis")
public class TaxiController {

    private final TaxiService taxiService;

    public TaxiController(TaxiService taxiService) {
        this.taxiService = taxiService;
    }

    @GetMapping
    public ResponseEntity<List<TaxiDTO>> listarTaxis() {
        return ResponseEntity.ok(taxiService.listarTaxis());
    }

    @PostMapping
    public ResponseEntity<TaxiDTO> criarTaxi(@RequestBody CriarTaxiDTO criarTaxiDTO) {
        return ResponseEntity.ok(taxiService.criarTaxi(criarTaxiDTO));
    }
}
