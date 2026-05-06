package pt.ul.fc.ptir.webtaxi.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ul.fc.ptir.webtaxi.entity.Conforto;
import pt.ul.fc.ptir.webtaxi.service.ConfortoService;


@RestController
@RequestMapping("/confortos")
public class ConfortoController {
    private final ConfortoService confortoService;

    @Autowired
    public ConfortoController(ConfortoService confortoService) {
        this.confortoService = confortoService;
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('GESTOR')")
    public List<Conforto> getConfortos() {
        return confortoService.getAllConfortos();
    }
}
