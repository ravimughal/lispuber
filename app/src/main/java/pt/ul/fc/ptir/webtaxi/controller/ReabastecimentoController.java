package pt.ul.fc.ptir.webtaxi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ul.fc.ptir.webtaxi.dto.ReabastecimentoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.ReabastecimentoFinalizarDTO;
import pt.ul.fc.ptir.webtaxi.dto.ReabastecimentoResponseDTO;
import pt.ul.fc.ptir.webtaxi.service.ReabastecimentoService;

import java.util.List;

@RestController
@RequestMapping("/reabastecimentos")
public class ReabastecimentoController {

    private final ReabastecimentoService reabastecimentoService;

    @Autowired
    public ReabastecimentoController(ReabastecimentoService reabastecimentoService) {
        this.reabastecimentoService = reabastecimentoService;
    }
    @PostMapping
    public ResponseEntity<ReabastecimentoResponseDTO> iniciarReabastecimento(
            @RequestBody ReabastecimentoCreateDTO dto) {
        ReabastecimentoResponseDTO response =
                reabastecimentoService.iniciarReabastecimento(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ReabastecimentoResponseDTO> finalizarReabastecimento(
            @PathVariable Long id,
            @RequestBody ReabastecimentoFinalizarDTO dto) {
        ReabastecimentoResponseDTO response =
                reabastecimentoService.finalizarReabastecimento(id, dto);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<List<ReabastecimentoResponseDTO>> listarReabastecimentos() {
        return ResponseEntity.ok(reabastecimentoService.listarReabastecimento());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ReabastecimentoResponseDTO> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reabastecimentoService.obterReabastecimentoPorId(id));
    }
}