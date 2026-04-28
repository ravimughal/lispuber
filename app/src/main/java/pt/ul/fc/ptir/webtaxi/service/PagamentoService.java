package pt.ul.fc.ptir.webtaxi.service;

import pt.ul.fc.ptir.webtaxi.dto.PagamentoEstadoDTO;
import pt.ul.fc.ptir.webtaxi.dto.PagamentoIniciarResponseDTO;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.entity.Pagamento;
import pt.ul.fc.ptir.webtaxi.entity.Pagamento.EstadoPagamento;
import pt.ul.fc.ptir.webtaxi.entity.Viagem;
import pt.ul.fc.ptir.webtaxi.repository.PagamentoRepository;
import pt.ul.fc.ptir.webtaxi.repository.ViagemRepository;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PagamentoService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${stripe.publishable-key}")
    private String stripePublishableKey;

    private final PagamentoRepository pagamentoRepository;
    private final ViagemRepository viagemRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository,
                             ViagemRepository viagemRepository) {
        this.pagamentoRepository = pagamentoRepository;
        this.viagemRepository = viagemRepository;
    }

    /**
     * Called by the client's frontend to initiate payment for a trip.
     * Creates a Stripe PaymentIntent and saves a Pagamento record as PENDENTE.
     * If one already exists (client retrying after failure), reuses it.
     */
    @Transactional
    public PagamentoIniciarResponseDTO iniciarPagamento(Long viagemId) {
        Viagem viagem = viagemRepository.findById(viagemId)
                .orElseThrow(() -> new RuntimeException("Viagem não encontrada: " + viagemId));

        if (viagem.getDuracao().getFim() == null) {
            throw new RuntimeException("Viagem ainda não foi finalizada pelo motorista.");
        }

        // Price comes from pedido — this is what the user story expects
        Double preco = viagem.getPedido().getPreco();

        // Check if a payment already exists (retry case from US9 criterion b)
        var existing = pagamentoRepository.findByViagemId(viagemId);
        if (existing.isPresent()) {
            Pagamento pag = existing.get();
            if (pag.getEstado() == EstadoPagamento.PAGO) {
                throw new RuntimeException("Viagem já foi paga.");
            }
            // PENDENTE or FALHOU → retrieve existing PaymentIntent from Stripe and return it
            try {
                Stripe.apiKey = stripeSecretKey;
                PaymentIntent intent = PaymentIntent.retrieve(pag.getStripePaymentId());
                Mapper.converterParaPagamentoIniciarResponseDTO(pag, intent.getClientSecret(), stripePublishableKey);
            } catch (StripeException e) {
                throw new RuntimeException("Erro ao recuperar PaymentIntent do Stripe: " + e.getMessage());
            }
        }

        // Create new PaymentIntent on Stripe
        // Stripe amounts are in the smallest currency unit — cents for EUR
        long amountInCents = Math.round(preco * 100);

        try {
            Stripe.apiKey = stripeSecretKey;

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur")
                    .setDescription("Pagamento de viagem #" + viagemId)
                    // automatic_payment_methods lets Stripe handle the payment method types
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putMetadata("viagem_id", String.valueOf(viagemId))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Save to DB
            Pagamento pagamento = new Pagamento();
            pagamento.setViagem(viagem);
            pagamento.setStripePaymentId(intent.getId());
            pagamento.setValor(preco);
            pagamento.setEstado(EstadoPagamento.PENDENTE);
            pagamentoRepository.save(pagamento);

            return Mapper.converterParaPagamentoIniciarResponseDTO(pagamento, intent.getClientSecret(), stripePublishableKey);

        } catch (StripeException e) {
            throw new RuntimeException("Erro ao criar PaymentIntent no Stripe: " + e.getMessage());
        }
    }

    /**
     * Called by the webhook endpoint when Stripe sends a payment event.
     * Verifies the signature to make sure it really came from Stripe.
     */
    @Transactional
    public void processarWebhook(String payload, String sigHeader) {
        Event event;
        
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Webhook signature inválida.");
        }
        System.out.println("Evento: " + event.getType());
        // We care about these two event types
        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                String json = event.getDataObjectDeserializer().getRawJson();
                String paymentIntentId;
                try {
                    paymentIntentId = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readTree(json)
                            .get("id")
                            .asText();
                } catch (Exception e) {
                    throw new RuntimeException("Erro a extrair paymentIntentId");
                }

                System.out.println("PaymentIntent ID: " + paymentIntentId);
                marcarPago(paymentIntentId);
            }
            case "payment_intent.payment_failed" -> {
                var optional = event.getDataObjectDeserializer().getObject();
                if (optional.isEmpty()) return; // ignore event safely

                PaymentIntent intent = (PaymentIntent) optional.get();
                marcarFalhou(intent.getId());
            }
            default -> { /* ignore other events */ }
        }
    }

    private void marcarPago(String stripePaymentId) {
        pagamentoRepository.findByStripePaymentId(stripePaymentId).ifPresent(p -> {
            p.setEstado(EstadoPagamento.PAGO);
            p.setAtualizadoEm(LocalDateTime.now());
            pagamentoRepository.save(p);
        });
    }

    private void marcarFalhou(String stripePaymentId) {
        pagamentoRepository.findByStripePaymentId(stripePaymentId).ifPresent(p -> {
            p.setEstado(EstadoPagamento.FALHOU);
            p.setAtualizadoEm(LocalDateTime.now());
            pagamentoRepository.save(p);
        });
    }

    /**
     * Called by the driver's frontend to poll for payment status.
     * Returns PENDENTE, PAGO, or FALHOU.
     */
    @Transactional(readOnly = true)
    public PagamentoEstadoDTO obterEstado(Long viagemId) {
        Pagamento pagamento = pagamentoRepository.findByViagemId(viagemId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado para viagem: " + viagemId));
        return Mapper.converterParaPagamentoEstadoDTO(pagamento);
    }
}