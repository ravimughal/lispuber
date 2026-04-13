package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.ptir.webtaxi.dto.TaxiResponseDTO;
import pt.ul.fc.ptir.webtaxi.dto.TurnoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.TurnoResponseDTO;
import pt.ul.fc.ptir.webtaxi.service.TurnoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    @Autowired
    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping
    public ResponseEntity<TurnoResponseDTO> criarTurno(@RequestBody TurnoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.criarTurno(dto));
    }

    @GetMapping("/{id}/taxis-disponiveis")
    public ResponseEntity<List<TaxiResponseDTO>> listarTaxisDisponiveis(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.listarTaxisDisponiveis(id));
    }

    @PostMapping("/{id}/selecionar-taxi")
    public ResponseEntity<TurnoResponseDTO> selecionarTaxi(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body
    ) {
            Long taxiId = body.get("taxiId");
            if (taxiId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(turnoService.selecionarTaxi(id, taxiId));
    }
}