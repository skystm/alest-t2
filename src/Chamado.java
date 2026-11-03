public class Chamado {

    public static final int CRITICO = 1;
    public static final int ALTO    = 2;
    public static final int NORMAL  = 3;

    public final String id;
    public int prioridade;

    public int rodadaChegada               = -1;
    public int rodadaInicioClassificacao   = -1;
    public int rodadaFimClassificacao      = -1;
    public int rodadaInicioAtendimento     = -1;
    public int rodadaFimAtendimento        = -1;

    public Chamado(String id, int rodadaChegada) {
        this.id            = id;
        this.rodadaChegada = rodadaChegada;
    }

    public String nomePrioridade() {
        switch (prioridade) {
            case CRITICO: return "CRITICO";
            case ALTO:    return "ALTO";
            default:      return "NORMAL";
        }
    }

    public int tempoEsperaFila() {
        if (rodadaInicioClassificacao < 0) return -1;
        return rodadaInicioClassificacao - rodadaChegada;
    }

    public int tempoAtendimento() {
        if (rodadaInicioAtendimento < 0 || rodadaFimAtendimento < 0) return -1;
        return rodadaFimAtendimento - rodadaInicioAtendimento;
    }

    @Override
    public String toString() {
        return id + "(" + nomePrioridade() + ")";
    }
}
