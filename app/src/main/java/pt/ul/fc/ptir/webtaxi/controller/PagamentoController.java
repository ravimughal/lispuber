package pt.ul.fc.ptir.webtaxi.controller;

import pt.ul.fc.ptir.webtaxi.dto.PagamentoEstadoDTO;
import pt.ul.fc.ptir.webtaxi.dto.PagamentoIniciarResponseDTO;
import pt.ul.fc.ptir.webtaxi.service.PagamentoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    /**
     * POST /pagamentos/viagem/{id}/iniciar
     * Client calls this after the driver finalizes the trip.
     * Returns the Stripe clientSecret so the frontend can confirm payment.
     * Secured: only the client of that trip should call this (validate via JWT if needed).
     */
    @PostMapping("/viagem/{id}/iniciar")
    public ResponseEntity<PagamentoIniciarResponseDTO> iniciarPagamento(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.iniciarPagamento(id));
    }

    /**
     * POST /pagamentos/webhook
     * Stripe calls this with the payment result. Must NOT be protected by JWT —
     * Stripe doesn't send your token. Security is done via signature verification inside the service.
     *
     * IMPORTANT: Spring must NOT parse the body — Stripe signature depends on raw bytes.
     * That's why the parameter is a raw String with the full body.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        pagamentoService.processarWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /pagamentos/viagem/{id}/estado
     * Driver polls this every few seconds to know if client has paid.
     * Returns: { viagemId, estado } where estado is PENDENTE | PAGO | FALHOU
     */
    @GetMapping("/viagem/{id}/estado")
    public ResponseEntity<PagamentoEstadoDTO> obterEstado(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.obterEstado(id));
    }
}