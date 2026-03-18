package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
