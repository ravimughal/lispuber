package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.ptir.webtaxi.dto.FaturaResponseDTO;
import pt.ul.fc.ptir.webtaxi.service.FaturaService;

import java.util.List;

@RestController
@RequestMapping("/faturas")
public class FaturaController {

    private final FaturaService faturaService;

    public FaturaController(FaturaService faturaService) {
        this.faturaService = faturaService;
    }

    @PostMapping("/viagem/{viagemId}/emitir")
    public ResponseEntity<FaturaResponseDTO> emitirFatura(@PathVariable Long viagemId) {
        return ResponseEntity.ok(faturaService.emitirFatura(viagemId));
    }

    @GetMapping("/motorista/{motoristaId}")
    public ResponseEntity<List<FaturaResponseDTO>> listarFaturasMotorista(@PathVariable Long motoristaId) {
        return ResponseEntity.ok(faturaService.listarFaturasDoMotorista(motoristaId));
    }
}
