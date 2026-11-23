# Roteiro direto para o vídeo

**Duração sugerida:** 4 minutos

**Apresentadores:** Rafael e Daniel
**Demonstração:** abrir a aplicação web e mostrar o código apenas nos trechos indicados.

## Antes de gravar

Deixe a aplicação aberta e pronta para executar. Na interface, use inicialmente:

- Semente: `42`
- Rodadas: `100`
- Atendentes: `3`
- Classificadores: `2`
- Capacidade da fila: `20`
- Chegadas máximas por rodada: `3`

## Roteiro

### 0:00 — Apresentação

**Tela:** aplicação aberta.

**Rafael:**

> "Somos Rafael e Daniel. Nosso trabalho é uma simulação de uma central de suporte de TI. Chamados chegam, são classificados por prioridade e atendidos por recursos limitados."

### 0:20 — Como funciona

**Tela:** mostrar os parâmetros da aplicação.

**Daniel:**

> "A simulação tem três etapas: entrada, classificação e atendimento. A fila de entrada é FIFO e tem capacidade limitada. Se estiver cheia, o chamado é recusado."

> "Depois da classificação, o chamado vai para uma fila Crítica, Alta ou Normal. Os atendentes sempre escolhem a fila de maior prioridade disponível. Um atendimento já iniciado não é interrompido."

### 0:55 — Estruturas e tempo

**Tela:** no VS Code, mostrar rapidamente `Fila.java`, `No.java` e o loop de rodadas em `SimuladorHelpDesk.java`.

**Rafael:**

> "A fila foi implementada com nós encadeados. A inserção acontece no fim e a remoção no início. Cada nó guarda um chamado."

> "O tempo avança em rodadas. A cada rodada, os recursos avançam, operações podem terminar, chegam novos chamados e os recursos livres iniciam o próximo trabalho. Assim, uma operação ocupa o recurso pelo tempo configurado."

### 1:35 — Parâmetros e execução

**Tela:** voltar à aplicação; apontar os campos e clicar em **Executar cenário configurado**.

**Daniel:**

> "Os campos permitem alterar a semente, a duração da simulação, o número de atendentes e classificadores, a capacidade da fila e a quantidade máxima de chegadas por rodada."

> "A semente 42 torna esta execução reproduzível: usando os mesmos parâmetros e a mesma semente, obtemos os mesmos resultados."

**Ação:** aguardar o resumo e mostrar os valores apresentados.

### 2:15 — Comparação de cenários

**Tela:** clicar em **Comparar com 4 classificadores**. Mostrar a mudança nos resultados.

**Rafael:**

> "Agora alteramos o número de classificadores de dois para quatro. Com a mesma semente, a fila de entrada diminui e não há recusas neste cenário. Em contrapartida, a ocupação dos atendentes aumenta."

> "Isso mostra que aumentar a capacidade de classificação transfere mais chamados para o atendimento. Por isso, os dois tipos de recurso precisam ser analisados em conjunto."

### 2:55 — Métricas e encerramento

**Tela:** apontar o resumo, o gráfico da fila e, se necessário, abrir o log detalhado.

**Daniel:**

> "O resumo mostra chegadas, recusas, classificações, conclusões, espera média e ocupação. O gráfico mostra como a fila de entrada varia durante as rodadas. O log detalhado permite conferir os dados de cada rodada."

**Rafael:**

> "Com isso demonstramos o funcionamento, as estruturas de dados, os parâmetros e o efeito de alterar a configuração. Obrigado."

## Observação para a gravação

Não é necessário ler o log inteiro nem explicar cada linha do código. Mostrem apenas os trechos indicados e os resultados das duas execuções.
