package br.com.arreda.backend.exception;

public class RegraDeNegocioException extends RuntimeException{
    public RegraDeNegocioException (String mensagem){
        super(mensagem);
    }
}
