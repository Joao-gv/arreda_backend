package br.com.arreda.backend.services;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimiteSolicitacoesTest {

    @Test
    void aceitaAteOLimiteEBloqueiaDepois_ignorandoCaixaEEspacos() {
        LimiteSolicitacoes limite = new LimiteSolicitacoes(2, Duration.ofMinutes(15));

        assertTrue(limite.tentarRegistrar("Ana@Exemplo.com"));
        assertTrue(limite.tentarRegistrar("  ana@exemplo.com "));
        assertFalse(limite.tentarRegistrar("ana@exemplo.com"));
        assertTrue(limite.tentarRegistrar("outra@exemplo.com"), "cada chave tem a sua própria contagem");
    }

    @Test
    void liberaNovamenteDepoisDaJanela() throws InterruptedException {
        LimiteSolicitacoes limite = new LimiteSolicitacoes(1, Duration.ofMillis(50));

        assertTrue(limite.tentarRegistrar("ana@exemplo.com"));
        assertFalse(limite.tentarRegistrar("ana@exemplo.com"));
        Thread.sleep(80);
        assertTrue(limite.tentarRegistrar("ana@exemplo.com"));
    }

    @Test
    void chavesExpiradasSaoRemovidasDaMemoria() throws InterruptedException {
        LimiteSolicitacoes limite = new LimiteSolicitacoes(1, Duration.ofMillis(50));
        for (int i = 0; i < 100; i++) {
            limite.tentarRegistrar("spam-" + i + "@exemplo.com");
        }
        assertEquals(100, limite.quantidadeDeChaves());

        Thread.sleep(80);
        limite.tentarRegistrar("nova@exemplo.com");

        assertEquals(1, limite.quantidadeDeChaves(), "só a chave recente deve continuar em memória");
    }

    @Test
    void requisicoesSimultaneasNaoUltrapassamOLimite() throws Exception {
        LimiteSolicitacoes limite = new LimiteSolicitacoes(3, Duration.ofMinutes(15));
        int threads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                Callable<Boolean> tarefa = () -> {
                    largada.await();
                    return limite.tentarRegistrar("concorrente@exemplo.com");
                };
                resultados.add(executor.submit(tarefa));
            }
            largada.countDown();

            long aceitas = 0;
            for (Future<Boolean> resultado : resultados) {
                if (resultado.get()) {
                    aceitas++;
                }
            }
            assertEquals(3, aceitas);
        } finally {
            executor.shutdownNow();
        }
    }
}
