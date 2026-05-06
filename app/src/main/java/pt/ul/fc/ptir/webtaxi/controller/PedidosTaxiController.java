package pt.ul.fc.ptir.webtaxi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.ptir.webtaxi.dto.PedidoCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.PedidoResponseDTO;
import pt.ul.fc.ptir.webtaxi.service.PedidoService;

@RestController
@RequestMapping("/pedidos-taxi")
public class PedidosTaxiController {
    private final PedidoService pedidoService;

    @Autowired
    public PedidosTaxiController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // POST /pedidos-taxi - Criar pedido
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody PedidoCreateDTO pedidoDto) throws Exception{
        PedidoResponseDTO novoPedido = pedidoService.criarPedido(pedidoDto);
        return new ResponseEntity<>(novoPedido, HttpStatus.CREATED);
    }

    // GET /pedidos-taxi/pendentes - Listar para motoristas
    @GetMapping("/pendentes")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidosPendentes() {
        List<PedidoResponseDTO> pendentes = pedidoService.getPedidosPendentes();
        return ResponseEntity.ok(pendentes);
    }

    // GET /pedidos-taxi - Listar todos os pedidos
    @GetMapping("/")
    public ResponseEntity<List<PedidoResponseDTO>> getPedidos() {
        List<PedidoResponseDTO> pedidos = pedidoService.getPedidos();
        return ResponseEntity.ok(pedidos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obterPedido(@PathVariable Long id){
        return ResponseEntity.ok(pedidoService.obterPedidoById(id));
    }

    @GetMapping("/byCliente")
    public ResponseEntity<List<PedidoResponseDTO>> obterPedido(){
        return ResponseEntity.ok(pedidoService.obterPedidosByCliente());
    }

    @GetMapping("/byMotorista")
    public ResponseEntity<List<PedidoResponseDTO>> obterPedidosByMotorista(){
        return ResponseEntity.ok(pedidoService.obterPedidosByMotorista());
    }


    // POST /pedidos-taxi/{id}/aceitar - Motorista aceita
    @PostMapping("/{id}/aceitar")
    public ResponseEntity<PedidoResponseDTO> aceitarPedido(@PathVariable Long id) {
        PedidoResponseDTO pedidoAceite = pedidoService.aceitarPedido(id);
        return ResponseEntity.ok(pedidoAceite);
    }

    // POST /pedidos-taxi/{id}/confirmar - Cliente confirma motorista
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmarMotorista(@PathVariable Long id) {
        PedidoResponseDTO pedidoConfirmado = pedidoService.confirmarMotorista(id);
        return ResponseEntity.ok(pedidoConfirmado);
    }

    // POST /pedidos-taxi/{id}/rejeitar - Cliente rejeita motorista
    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<PedidoResponseDTO> rejeitarMotorista(@PathVariable Long id) {
        PedidoResponseDTO pedidoRejeitado = pedidoService.rejeitarMotorista(id);
        return ResponseEntity.ok(pedidoRejeitado);
    }

    // POST /pedidos-taxi/{id}/cancelar - Cliente cancela pedido (neste momento n está a eliminar, está so a mudar o estado, ver se é o que queremos)
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long id) {
        PedidoResponseDTO pedidoCancelado=pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(pedidoCancelado);
    }
}
