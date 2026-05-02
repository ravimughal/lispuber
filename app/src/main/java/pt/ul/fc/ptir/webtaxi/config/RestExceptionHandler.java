package pt.ul.fc.ptir.webtaxi.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pt.ul.fc.ptir.webtaxi.exception.*;
import org.springframework.security.access.AccessDeniedException;

// Trata das exceções no contexto do serviço restful

@RestControllerAdvice(basePackages = "pt.ul.fc.ptir.webtaxi.controller")
public class RestExceptionHandler {

    @ExceptionHandler(DataInicioDepoisDoFimException.class)
    public ResponseEntity<String> tratarDataInicioDepoisDoFim(DataInicioDepoisDoFimException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<String> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> tratarIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> tratarAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEditavelException.class)
    public ResponseEntity<String> tratarRecursoNaoEditavel(RecursoNaoEditavelException e) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(e.getMessage());
    }
}