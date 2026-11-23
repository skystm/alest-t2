# Roteiro de apresentação - Trabalho 2

## Informações gerais

- **Tema:** Simulação de uma central de suporte de Help Desk
- **Integrantes:** Rafael Radomsky e Daniel Garcia
- **Disciplina:** Algoritmos e Estruturas de Dados I
- **Tempo-alvo:** 8 a 9 minutos
- **Máximo permitido:** 10 minutos
- **Divisão:** Rafael = introdução, implementação e execução; Daniel = parametrização, métricas e análise

## Preparação antes da gravação

1. Abra o projeto no VS Code, deixando a árvore de arquivos visível.
2. Tenha aberto o [relatório](/C:/Users/Badan/alest-t2/relatorio.md).
3. Deixe uma apresentação ou tela aberta com a sequência:
   - slide com o fluxo Entrada -> Classificação -> Processamento;
   - slide com as três prioridades;
   - arquivos Java relevantes;
   - gráfico 1;
   - gráfico 5;
   - gráfico 2 ou 4;
   - terminal com uma execução curta.
4. Antes da gravação, execute no terminal:

```bash
javac src/*.java
java -cp src SimuladorHelpDesk seed=42 rodadas=100 atendentes=3 classificadores=2 capacidade=20 csv=dados/apresentacao.csv
```

Para deixar a demonstração curta no vídeo, grave apenas uma parte inicial do terminal ou execute com menos rodadas:

```bash
java -cp src SimuladorHelpDesk rodadas=20 seed=42 csv=dados/apresentacao.csv
```

Para gerar os gráficos no final, use:

```bash
python gerar_graficos.py
```

---

## 0:00 - 0:35 | Abertura

### Tela

- Apresentar uma tela inicial com o título: **Simulação de um Sistema com Filas e Prioridades**
- Mostrar: nomes dos integrantes, Help Desk, disciplina e turma.

### Rafael

> "Olá. Nós somos Rafael Radomsky e Daniel Garcia, e este é o trabalho de simulação de um sistema com filas e prioridades da disciplina de Algoritmos e Estruturas de Dados I. Nosso tema é uma central de suporte de TI. O sistema recebe chamados, classifica-os por prioridade e encaminha-os a atendentes com capacidade limitada."

---

## 0:35 - 1:15 | Tema e justificativa

### Tela

- Manter o título na tela.
- Trocar para um diagrama simples:
  - Chamados chegam;
  - Fila de entrada;
  - 3 classificadores ou classificadores configuráveis;
  - filas Crítico, Alto e Normal;
  - atendentes.

### Daniel

> "Escolhemos um Help Desk porque o problema combina perfeitamente as três etapas exigidas pelo trabalho. Os chamados chegam em momentos diferentes e podem esperar em uma fila de entrada com capacidade limitada."

> "Na classificação, são atribuídas três prioridades: Crítico, para sistema fora do ar; Alto, para degradação parcial do serviço; e Normal, para dúvidas e solicitações rotineiras."

> "No processamento, um atendimento iniciado não é interrompido. Quando há mais de um chamado esperando, o atendente retira primeiro o chamado da fila não vazia de maior prioridade."

---

## 1:15 - 2:15 | Tempo simulado e ordem de atualização

### Tela

- Mostrar uma imagem ou diagrama numerado da rodada.
- Destacar esta ordem:
  1. Avançar classificadores e atendentes;
  2. Mover classificações concluídas para as filas de prioridade;
  3. Registrar atendimentos concluídos;
  4. Gerar novas chegadas;
  5. Preencher classificadores livres;
  6. Preencher atendentes livres;
  7. Registrar métricas.

### Rafael

> "Cada rodada representa uma unidade de tempo simulado, sem espera em tempo real. No início de cada rodada, o estado dos classificadores e atendentes é avançado. Depois, concluímos classificação e atendimento, geramos novas chegadas e iniciamos novas operações."

> "Essa ordem é importante porque uma operação não pode começar e terminar na mesma rodada. Por exemplo, uma operação com duração de duas rodadas iniciada em determinada rodada permanece ocupada na próxima antes de ser concluída."

### Tela adicional

- Exibir rapidamente o código correspondente no método `main` da classe [SimuladorHelpDesk.java](/C:/Users/Badan/alest-t2/src/SimuladorHelpDesk.java), nas linhas em que está o loop `for (int rodada = 1; ...)`.

### Daniel

> "O ponto de atualização está no primeiro loop de cada rodada. Os passos seguintes correspondem exatamente às classificações concluídas, às chegadas, aos despachos para classificação e aos despachos para atendimento."

---

## 2:15 - 3:15 | Estrutura de dados

### Tela

- Mostrar lado a lado:
  - [Fila.java](/C:/Users/Badan/alest-t2/src/Fila.java);
  - [No.java](/C:/Users/Badan/alest-t2/src/No.java);
  - diagrama `inicio -> No -> No -> No <- fim`.

### Rafael

> "As filas foram implementadas manualmente, usando uma lista encadeada de nós. A classe Fila contém referências para o início e para o fim. A operação enqueue insere no fim e a operação dequeue remove do início, mantendo o comportamento FIFO."

> "Ambas as operações principais têm custo constante, porque o tamanho da fila é atualizado e a estrutura guarda diretamente o primeiro e o último nó."

> "Adaptamos a estrutura que poderia armazenar Integer para armazenar chamados completos. Essa adaptação é feita pela classe Chamado, que guarda o identificador, a prioridade e os instantes em que o chamado passou pelas etapas."

### Tela adicional

- Mostrar [Chamado.java](/C:/Users/Badan/alest-t2/src/Chamado.java).

### Daniel

> "A estrutura Chamado permite calcular tempos de espera e duração do atendimento. Além disso, os classificadores e atendentes guardam o chamado em execução e o número de rodadas restantes."

---

## 3:15 - 4:10 | Recursos e prioridades

### Tela

- Mostrar [Classificador.java](/C:/Users/Badan/alest-t2/src/Classificador.java) e [Atendente.java](/C:/Users/Badan/alest-t2/src/Atendente.java).
- Mostrar as quatro filas representadas no diagrama.

### Rafael

> "Os classificadores são recursos limitados, configurados pelo número de classificadores e pelo tempo de classificação. Quando um classificador termina, atribui aleatoriamente a prioridade ao chamado e o coloca na fila correspondente."

> "Os atendentes têm comportamento parecido. Eles pegam da fila crítico, depois da fila alto e, por último, da fila normal. Se não houver chamado crítico, verificam a próxima prioridade disponível."

### Daniel

> "O atendimento não é interrompido. Mesmo que um chamado crítico chegue enquanto um chamado normal estiver sendo atendido, o chamado normal continua até terminar. Somente após a conclusão o atendente poderá escolher o chamado crítico."

---

## 4:10 - 5:05 | Parametrização

### Tela

- Mostrar o bloco de constantes no início de `SimuladorHelpDesk.java`.
- Opcionalmente usar uma tabela com os principais parâmetros.

| Parâmetro | Cenário padrão |
|---|---:|
| Rodadas | 100 |
| Chamadas máximas por rodada | 3 |
| Capacidade da entrada | 20 |
| Classificadores | 2 |
| Tempo de classificação | 2 rodadas |
| Atendentes | 3 |
| Semente | 42 |

### Rafael

> "Os parâmetros estão no início de SimuladorHelpDesk.java e também podem ser alterados pela linha de comando. Isso permite fazer experimentos sem modificar a lógica da simulação."

> "Um exemplo é mudar o número de atendentes com `atendentes=5`, o número de classificadores com `classificadores=4`, ou a capacidade da entrada com `capacidade=5`. Também configuramos uma semente fixa para tornar os cenários reprodutíveis."

### Daniel

> "Os parâmetros são coerentes com o tema. A semente 42 permite comparar cenários com a mesma sequência aleatória. A capacidade da fila representa um limite real da central. Já o máximo de chegadas controla o volume que pode ser produzido em cada rodada."

---

## 5:05 - 5:55 | Execução breve

### Tela

- Mostrar o terminal.
- Executar com 20 rodadas ou a execução padrão.

### Rafael

> "Agora vamos mostrar uma execução curta. Neste caso, deixamos 100 rodadas configuradas na base do programa e podemos alterar a quantidade pela linha de comando."

Executar:

```bash
java -cp src SimuladorHelpDesk rodadas=20 seed=42 csv=dados/apresentacao.csv
```

### Daniel

> "O programa imprime a situação de cada rodada: novos chamados, tamanho da fila de entrada, filas por prioridade, atendimentos concluídos e número de atendentes ocupados. Ao final, mostra também a demanda que permaneceu no sistema."

---

## 5:55 - 6:55 | Métricas

### Tela

- Mostrar `Metricas.java`.
- Mostrar uma tabela curta com as métricas principais.

### Daniel

> "As métricas foram implementadas na classe Metricas e permitem avaliar o sistema além da simples impressão das rodadas."

> "Coletamos a taxa de recusa, a espera média na fila de entrada, a espera após classificação, o tempo médio por prioridade, a ocupação dos atendentes e o throughput acumulado."

> "A taxa de recusa é o número de chamados recusados dividido pelos chamados que tentaram entrar. A ocupação representa quantos atendentes estiveram ocupados durante o conjunto de rodadas."

### Rafael

> "Os dados por rodada são exportados para CSV. Depois, o script gerar_graficos.py usa esses arquivos para produzir os gráficos usados no relatório."

---

## 6:55 - 8:05 | Cenários e gráficos

### Tela

- Mostrar inicialmente o [gráfico 1](/C:/Users/Badan/alest-t2/graficos/g1_fila_entrada.png).
- Depois mostrar o [gráfico 5](/C:/Users/Badan/alest-t2/graficos/g5_recusa_ocupacao.png).
- Manter o gráfico 5 por alguns segundos.

### Daniel

> "Executamos cinco cenários alterando um parâmetro por vez e mantendo a semente 42. O cenário de referência usa 3 atendentes, 2 classificadores, capacidade 20 e até 3 chegadas por rodada."

> "No primeiro gráfico, a fila de entrada do cenário de referência cresce até ficar próxima de 20 chamados. Isso indica que o acúmulo está relacionado principalmente à capacidade de classificação."

> "No segundo gráfico, comparam-se a taxa de recusa e a ocupação dos atendentes. Com mais classificadores, no cenário 3, a recusa cai de 29,8 por cento para zero. Porém, a ocupação dos atendentes sobe para 97 por cento."

### Rafael

> "Já aumentar de 3 para 5 atendentes, no cenário 2, reduz a ocupação para 52,4 por cento, mas não muda a recusa. Isso mostra que adicionar atendentes não resolve o gargalo quando os chamados continuam esperando na etapa de classificação."

### Tela adicional

- Mostrar uma tabela ou o [gráfico 4](/C:/Users/Badan/alest-t2/graficos/g4_concluidos_c1_vs_c3.png).

### Daniel

> "Diminuir a fila para capacidade 5 eleva a recusa para 38,7 por cento. Na alta demanda, com até 5 chegadas por rodada, a recusa chega a 48,5 por cento."

---

## 8:05 - 8:45 | Conclusão

### Tela

- Mostrar um resumo final ou a primeira página do relatório.
- Opcionalmente mostrar os três principais resultados:
  - mais classificadores -> menor recusa;
  - mais atendentes -> menor ocupação;
  - alta demanda -> maior saturação.

### Rafael

> "Para o cenário de referência, o principal gargalo está na etapa de classificação. A entrada acumula chamados enquanto os classificadores não conseguem processá-los a tempo."

> "Ao aumentar a capacidade de classificação, eliminamos a recusa, mas criamos pressão sobre os atendentes. Portanto, os recursos precisam ser dimensionados em conjunto."

### Daniel

> "A análise mostra que não existe uma única alteração automaticamente correta. Se o objetivo é aceitar o maior número possível de chamados, são necessários mais classificadores. Se o objetivo é evitar atendimento sobrecarregado, os atendentes também precisam acompanhar esse aumento."

---

## 8:45 - 9:15 | Encerramento

### Tela

- Voltar ao título ou exibir uma tela final com **Obrigado**.

### Rafael

> "Apresentamos o tema, as três etapas, a estrutura encadeada adaptada para chamados, os parâmetros configuráveis, as métricas e os resultados dos cinco cenários."

### Daniel

> "Entregamos o código-fonte, os dados em CSV, os gráficos e o relatório com a análise completa. Obrigado!"

---

## Checklist final antes de gravar

- [ ] A fala cabe em aproximadamente 9 minutos.
- [ ] Cada integrante fala em todos os momentos importantes.
- [ ] Nenhum trecho explica linha por linha.
- [ ] Os trechos de código correspondem ao que está sendo mostrado.
- [ ] O terminal está visível e legível.
- [ ] Pelo menos três gráficos são mostrados.
- [ ] A execução não é muito longa.
- [ ] A análise explica os resultados, não apenas repete números.
- [ ] O vídeo termina em menos de 10 minutos.
