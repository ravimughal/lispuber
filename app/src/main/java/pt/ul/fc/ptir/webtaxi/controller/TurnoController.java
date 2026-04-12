package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.ptir.webtaxi.dto.TaxiResponseDTO;
import pt.ul.fc.ptir.webtaxi.dto.TurnoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.TurnoResponseDTO;
import pt.ul.fc.ptir.webtaxi.services.TurnoService;

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
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.criarTurno(dto));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/taxis-disponiveis")
    public ResponseEntity<List<TaxiResponseDTO>> listarTaxisDisponiveis(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(turnoService.listarTaxisDisponiveis(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/selecionar-taxi")
    public ResponseEntity<TurnoResponseDTO> selecionarTaxi(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long taxiId = body.get("taxiId");
            if (taxiId == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(turnoService.selecionarTaxi(id, taxiId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}