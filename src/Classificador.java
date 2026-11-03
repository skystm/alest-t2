public class Classificador {

    private final int    id;
    private final int    tempoClassificacao;
    private Chamado      chamadoAtual;
    private int          rodadasRestantes;

    public Classificador(int id, int tempoClassificacao) {
        this.id                 = id;
        this.tempoClassificacao = tempoClassificacao;
        this.chamadoAtual       = null;
        this.rodadasRestantes   = 0;
    }

    public boolean isLivre() {
        return chamadoAtual == null;
    }

    public void iniciar(Chamado c, int rodadaAtual) {
        chamadoAtual                       = c;
        rodadasRestantes                   = tempoClassificacao;
        c.rodadaInicioClassificacao        = rodadaAtual;
        c.rodadaFimClassificacao           = rodadaAtual + tempoClassificacao;
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
