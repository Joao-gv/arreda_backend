package br.com.arreda.backend.services;

import java.io.IOException;

public interface ArmazenamentoFotoPerfil {
    String salvar(byte[] conteudo) throws IOException;

    byte[] ler(String chave) throws IOException;

    void excluir(String chave) throws IOException;
}
