import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Metricas {

    // --- contadores globais ---
    private int totalChegaram      = 0;
    private int totalRecusados     = 0;
    private int totalClassificados = 0;
    private int totalConcluidos    = 0;

    // --- tempos acumulados para médias ---
    private long somaEsperaFila         = 0; // rodadaInicioClass - rodadaChegada
    private int  contEsperaFila         = 0;

    private long somaEsperaPosClass     = 0; // rodadaInicioAtend - rodadaFimClass
    private int  contEsperaPosClass     = 0;

    private long[] somaAtendPorPrio     = new long[4]; // índice 1,2,3
    private int[]  contAtendPorPrio     = new int[4];

    // --- série temporal por rodada ---
    private static class SnapshotRodada {
        int rodada, filaEntrada, filaCritico, filaAlto, filaNormal, concluidosNaRodada;
    }
    private final List<SnapshotRodada> historico = new ArrayList<>();

    // --- ocupação dos atendentes ---
    private int rodadasTotais       = 0;
    private int somaAtendentesOcup  = 0; // soma de quantos atendentes estavam ocupados a cada rodada
    private int numAtendentes       = 0;

    public void setNumAtendentes(int n) {
        this.numAtendentes = n;
    }

    // --- registro por rodada ---

    public void registrarChegada(int qtd) {
        totalChegaram += qtd;
    }

    public void registrarRecusado() {
        totalRecusados++;
    }

    public void registrarClassificado() {
        totalClassificados++;
    }

    public void registrarConcluido(Chamado c) {
        totalConcluidos++;
        if (c.rodadaInicioClassificacao >= 0 && c.rodadaChegada >= 0) {
            somaEsperaFila += c.rodadaInicioClassificacao - c.rodadaChegada;
            contEsperaFila++;
        }
        if (c.rodadaFimClassificacao >= 0 && c.rodadaInicioAtendimento >= 0) {
            somaEsperaPosClass += c.rodadaInicioAtendimento - c.rodadaFimClassificacao;
            contEsperaPosClass++;
        }
        if (c.prioridade >= 1 && c.prioridade <= 3) {
            somaAtendPorPrio[c.prioridade] += c.tempoAtendimento();
            contAtendPorPrio[c.prioridade]++;
        }
    }

    public void registrarRodada(int rodada, int fe, int fc, int fa, int fn, int concluidos, int atendOcup) {
        SnapshotRodada s      = new SnapshotRodada();
        s.rodada              = rodada;
        s.filaEntrada         = fe;
        s.filaCritico         = fc;
        s.filaAlto            = fa;
        s.filaNormal          = fn;
        s.concluidosNaRodada  = concluidos;
        historico.add(s);

        rodadasTotais++;
        somaAtendentesOcup += atendOcup;
    }

    // --- resumo final ---

    public void imprimirResumo() {
        System.out.println("\n========== RESUMO FINAL ==========");
        System.out.println("Chamados chegaram   : " + totalChegaram);
        System.out.println("Chamados recusados  : " + totalRecusados
            + " (" + pct(totalRecusados, totalChegaram) + "%)");
        System.out.println("Chamados classificados: " + totalClassificados);
        System.out.println("Chamados concluídos : " + totalConcluidos);
        System.out.println();

        System.out.printf("Espera média fila entrada    : %.2f rodadas%n",
            media(somaEsperaFila, contEsperaFila));
        System.out.printf("Espera média pós-classificação: %.2f rodadas%n",
            media(somaEsperaPosClass, contEsperaPosClass));
        System.out.println();

        String[] nomes = {"", "CRITICO", "ALTO", "NORMAL"};
        for (int p = 1; p <= 3; p++) {
            System.out.printf("Tempo médio atend. %-7s: %.2f rodadas (n=%d)%n",
                nomes[p], media(somaAtendPorPrio[p], contAtendPorPrio[p]), contAtendPorPrio[p]);
        }
        System.out.println();

        double ocup = numAtendentes > 0
            ? (double) somaAtendentesOcup / (numAtendentes * rodadasTotais) * 100.0
            : 0;
        System.out.printf("Ocupação média atendentes   : %.1f%%%n", ocup);
        System.out.println("==================================");
    }

    public void exportarCSV(String caminho) {
        try (FileWriter fw = new FileWriter(caminho)) {
            fw.write("rodada,filaEntrada,filaCritico,filaAlto,filaNormal,concluidosNaRodada\n");
            for (SnapshotRodada s : historico) {
                fw.write(s.rodada + "," + s.filaEntrada + "," + s.filaCritico + ","
                    + s.filaAlto + "," + s.filaNormal + "," + s.concluidosNaRodada + "\n");
            }
            System.out.println("\nCSV exportado: " + caminho);
        } catch (IOException e) {
            System.err.println("Erro ao exportar CSV: " + e.getMessage());
        }
    }

    // --- helpers ---

    private double media(long soma, int cont) {
        return cont == 0 ? 0.0 : (double) soma / cont;
    }

    private String pct(int parte, int total) {
        if (total == 0) return "0.0";
        return String.format("%.1f", (double) parte / total * 100.0);
    }
}
