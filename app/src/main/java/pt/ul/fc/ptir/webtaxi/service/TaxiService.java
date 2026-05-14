package pt.ul.fc.ptir.webtaxi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.transaction.annotation.Transactional;
import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.entity.enums.NivelConforto;
import pt.ul.fc.ptir.webtaxi.entity.enums.TipoMotor;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEditavelException;
import pt.ul.fc.ptir.webtaxi.exception.RecursoNaoEncontradoException;
import pt.ul.fc.ptir.webtaxi.repository.ConfortoRepository;
import pt.ul.fc.ptir.webtaxi.dto.TaxiCreateDTO;
import pt.ul.fc.ptir.webtaxi.dto.TaxiResponseDTO;
import pt.ul.fc.ptir.webtaxi.entity.Taxi;
import pt.ul.fc.ptir.webtaxi.mapper.Mapper;
import pt.ul.fc.ptir.webtaxi.repository.TaxiRepository;
import pt.ul.fc.ptir.webtaxi.repository.TurnoRepository;
import pt.ul.fc.ptir.webtaxi.repository.ViagemRepository;

@Service
public class TaxiService {

    private final TaxiRepository taxiRepository;
    private final ConfortoRepository confortoRepository;
    private final TurnoRepository turnoRepository;
    private final ViagemRepository viagemRepository;
    

    @Autowired
    public TaxiService(TaxiRepository taxiRepository, ConfortoRepository confortoRepository, TurnoRepository turnoRepository, ViagemRepository viagemRepository) {
        this.taxiRepository = taxiRepository;
        this.confortoRepository = confortoRepository;
        this.turnoRepository = turnoRepository;
        this.viagemRepository = viagemRepository;
    }

    public List<TaxiResponseDTO> listarTaxis() {
        List<Taxi> taxis = taxiRepository.findAll();
        if (taxis.isEmpty())
            throw new RecursoNaoEncontradoException("Não há táxis registados.");
        return taxis.stream().map(Mapper::converterParaTaxiDTO).toList();
    }

    @Transactional
    public TaxiResponseDTO criarTaxi(TaxiCreateDTO taxiCreateDTO) {
        validarTaxi(taxiCreateDTO);

        Conforto conforto = confortoRepository.findByNivelConforto(NivelConforto.valueOf(taxiCreateDTO.nivelConforto())).get();

        Taxi taxi = Mapper.converterParaTaxi(taxiCreateDTO, conforto);

        if (taxiRepository.existsByMatricula(taxi.getMatricula()))
            throw new IllegalStateException("Já existe um táxi com esta matrícula. (matrícula " + taxi.getMatricula() + ")");

        taxi.setCriadoEm(LocalDateTime.now());
        taxi.setAtualizadoEm(LocalDateTime.now());
        taxi = taxiRepository.save(taxi);
        return Mapper.converterParaTaxiDTO(taxi);
    }

    @Transactional
    public TaxiResponseDTO obterTaxi(Long id) {
        Taxi taxi = taxiRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Táxi não encontrado com id: " + id));
        return Mapper.converterParaTaxiDTO(taxi);
    }

    // TODO: depois mudar isso para não precisar dos ifs (alterar lógica de patch)
    @Transactional
    public TaxiResponseDTO editarTaxi(Long id, TaxiCreateDTO taxiCreateDTO) {

        Taxi taxi = taxiRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Táxi não encontrado com id: " + id));
        validarTaxi(taxiCreateDTO);

        // só permite editar o conforto caso este táxi nunca tenha feito uma viagem
        if (!viagemRepository.existeTaxiEmViagens(id)) {
            Conforto conforto = confortoRepository.findByNivelConforto(NivelConforto.valueOf(taxiCreateDTO.nivelConforto()))
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Nível de conforto não existe: " + taxiCreateDTO.nivelConforto()));
            taxi.setConforto(conforto);
        }
        taxi.setKilometros(taxiCreateDTO.kilometros());

        taxi.setAtualizadoEm(LocalDateTime.now());
        taxi = taxiRepository.save(taxi);
        return Mapper.converterParaTaxiDTO(taxi);
    }

    @Transactional
    public void removerTaxi(Long id) {
        // verificar se o taxi já foi requisitado para algum turno
        if (turnoRepository.existeTaxiEmTurnos(id))
            throw new RecursoNaoEditavelException("Táxi já foi requisitado em algum turno.");
        Taxi taxi = taxiRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Táxi não encontrado com id: " + id));
        taxiRepository.delete(taxi);
    }

    public void validarTaxi(TaxiCreateDTO taxiCreateDTO) throws IllegalArgumentException {
        if(taxiCreateDTO.nivelConforto()==null || taxiCreateDTO.nivelConforto().isBlank()){
            throw new IllegalArgumentException("Erro: o nivel de conforto não pode ser null");
        }

        if(!isMatriculaValid(taxiCreateDTO.matricula())){
            throw new IllegalArgumentException("Erro: A estrutura da matricula do taxi é inválida.");
        }

        if(taxiCreateDTO.modelo().isBlank() || taxiCreateDTO.modelo()==null){
            throw new IllegalArgumentException("Erro: O modelo do taxi não pode ser vazio ou null");
        }
        if(taxiCreateDTO.marca().isBlank() || taxiCreateDTO.marca()==null){
            throw new IllegalArgumentException("Erro: A marca do taxi não pode ser vazio ou null");
        }
        if(taxiCreateDTO.tipoMotor()==null || taxiCreateDTO.tipoMotor().isBlank()){
            throw new IllegalArgumentException("Erro: O tipo de motor do taxi não pode ser vazio ou null");
        }

        try{
            TipoMotor.valueOf(taxiCreateDTO.tipoMotor().toUpperCase());
        }
        catch(Exception e){
            throw new IllegalArgumentException("Erro: O tipo de motor de um taxi tem de ser:  COMBUSTAO ou ELETRICO");
        }

        if(taxiCreateDTO.anoCompra()<=1900||taxiCreateDTO.anoCompra()==null){
            throw new IllegalArgumentException("Erro: O Ano de compra do taxi tem de ser um número inteiro maior que 1900.");
        }
        Year anocompra = Year.parse(taxiCreateDTO.anoCompra().toString());
        Year anoAtual = Year.now();
        if(!anocompra.isBefore(anoAtual)){
            throw new IllegalArgumentException("Erro: O ano de compra do taxi tem de ser menor ou igual a "+anoAtual);
        }


        if(taxiCreateDTO.nivelConforto()==null || taxiCreateDTO.nivelConforto().isBlank()){
            throw new IllegalArgumentException("Erro: O nivel de conforto do taxi não pode ser vazio ou null.");
        }
        try{
            NivelConforto.valueOf(taxiCreateDTO.nivelConforto().toUpperCase());
        }catch(Exception e){
            throw new IllegalArgumentException("Erro: O nivel de conforto do taxi é invalido. Valores válidos: BASICO, CONFORTO, LUXUOSO");
        }
        confortoRepository.findByNivelConforto(NivelConforto.valueOf(taxiCreateDTO.nivelConforto().toUpperCase()))
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conforto não encontrado."));

        if(taxiCreateDTO.kilometros()==null||taxiCreateDTO.kilometros()<0){
            throw new IllegalArgumentException("Erro: Os kilometros do taxi têm de ser um número inteiro maior ou igual a 0.");
        }

    }

    private boolean isMatriculaValid(String matricula){
        if (matricula == null){
            return false;
        }
        String pattern_matricula = "^([A-Z]{2}-\\d{2}-[A-Z]{2}|\\d{2}-[A-Z]{2}-\\d{2}|\\d{2}-\\d{2}-[A-Z]{2}|[A-Z]{2}-\\d{2}-\\d{2})$";
        Pattern pattern =Pattern.compile(pattern_matricula);

        return pattern.matcher(matricula.toUpperCase()).matches();
    }
}
