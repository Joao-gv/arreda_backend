package br.com.arreda.backend.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@RestControllerAdvice
@Hidden
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosErroValidacao>> tratarErro400(MethodArgumentNotValidException ex) {
        var erros = ex.getFieldErrors();
        return ResponseEntity.badRequest().body(erros.stream().map(DadosErroValidacao::new).toList());
    }


    public record DadosErroValidacao(String campo, String mensagem) {
        public DadosErroValidacao(org.springframework.validation.FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }
}
