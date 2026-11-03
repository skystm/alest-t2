import java.util.Random;

public class SimuladorHelpDesk {

    // =========================================================
    //  PARÂMETROS CONFIGURÁVEIS — altere aqui sem tocar na lógica
    // =========================================================

    // Semente para reprodutibilidade (use -1 para aleatório)
    static final long SEED = 42;

    // Número total de rodadas da simulação
    static final int TOTAL_RODADAS = 100;

    // --- Fila de entrada ---
    static final int CAPACIDADE_FILA_ENTRADA = 20;
    // Máximo de chamados que podem chegar por rodada (sorteia 0..MAX_CHEGADA)
    static final int MAX_CHEGADA_POR_RODADA = 3;

    // --- Classificação ---
    static final int NUM_CLASSIFICADORES  = 2;
    static final int TEMPO_CLASSIFICACAO  = 2; // rodadas

    // --- Processamento ---
    static final int NUM_ATENDENTES = 3;
    // Índice 0 não usado; 1=CRITICO, 2=ALTO, 3=NORMAL
    static final int[] TEMPO_ATENDIMENTO = {0, 4, 3, 2};

    // --- Probabilidades de prioridade (devem somar 1.0) ---
    static final double PROB_CRITICO = 0.20;
    static final double PROB_ALTO    = 0.35;
    // PROB_NORMAL = 1 - PROB_CRITICO - PROB_ALTO

    // --- Arquivo de saída CSV ---
    static final String ARQUIVO_CSV = "metricas.csv";

    // =========================================================

    public static void main(String[] args) {

        Random rng = (SEED == -1) ? new Random() : new Random(SEED);

        // Filas
        Fila filaEntrada = new Fila(CAPACIDADE_FILA_ENTRADA);
        Fila filaCritico = new Fila();
        Fila filaAlto    = new Fila();
        Fila filaNormal  = new Fila();

        // Recursos
        Classificador[] classificadores = new Classificador[NUM_CLASSIFICADORES];
        for (int i = 0; i < NUM_CLASSIFICADORES; i++)
            classificadores[i] = new Classificador(i + 1, TEMPO_CLASSIFICACAO);

        Atendente[] atendentes = new Atendente[NUM_ATENDENTES];
        for (int i = 0; i < NUM_ATENDENTES; i++)
            atendentes[i] = new Atendente(i + 1, TEMPO_ATENDIMENTO);

        Metricas metricas = new Metricas();
        metricas.setNumAtendentes(NUM_ATENDENTES);

        int contadorId = 1;

        System.out.println("=== Simulação Help Desk — ALEST I ===");
        System.out.printf("Seed: %d | Rodadas: %d | Atendentes: %d | Classificadores: %d%n%n",
            SEED, TOTAL_RODADAS, NUM_ATENDENTES, NUM_CLASSIFICADORES);

        for (int rodada = 1; rodada <= TOTAL_RODADAS; rodada++) {

            // ------------------------------------------------
            // PASSO 1 — Tick: avança estado de todos os recursos
            // ------------------------------------------------

            // Classificadores que terminam devem ser coletados depois do tick
            Chamado[] classificadosAgora = new Chamado[NUM_CLASSIFICADORES];
            for (int i = 0; i < NUM_CLASSIFICADORES; i++)
                classificadosAgora[i] = classificadores[i].tick();

            Chamado[] concluidosAgora = new Chamado[NUM_ATENDENTES];
            for (int i = 0; i < NUM_ATENDENTES; i++)
                concluidosAgora[i] = atendentes[i].tick();

            // ------------------------------------------------
            // PASSO 2 — Move chamados classificados para fila de prioridade
            // ------------------------------------------------
            for (int i = 0; i < NUM_CLASSIFICADORES; i++) {
                if (classificadosAgora[i] != null) {
                    Chamado c = classificadosAgora[i];
                    // Atribui prioridade aleatória segundo as probabilidades
                    double sorteio = rng.nextDouble();
                    if (sorteio < PROB_CRITICO)
                        c.prioridade = Chamado.CRITICO;
                    else if (sorteio < PROB_CRITICO + PROB_ALTO)
                        c.prioridade = Chamado.ALTO;
                    else
                        c.prioridade = Chamado.NORMAL;

                    enfileirarPorPrioridade(c, filaCritico, filaAlto, filaNormal);
                    metricas.registrarClassificado();
                }
            }

            // ------------------------------------------------
            // PASSO 3 — Registra atendimentos concluídos
            // ------------------------------------------------
            int concluidosNaRodada = 0;
            for (int i = 0; i < NUM_ATENDENTES; i++) {
                if (concluidosAgora[i] != null) {
                    metricas.registrarConcluido(concluidosAgora[i]);
                    concluidosNaRodada++;
                }
            }

            // ------------------------------------------------
            // PASSO 4 — Entrada: novos chamados chegam
            // ------------------------------------------------
            int numChegando = rng.nextInt(MAX_CHEGADA_POR_RODADA + 1);
            metricas.registrarChegada(numChegando);
            for (int k = 0; k < numChegando; k++) {
                String id   = String.format("CHAMADO-%03d", contadorId++);
                Chamado novo = new Chamado(id, rodada);
                boolean aceito = filaEntrada.enqueue(novo);
                if (!aceito) metricas.registrarRecusado();
            }

            // ------------------------------------------------
            // PASSO 5 — Classificadores livres pegam da fila de entrada
            // ------------------------------------------------
            for (int i = 0; i < NUM_CLASSIFICADORES; i++) {
                if (classificadores[i].isLivre() && !filaEntrada.isEmpty()) {
                    Chamado c = filaEntrada.dequeue();
                    classificadores[i].iniciar(c, rodada);
                }
            }

            // ------------------------------------------------
            // PASSO 6 — Atendentes livres pegam da fila de maior prioridade
            // ------------------------------------------------
            for (int i = 0; i < NUM_ATENDENTES; i++) {
                if (atendentes[i].isLivre()) {
                    Chamado c = proximoDaFilaDeMaiorPrioridade(filaCritico, filaAlto, filaNormal);
                    if (c != null) atendentes[i].iniciar(c, rodada);
                }
            }

            // ------------------------------------------------
            // PASSO 7 — Coleta métricas e imprime log da rodada
            // ------------------------------------------------
            int atendOcup = 0;
            for (Atendente a : atendentes)
                if (!a.isLivre()) atendOcup++;

            metricas.registrarRodada(rodada,
                filaEntrada.getTamanho(),
                filaCritico.getTamanho(),
                filaAlto.getTamanho(),
                filaNormal.getTamanho(),
                concluidosNaRodada,
                atendOcup);

            System.out.printf("[R%3d] Chegaram:%-2d | FilaEnt:%-2d | C:%-2d A:%-2d N:%-2d | Concluídos:%-2d | Atend.ocup:%d/%d%n",
                rodada, numChegando,
                filaEntrada.getTamanho(),
                filaCritico.getTamanho(), filaAlto.getTamanho(), filaNormal.getTamanho(),
                concluidosNaRodada,
                atendOcup, NUM_ATENDENTES);
        }

        // ------------------------------------------------
        // Fim da simulação — estado residual
        // ------------------------------------------------
        System.out.println("\n--- Demandas ainda no sistema ao fim da simulação ---");
        System.out.println("Fila de entrada    : " + filaEntrada.getTamanho() + " chamado(s)");
        System.out.println("Fila CRITICO       : " + filaCritico.getTamanho() + " chamado(s)");
        System.out.println("Fila ALTO          : " + filaAlto.getTamanho() + " chamado(s)");
        System.out.println("Fila NORMAL        : " + filaNormal.getTamanho() + " chamado(s)");

        int emClassificacao = 0;
        for (Classificador cl : classificadores)
            if (!cl.isLivre()) emClassificacao++;
        System.out.println("Em classificação   : " + emClassificacao + " chamado(s)");

        int emAtendimento = 0;
        for (Atendente a : atendentes)
            if (!a.isLivre()) emAtendimento++;
        System.out.println("Em atendimento     : " + emAtendimento + " chamado(s)");

        metricas.imprimirResumo();
        metricas.exportarCSV(ARQUIVO_CSV);
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private static void enfileirarPorPrioridade(Chamado c, Fila fc, Fila fa, Fila fn) {
        switch (c.prioridade) {
            case Chamado.CRITICO: fc.enqueue(c); break;
            case Chamado.ALTO:    fa.enqueue(c); break;
            default:              fn.enqueue(c); break;
        }
    }

    private static Chamado proximoDaFilaDeMaiorPrioridade(Fila fc, Fila fa, Fila fn) {
        if (!fc.isEmpty()) return fc.dequeue();
        if (!fa.isEmpty()) return fa.dequeue();
        if (!fn.isEmpty()) return fn.dequeue();
        return null;
    }
}
