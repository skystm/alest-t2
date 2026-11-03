public class Atendente {

    private final int   id;
    private final int[] temposPorPrioridade; // índice 0=não usado, 1=CRITICO, 2=ALTO, 3=NORMAL
    private Chamado     chamadoAtual;
    private int         rodadasRestantes;

    public Atendente(int id, int[] temposPorPrioridade) {
        this.id                  = id;
        this.temposPorPrioridade = temposPorPrioridade;
        this.chamadoAtual        = null;
        this.rodadasRestantes    = 0;
    }

    public boolean isLivre() {
        return chamadoAtual == null;
    }

    public void iniciar(Chamado c, int rodadaAtual) {
        chamadoAtual                   = c;
        int duracao                    = temposPorPrioridade[c.prioridade];
        rodadasRestantes               = duracao;
        c.rodadaInicioAtendimento      = rodadaAtual;
        c.rodadaFimAtendimento         = rodadaAtual + duracao;
    }

    /**
     * Avança uma rodada. Retorna o chamado se terminou, null caso contrário.
     */
    public Chamado tick() {
        if (chamadoAtual == null) return null;
        rodadasRestantes--;
        if (rodadasRestantes <= 0) {
            Chamado concluido = chamadoAtual;
            chamadoAtual      = null;
            return concluido;
        }
        return null;
    }

    public Chamado getChamadoAtual() {
        return chamadoAtual;
    }

    public int getId() {
        return id;
    }
}
