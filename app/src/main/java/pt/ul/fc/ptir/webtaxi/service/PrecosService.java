package pt.ul.fc.ptir.webtaxi.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;

@Service
@Transactional
public class PrecosService {

    private final Double TAXA_CONFORTO=1.10;
    private final Double TAXA_LUXUOSO=1.25;

     private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
       
    @Autowired
    public PrecosService(){
        
    }

    @Transactional
    public Double getPrecoPedido(Double latitude_origem, Double longitude_origem,
                                 Double latitude_destino,Double longitude_destino,LocalDateTime dataPedido,NivelConforto conforto) throws Exception{
        
        // PREÇOS POR KILOMETRO, TENDO EM CONTA A HORA DO DIA
        // DIA = 1.5€ ao km
        // NOITE = 2.0€ ao km

        // taxas de conforto
        // Basico 0.0%
        // Conforto 10.0%
        // Luxo 25.0%
        LocalTime NOITE = LocalTime.of(19, 59);
                
        Double distancia_km=obterRota(latitude_origem,longitude_origem,latitude_destino,longitude_destino);
        System.out.println("Distancia km"+distancia_km);
        Double preco_final=distancia_km;

        LocalDateTime hourTemp= dataPedido;
        LocalTime time = hourTemp.toLocalTime();


        if(time.isAfter(NOITE)){
            preco_final*=2.0;
        }else{
            preco_final*=1.5;
        }


        if(conforto==NivelConforto.CONFORTO){
            preco_final*=TAXA_CONFORTO;
        }
        else if(conforto==NivelConforto.LUXUOSO){
            preco_final*=TAXA_LUXUOSO;
        }
        return preco_final;

    }
        


    public Double obterRota(Double latitude_origem, Double longitude_origem,
                                 Double latitude_destino,Double longitude_destino) throws Exception {

        String url = String.format(Locale.US,
            "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
            longitude_origem, latitude_origem, longitude_destino, latitude_destino
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String resposta = response.body();

            Pattern pattern = Pattern.compile("\"distance\":(\\d+\\.?\\d*)");
            Matcher matcher = pattern.matcher(resposta);

            if (matcher.find()) {
                double metros = Double.parseDouble(matcher.group(1));
                return metros / 1000; 
            }

        return 0.0;
        } else {
            throw new RuntimeException("Erro na API: " + response.statusCode());
        }
    }

}
