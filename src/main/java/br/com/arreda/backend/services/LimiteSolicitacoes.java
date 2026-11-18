package br.com.arreda.backend.services;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Limite de solicitações por chave (ex.: e-mail) dentro de uma janela de tempo.
 * Seguro para requisições simultâneas: cada chave é atualizada de forma atômica
 * via {@link ConcurrentHashMap#compute}, e chaves sem solicitações recentes são removidas.
 */
// ponytail: limite em memória. Zera no restart e vale apenas para 1 instância;
// migrar para tabela/Redis quando o backend rodar em mais de uma instância.
public class LimiteSolicitacoes {

    private final int limite;
    private final Duration janela;
    private final ConcurrentHashMap<String, Deque<Instant>> solicitacoesPorChave = new ConcurrentHashMap<>();
    private final AtomicReference<Instant> ultimaLimpeza = new AtomicReference<>(Instant.now());

    public LimiteSolicitacoes(int limite, Duration janela) {
        this.limite = limite;
        this.janela = janela;
    }

    /**
     * Registra a solicitação se a chave ainda estiver dentro do limite.
     *
     * @return true quando a solicitação foi aceita; false quando o limite foi atingido.
     */
    public boolean tentarRegistrar(String chave) {
        Instant agora = Instant.now();
        Instant inicioDaJanela = agora.minus(janela);
        boolean[] aceita = {false};

        solicitacoesPorChave.compute(normalizar(chave), (k, solicitacoes) -> {
            Deque<Instant> recentes = solicitacoes == null ? new ArrayDeque<>() : solicitacoes;
            recentes.removeIf(data -> !data.isAfter(inicioDaJanela));
            if (recentes.size() < limite) {
                recentes.addLast(agora);
                aceita[0] = true;
            }
            return recentes.isEmpty() ? null : recentes;
        });

        limparChavesExpiradas(agora, inicioDaJanela);
        return aceita[0];
    }

    /** Quantidade de chaves em memória (usado nos testes para garantir que não cresce sem limite). */
    int quantidadeDeChaves() {
        return solicitacoesPorChave.size();
    }

    /** No máximo uma varredura por janela, para que e-mails nunca repetidos não fiquem para sempre. */
    private void limparChavesExpiradas(Instant agora, Instant inicioDaJanela) {
        Instant anterior = ultimaLimpeza.get();
        if (anterior.isAfter(inicioDaJanela) || !ultimaLimpeza.compareAndSet(anterior, agora)) {
            return;
        }
        for (String chave : solicitacoesPorChave.keySet()) {
            solicitacoesPorChave.computeIfPresent(chave, (k, solicitacoes) -> {
                solicitacoes.removeIf(data -> !data.isAfter(inicioDaJanela));
                return solicitacoes.isEmpty() ? null : solicitacoes;
            });
        }
    }

    private static String normalizar(String chave) {
        return chave.trim().toLowerCase();
    }
}
