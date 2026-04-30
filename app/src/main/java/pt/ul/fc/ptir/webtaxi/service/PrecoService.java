package pt.ul.fc.ptir.webtaxi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import pt.ul.fc.ptir.webtaxi.entity.Cliente;
import pt.ul.fc.ptir.webtaxi.entity.ConfiguracaoTaxa;
import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.entity.Gestor;
import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;
import pt.ul.fc.ptir.webtaxi.repository.ConfiguracaoTaxaRepository;
import pt.ul.fc.ptir.webtaxi.repository.ConfortoRepository;

@Service
@Transactional
public class PrecoService {


    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Autowired
    private ConfortoRepository confortoRepository;
    
    @Autowired
    private ConfiguracaoTaxaRepository taxaRepository;

    @Autowired
    public PrecoService(ConfortoRepository confortoRepository,ConfiguracaoTaxaRepository taxaRepository){
        this.confortoRepository=confortoRepository;
        this.taxaRepository=taxaRepository;
        
    }

    @Transactional
    public String getPrecoPedido(Double latitude_origem, Double longitude_origem,
                                Double latitude_destino, Double longitude_destino, 
                                LocalDateTime dataInicio, NivelConforto conforto) throws Exception {

        verificarInputsPedido(latitude_origem, latitude_destino, longitude_origem, longitude_destino, dataInicio, conforto);
        Double tempoTotalDouble = obterTempoViagem(latitude_origem, longitude_origem, latitude_destino, longitude_destino);
        System.out.println("Tempo total da viagem calculado: "+tempoTotalDouble);
        int tempoTotalMinutos = tempoTotalDouble.intValue();


        Conforto entidadeConforto = confortoRepository.findByNivelConforto(conforto).orElse(null);
        Double precoPorMinuto;
        
        if (entidadeConforto != null) {
            precoPorMinuto = entidadeConforto.getPreco(); 
        } else {
            precoPorMinuto = 0.20; 
        }

        // Ir buscar taxa noturna
        ConfiguracaoTaxa taxaNoturnaObj = taxaRepository.findById("TAXA_NOTURNA").orElse(null);
        Double multiplicadorNoite;
        
        if (taxaNoturnaObj != null) {
            multiplicadorNoite = taxaNoturnaObj.getValor();
        } else {
            multiplicadorNoite = 1.0; 
        }

        Double precoFinal = 0.0;
        LocalDateTime tempoDecorrido = dataInicio;


        for (int i = 0; i < tempoTotalMinutos; i++) {
            LocalTime horaAtual = tempoDecorrido.toLocalTime();
            Double custoDesteMinuto;

            // Verifica se o minuto atual está no horário de agravamento (entre 21h e 06h)
            if (horaAtual.isAfter(LocalTime.of(20, 59)) || horaAtual.isBefore(LocalTime.of(6, 0))) {
                custoDesteMinuto = precoPorMinuto * multiplicadorNoite;
            } else {
                custoDesteMinuto = precoPorMinuto;
            }

            precoFinal = precoFinal + custoDesteMinuto;
            
            tempoDecorrido = tempoDecorrido.plusMinutes(1);
        }

        System.out.println("Custo Total Calculado: " + precoFinal);

        Double precoMinimo = 2.50;
        Double precoFinalComMinimo = Math.max(precoFinal, precoMinimo);
        
        return String.format(Locale.US, "%.2f", precoFinalComMinimo);
    }

    //SO PARA GESTOR
    @Transactional
    public void atualizarTaxaNoturna(Double novoValor) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof Gestor gestorLogado)) {
            throw new org.springframework.security.access.AccessDeniedException("Apenas gestores podem atualizar a taxa noturna.");
        }    
        ConfiguracaoTaxa taxa = taxaRepository.findById("TAXA_NOTURNA")
            .orElse(new ConfiguracaoTaxa("TAXA_NOTURNA", 0.0));  //se não existir, cria
        
        taxa.setValor(novoValor);
        taxaRepository.save(taxa);
    }


    //SO PARA GESTOR
    @Transactional
    public void atualizarPrecoPorMinutoEmConforto(Double preco, NivelConforto conforto){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof Gestor gestorLogado)) {
            throw new org.springframework.security.access.AccessDeniedException("Apenas gestores podem atualizar o preço por minuto de um tipo de conforto.");
        }    
        if(preco==null||preco<0){
            throw new IllegalArgumentException("Erro: o preço tem de ser um double maior do que 0.");
        }
        if(conforto==null){
            throw new IllegalArgumentException("Erro: O conforto não pode ser null.");
        }
        Conforto entidadeConforto = confortoRepository.findByNivelConforto(conforto).orElse(null);

        if (entidadeConforto != null) {
            entidadeConforto.setPreco(preco);
        } else {
            throw new IllegalStateException("Erro: Não existe o nivel de conforto "+conforto+".");
        }

        confortoRepository.save(entidadeConforto);

    }


    @Transactional
    public Double obterTempoViagem(Double latOri, Double lonOri, Double latDest, Double lonDest) throws Exception {
        
        String url = String.format(Locale.US,
            "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
            lonOri, latOri, lonDest, latDest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String body = response.body();


            Pattern pattern = Pattern.compile("\"duration\":(\\d+\\.?\\d*)");
            Matcher matcher = pattern.matcher(body);

            if (matcher.find()) {
                double segundos = Double.parseDouble(matcher.group(1));
                return segundos / 60; 
            }
            return 0.0;
        } else {
            throw new RuntimeException("Erro ao consultar API de rota: " + response.statusCode());
        }
    }


    private  void verificarInputsPedido(Double latitudeOrigem, Double longitudeOrigem,Double latitudeDestino,
                                    Double longitudeDestino, LocalDateTime dataPedido,NivelConforto conforto) throws IllegalArgumentException{

        if(latitudeOrigem==null||latitudeOrigem>90||latitudeOrigem<-90){
            throw new IllegalArgumentException("Erro: Latitude origem fora do range válido [-90,90].");
        }
        if(latitudeDestino==null||latitudeDestino>90||latitudeDestino<-90){
            throw new IllegalArgumentException("Erro: Latitude destino fora do range válido. [-90,90]");
        }
        if(longitudeOrigem==null||longitudeOrigem>180||longitudeOrigem<-180){
            throw new IllegalArgumentException("Erro: Longitude origem fora do range válido [-180,180].");
        }
        if(longitudeDestino==null||longitudeDestino>180||longitudeDestino<-180){
            throw new IllegalArgumentException("Erro: Longitude destino fora do range válido [-180,180].");
        }
        if(dataPedido==null){
            throw new IllegalArgumentException("Erro: A data do pedido não pode ser null.");
        }
        if(conforto==null){
            throw new IllegalArgumentException("Erro: O nivel de conforto não pode ser null");
        }

    }                        
    

}
