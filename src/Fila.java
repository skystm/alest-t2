public class Fila {

    private No    inicio;
    private No    fim;
    private int   tamanho;
    private final int capacidade;

    public Fila(int capacidade) {
        this.capacidade = capacidade;
        this.tamanho    = 0;
        this.inicio     = null;
        this.fim        = null;
    }

    /** Fila sem limite (usada para filas de prioridade). */
    public Fila() {
        this(Integer.MAX_VALUE);
    }

    public boolean isEmpty() {
        return tamanho == 0;
    }

    public boolean isFull() {
        return tamanho >= capacidade;
    }

    public int getTamanho() {
        return tamanho;
    }

    /**
     * Insere ao final. Retorna true se inserido, false se fila cheia.
     */
    public boolean enqueue(Chamado c) {
        if (isFull()) return false;
        No novo = new No(c);
        if (fim == null) {
            inicio = novo;
            fim    = novo;
        } else {
            fim.proximo = novo;
            fim         = novo;
        }
        tamanho++;
        return true;
    }

    /**
     * Remove e retorna o primeiro. Retorna null se vazia.
     */
    public Chamado dequeue() {
        if (isEmpty()) return null;
        Chamado dado = inicio.dado;
        inicio = inicio.proximo;
        if (inicio == null) fim = null;
        tamanho--;
        return dado;
    }

    public Chamado peek() {
        if (isEmpty()) return null;
        return inicio.dado;
    }
}
