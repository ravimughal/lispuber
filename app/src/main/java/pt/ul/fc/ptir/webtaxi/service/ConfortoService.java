package pt.ul.fc.ptir.webtaxi.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.repository.ConfortoRepository;



@Service
@Transactional
public class ConfortoService {

    @Autowired
    private ConfortoRepository confortoRepository;
    
    @Autowired
    public ConfortoService(ConfortoRepository confortoRepository){
        this.confortoRepository=confortoRepository;        
    }

   @Transactional
    public List<Conforto> getAllConfortos() {
        return confortoRepository.findAll();
    }
}
