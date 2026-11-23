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
    // Uso: java SimuladorHelpDesk [csv=arquivo] [atendentes=N]
    //      [classificadores=N] [capacidade=N] [rodadas=N] [seed=N]
    //      [maxchegada=N] [tempoclassif=N]
    // =========================================================

    public static void main(String[] args) {

        // leitura de overrides via linha de comando
        long   seed             = SEED;
        int    totalRodadas     = TOTAL_RODADAS;
        int    capFilaEntrada   = CAPACIDADE_FILA_ENTRADA;
        int    maxChegada       = MAX_CHEGADA_POR_RODADA;
        int    numClass         = NUM_CLASSIFICADORES;
        int    tempoClass       = TEMPO_CLASSIFICACAO;
        int    numAtend         = NUM_ATENDENTES;
        int[]  tempoAtend       = TEMPO_ATENDIMENTO.clone();
        String arquivoCsv       = ARQUIVO_CSV;

        for (String arg : args) {
            String[] kv = arg.split("=", 2);
            if (kv.length != 2) continue;
            switch (kv[0].toLowerCase()) {
                case "seed":            seed           = Long.parseLong(kv[1]);   break;
                case "rodadas":         totalRodadas   = Integer.parseInt(kv[1]); break;
                case "capacidade":      capFilaEntrada = Integer.parseInt(kv[1]); break;
                case "maxchegada":      maxChegada     = Integer.parseInt(kv[1]); break;
                case "classificadores": numClass       = Integer.parseInt(kv[1]); break;
                case "tempoclassif":    tempoClass     = Integer.parseInt(kv[1]); break;
                case "atendentes":      numAtend       = Integer.parseInt(kv[1]); break;
                case "csv":             arquivoCsv     = kv[1];                   break;
            }
        }

        if (totalRodadas < 1) {
            throw new IllegalArgumentException("rodadas deve ser maior ou igual a 1");
        }
        if (capFilaEntrada < 1) {
            throw new IllegalArgumentException("capacidade deve ser maior ou igual a 1");
        }
        if (maxChegada < 0) {
            throw new IllegalArgumentException("maxchegada deve ser maior ou igual a 0");
        }
        if (numClass < 1) {
            throw new IllegalArgumentException("classificadores deve ser maior ou igual a 1");
        }
        if (tempoClass < 1) {
            throw new IllegalArgumentException("tempoclassif deve ser maior ou igual a 1");
        }
        if (numAtend < 1) {
            throw new IllegalArgumentException("atendentes deve ser maior ou igual a 1");
        }

        Random rng = (seed == -1) ? new Random() : new Random(seed);

        final double probCritico = PROB_CRITICO;
        final double probAlto    = PROB_ALTO;

        // Filas
        Fila filaEntrada = new Fila(capFilaEntrada);
        Fila filaCritico = new Fila();
        Fila filaAlto    = new Fila();
        Fila filaNormal  = new Fila();

        // Recursos
        Classificador[] classificadores = new Classificador[numClass];
        for (int i = 0; i < numClass; i++)
            classificadores[i] = new Classificador(i + 1, tempoClass);

        Atendente[] atendentes = new Atendente[numAtend];
        for (int i = 0; i < numAtend; i++)
            atendentes[i] = new Atendente(i + 1, tempoAtend);

        Metricas metricas = new Metricas();
        metricas.setNumAtendentes(numAtend);

        int contadorId = 1;

        System.out.println("=== Simulacao Help Desk - ALEST I ===");
        System.out.printf("Seed: %d | Rodadas: %d | Atendentes: %d | Classificadores: %d | CapFilaEnt: %d%n%n",
            seed, totalRodadas, numAtend, numClass, capFilaEntrada);

        for (int rodada = 1; rodada <= totalRodadas; rodada++) {

            // ------------------------------------------------
            // PASSO 1 — Tick: avança estado de todos os recursos
            // ------------------------------------------------
            Chamado[] classificadosAgora = new Chamado[numClass];
            for (int i = 0; i < numClass; i++)
                classificadosAgora[i] = classificadores[i].tick();

            Chamado[] concluidosAgora = new Chamado[numAtend];
            for (int i = 0; i < numAtend; i++)
                concluidosAgora[i] = atendentes[i].tick();

            // ------------------------------------------------
            // PASSO 2 — Move chamados classificados para fila de prioridade
            // ------------------------------------------------
            for (int i = 0; i < numClass; i++) {
                if (classificadosAgora[i] != null) {
                    Chamado c = classificadosAgora[i];
                    double sorteio = rng.nextDouble();
                    if (sorteio < probCritico)
                        c.prioridade = Chamado.CRITICO;
                    else if (sorteio < probCritico + probAlto)
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
            for (int i = 0; i < numAtend; i++) {
                if (concluidosAgora[i] != null) {
                    metricas.registrarConcluido(concluidosAgora[i]);
                    concluidosNaRodada++;
                }
            }

            // ------------------------------------------------
            // PASSO 4 — Entrada: novos chamados chegam
            // ------------------------------------------------
            int numChegando = rng.nextInt(maxChegada + 1);
            metricas.registrarChegada(numChegando);
            for (int k = 0; k < numChegando; k++) {
                String id    = String.format("CHAMADO-%03d", contadorId++);
                Chamado novo = new Chamado(id, rodada);
                boolean aceito = filaEntrada.enqueue(novo);
                if (!aceito) metricas.registrarRecusado();
            }

            // ------------------------------------------------
            // PASSO 5 — Classificadores livres pegam da fila de entrada
            // ------------------------------------------------
            for (int i = 0; i < numClass; i++) {
                if (classificadores[i].isLivre() && !filaEntrada.isEmpty()) {
                    Chamado c = filaEntrada.dequeue();
                    classificadores[i].iniciar(c, rodada);
                }
            }

            // ------------------------------------------------
            // PASSO 6 — Atendentes livres pegam da fila de maior prioridade
            // ------------------------------------------------
            for (int i = 0; i < numAtend; i++) {
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

            System.out.printf("[R%3d] Chegaram:%-2d | FilaEnt:%-2d | C:%-2d A:%-2d N:%-2d | Concluidos:%-2d | Atend.ocup:%d/%d%n",
                rodada, numChegando,
                filaEntrada.getTamanho(),
                filaCritico.getTamanho(), filaAlto.getTamanho(), filaNormal.getTamanho(),
                concluidosNaRodada,
                atendOcup, numAtend);
        }

        // ------------------------------------------------
        // Fim da simulação — estado residual
        // ------------------------------------------------
        System.out.println("\n--- Demandas ainda no sistema ao fim da simulacao ---");
        System.out.println("Fila de entrada    : " + filaEntrada.getTamanho() + " chamado(s)");
        System.out.println("Fila CRITICO       : " + filaCritico.getTamanho() + " chamado(s)");
        System.out.println("Fila ALTO          : " + filaAlto.getTamanho() + " chamado(s)");
        System.out.println("Fila NORMAL        : " + filaNormal.getTamanho() + " chamado(s)");

        int emClassificacao = 0;
        for (Classificador cl : classificadores)
            if (!cl.isLivre()) emClassificacao++;
        System.out.println("Em classificacao   : " + emClassificacao + " chamado(s)");

        int emAtendimento = 0;
        for (Atendente a : atendentes)
            if (!a.isLivre()) emAtendimento++;
        System.out.println("Em atendimento     : " + emAtendimento + " chamado(s)");

        metricas.imprimirResumo();
        metricas.exportarCSV(arquivoCsv);
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
