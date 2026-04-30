package pt.ul.fc.ptir.webtaxi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.ptir.webtaxi.dto.GetPrecoViagemDTO;
import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;
import pt.ul.fc.ptir.webtaxi.service.PrecoService;

@RestController
@RequestMapping("/precos")
public class PrecoController {
    private final PrecoService precoService;

    @Autowired
    public PrecoController(PrecoService precoService) {
        this.precoService = precoService;
    }


    @PostMapping("/calculate-preco-viagem")
    public ResponseEntity<String> calculatePrecoViagem(@RequestBody GetPrecoViagemDTO dto){
        String preco_final;
        try{
            preco_final = precoService.getPrecoPedido(dto.latOrigem(), dto.lonOrigem(), dto.latDestino(), dto.lonDestino(), dto.data(), dto.conforto());
        }catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(preco_final);
    }


    @PatchMapping("/taxa-noturna")
    public ResponseEntity<String> atualizarTaxaNoturna(@RequestParam Double novoValor) {
        try {
            precoService.atualizarTaxaNoturna(novoValor);
            return ResponseEntity.ok("Taxa noturna atualizada com sucesso para: " + novoValor);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                                 .body("Erro ao atualizar taxa: " + e.getMessage());
        }
    }

    @PatchMapping("/precomin-conforto")
    public ResponseEntity<String> atualizarPrecoConforto(
            @RequestParam Double preco, 
            @RequestParam NivelConforto conforto) {
        try {
            precoService.atualizarPrecoPorMinutoEmConforto(preco, conforto);
            return ResponseEntity.ok("Preço por minuto do nível " + conforto + " atualizado para: " + preco);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro inesperado: " + e.getMessage());
        }
    }
}
