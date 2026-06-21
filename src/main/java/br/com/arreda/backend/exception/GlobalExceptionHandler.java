package br.com.arreda.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura nossos erros de negocio (Status 400 - Bad Request)
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroPadraoDTO> tratarErroRegraDeNegocio(RegraDeNegocioException ex){
        var erro = new ErroPadraoDTO(ex.getMessage(),HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    // Captura os erros de validação dos DTOs gerados pelo @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public  ResponseEntity<List<ErroValidacaoDTO>> tratarErrosdeValidacao(MethodArgumentNotValidException ex) {
       var erros = ex.getFieldErrors().stream()
               .map(erro -> new ErroValidacaoDTO(erro.getField(), erro.getDefaultMessage()))
               .toList();


       return ResponseEntity.badRequest().body(erros);
    }
    // Captura erros de JSON mal formatado ou requisição sem corpo
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErroPadraoDTO> tratarErroDeMensagemMalFormatada(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        var erro = new ErroPadraoDTO("Corpo da requisição ausente ou estrutura do JSON mal formatada.", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(erro);
    }

    // A Rede de Segurança Final: Captura QUALQUER erro inesperado que não mapeamos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoDTO> tratarErroGenerico(Exception ex) {
        // No console do servidor, a gente imprime o erro real para o desenvolvedor investigar
        ex.printStackTrace();

        // Para o cliente, devolvemos uma mensagem amigável sem expor o código
        var erro = new ErroPadraoDTO("Ocorreu um erro interno inesperado no servidor.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
