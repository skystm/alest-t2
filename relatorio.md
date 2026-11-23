# Relatório — Trabalho 2: Simulação de um Sistema com Filas e Prioridades

**Disciplina:** Algoritmos e Estruturas de Dados I  
**Professor:** Gabriel Fonseca Silva  
**Integrantes:** Rafael Radomsky e Daniel Garcia  
**Semestre:** 2026-2

---

## 1. Tema: Central de Suporte de TI (Help Desk)

O sistema simula o fluxo de chamados técnicos em uma central de suporte de TI. Chamados chegam continuamente, são classificados por prioridade e processados por atendentes com capacidade limitada.

A escolha justifica o uso de filas e prioridades porque, em ambientes reais de TI, chamados críticos (sistema fora do ar) devem ser atendidos antes de solicitações rotineiras, e o volume de chamados pode superar a capacidade de atendimento simultâneo, exigindo filas de espera.

---

## 2. Etapas da Simulação

### 2.1 Entrada

Cada chamado recebe um identificador único no formato `CHAMADO-001`. Na chegada, o chamado entra na **fila de entrada** (FIFO, capacidade configurável). Se a fila estiver cheia, o chamado é **recusado** e contabilizado. A quantidade de chamados por rodada é sorteada uniformemente entre 0 e `MAX_CHEGADA_POR_RODADA`.

### 2.2 Classificação

Classificadores (recursos limitados) retiram chamados da fila de entrada na **ordem de chegada** (FIFO) e determinam a prioridade com base em probabilidades configuráveis. O processo dura `TEMPO_CLASSIFICACAO` rodadas por chamado. Ao terminar, o chamado é inserido na fila correspondente à prioridade definida.

**Três níveis de prioridade:**

| Nível | Significado | Probabilidade |
|-------|-------------|---------------|
| 1 – CRÍTICO | Sistema fora do ar, impacto total na produção | 20% |
| 2 – ALTO | Degradação de serviço, impacto parcial | 35% |
| 3 – NORMAL | Dúvidas, solicitações rotineiras | 45% |

### 2.3 Processamento

Atendentes (recursos limitados) retiram o chamado da fila **não vazia de maior prioridade**. O processamento não é interrompido por chamados mais urgentes que chegarem depois. A duração varia por prioridade: CRÍTICO = 4, ALTO = 3, NORMAL = 2 rodadas.

---

## 3. Passagem do Tempo

Cada rodada corresponde a uma unidade de tempo simulado. A ordem de atualização por rodada é:

1. **Tick** — avança o estado de todos os classificadores e atendentes em execução
2. **Conclusão da classificação** — chamados que terminaram recebem prioridade e entram na fila correspondente
3. **Conclusão do atendimento** — chamados que terminaram têm métricas registradas
4. **Entrada** — novos chamados são gerados e tentam entrar na fila de entrada
5. **Despacho para classificação** — classificadores livres pegam da fila de entrada
6. **Despacho para atendimento** — atendentes livres pegam da fila de maior prioridade disponível
7. **Log e métricas** — estado da rodada é registrado

> **Regra de duração mínima:** uma operação iniciada na rodada N só pode terminar na rodada N + duração (nunca na mesma rodada). Isso é garantido porque o tick acontece *antes* do despacho.

---

## 4. Estruturas de Dados

### 4.1 Fila encadeada (`Fila.java`, `No.java`)

Implementação manual com nós encadeados, sem uso de `java.util`. Suporta capacidade máxima configurável (para a fila de entrada) ou ilimitada (filas de prioridade).

```
No: | dado (Chamado) | proximo (No) |
Fila: inicio → [No] → [No] → [No] ← fim
```

Operações: `enqueue` insere no fim; `dequeue` remove do início — ambas O(1).

A estrutura originalmente projetada para `Integer` foi adaptada para `Chamado`, mantendo a mesma lógica de nós encadeados.

### 4.2 Classe auxiliar `Chamado`

Armazena identificador, prioridade e os timestamps de cada etapa (rodada de chegada, início/fim de classificação, início/fim de atendimento). Esses campos são usados no cálculo das métricas de tempo de espera.

### 4.3 Recursos com estado (`Classificador`, `Atendente`)

Cada recurso mantém o chamado atual e o contador de rodadas restantes. O método `tick()` decrementa o contador e retorna o chamado quando termina, permitindo o controle preciso da duração das operações.

---

## 5. Parâmetros Configuráveis

Todos os parâmetros estão no topo de `SimuladorHelpDesk.java` e podem ser sobrescritos via linha de comando (`chave=valor`).

| Parâmetro | Significado | Valor padrão | Intervalo |
|-----------|-------------|--------------|-----------|
| `SEED` | Semente do gerador aleatório (-1 = aleatório) | 42 | qualquer long |
| `TOTAL_RODADAS` | Duração da simulação | 100 | ≥ 1 |
| `CAPACIDADE_FILA_ENTRADA` | Capacidade máxima da fila de entrada | 20 | ≥ 1 |
| `MAX_CHEGADA_POR_RODADA` | Máximo de chamados por rodada | 3 | ≥ 0 |
| `NUM_CLASSIFICADORES` | Classificadores simultâneos | 2 | ≥ 1 |
| `TEMPO_CLASSIFICACAO` | Rodadas por classificação | 2 | ≥ 1 |
| `NUM_ATENDENTES` | Atendentes simultâneos | 3 | ≥ 1 |
| `TEMPO_ATENDIMENTO[1..3]` | Rodadas por atendimento (por prioridade) | {4, 3, 2} | ≥ 1 |
| `PROB_CRITICO` | Probabilidade de prioridade CRÍTICO | 0.20 | [0.0, 1.0] |
| `PROB_ALTO` | Probabilidade de prioridade ALTO | 0.35 | [0.0, 1.0] |

---

## 6. Métricas Coletadas

| Métrica | O que representa | Como é calculada |
|---------|-----------------|------------------|
| Taxa de recusa | % de chamados rejeitados por fila cheia | recusados / chegaram × 100 |
| Espera média na fila de entrada | Rodadas entre chegada e início da classificação | média de (rodadaInicioClass − rodadaChegada) |
| Espera média pós-classificação | Rodadas entre fim da classificação e início do atendimento | média de (rodadaInicioAtend − rodadaFimClass) |
| Tempo médio de atendimento por prioridade | Duração real do atendimento | média de (rodadaFimAtend − rodadaInicioAtend) por prioridade |
| Ocupação dos atendentes | % do tempo em que os atendentes estão ocupados | soma(atend. ocupados por rodada) / (numAtend × totalRodadas) × 100 |
| Throughput acumulado | Total de chamados concluídos ao longo do tempo | soma acumulada de concluídos por rodada |

---

## 7. Experimentação com Diferentes Configurações

Todos os cenários usam a mesma semente (42) e 100 rodadas. O cenário 1 é a referência; nos demais, apenas um parâmetro foi alterado.

| Cenário | Configuração alterada | Chamados recusados | Concluídos | Espera fila entrada | Ocupação atendentes |
|---------|----------------------|--------------------|------------|---------------------|---------------------|
| C1 – Referência | — | 50 (29,8%) | 95 | 13,83 rod. | 87,3% |
| C2 – +Atendentes | atendentes: 3 → 5 | 50 (29,8%) | 95 | 13,83 rod. | 52,4% |
| C3 – +Classificadores | classificadores: 2 → 4 | 0 (0,0%) | 86 | 0,23 rod. | 97,0% |
| C4 – Fila pequena | capacidade: 20 → 5 | 65 (38,7%) | 95 | 3,31 rod. | 87,3% |
| C5 – Alta demanda | maxChegada: 3 → 5 | 113 (48,5%) | 96 | 15,26 rod. | 84,7% |

---

## 8. Análise dos Resultados

### Gráfico 1 — Tamanho da Fila de Entrada por Rodada

O cenário de referência (C1) mostra a fila de entrada saturando rapidamente e permanecendo próxima do limite de 20 chamados a partir da rodada 25. Isso indica que o gargalo do sistema está na etapa de **classificação**, não no atendimento.

O cenário C3 (4 classificadores) elimina completamente esse acúmulo: a fila de entrada nunca ultrapassa 2 chamados. Porém, isso transfere a pressão para as filas de prioridade, como mostra o gráfico 3.

O cenário C4 (capacidade 5) satura ainda mais rápido, com taxa de recusa de 38,7%, confirmando que a capacidade da fila de entrada funciona como válvula de controle do fluxo.

### Gráfico 2 — Throughput Acumulado

O throughput acumulado é praticamente idêntico entre C1, C2, C4 e C5 (~95 chamados em 100 rodadas). Isso mostra que o número de atendentes e a capacidade da fila de entrada **não são os gargalos** quando há 2 classificadores: os chamados chegam nas filas de prioridade em ritmo que os 3 atendentes já conseguem processar.

O cenário C3 apresenta throughput ligeiramente menor (86 concluídos), aparentemente paradoxal. A explicação está no gráfico 4.

### Gráfico 3 — Filas de Prioridade (Cenário 1)

No cenário de referência, as filas de prioridade ALTO e NORMAL raramente acumulam mais de 1 chamado simultaneamente. A fila CRÍTICO fica vazia na maior parte do tempo. Isso confirma que os atendentes dão conta da saída da classificação e que o gargalo está realmente na entrada → classificação.

### Gráfico 4 — Concluídos por Rodada: C1 vs C3

Com 4 classificadores (C3), mais chamados chegam às filas de prioridade por rodada, aumentando a ocupação dos atendentes para 97%. Os atendentes ficam constantemente sobrecarregados, especialmente com chamados CRÍTICO (4 rodadas) e ALTO (3 rodadas). Paradoxalmente, o total concluído em 100 rodadas é menor porque a maior proporção de chamados críticos e altos (que demoram mais) consome mais tempo dos atendentes.

### Gráfico 5 — Taxa de Recusa e Ocupação por Cenário

O gráfico evidencia o **trade-off central do sistema**: aumentar classificadores (C3) reduz a recusa a 0%, mas satura os atendentes (97%). Aumentar atendentes (C2) reduz a ocupação (52%), mas não afeta a recusa, pois o gargalo continua sendo a classificação. A alta demanda (C5) eleva tanto a recusa (48,5%) quanto mantém alta ocupação (84,7%), mostrando que o sistema opera acima da capacidade neste cenário.

**Conclusão:** Para o cenário de referência, o principal gargalo é o número de classificadores. Adicionar classificadores resolve o acúmulo na entrada, mas exige também mais atendentes para evitar saturação no processamento. A configuração ideal depende do objetivo: minimizar recusa (mais classificadores) ou minimizar custo operacional (balancear atendentes × classificadores).

---

## 9. Instruções de Compilação e Execução

```bash
# Compilar
javac src/*.java

# Executar com parâmetros padrão
java -cp src SimuladorHelpDesk

# Executar com parâmetros customizados
java -cp src SimuladorHelpDesk atendentes=5 classificadores=4 rodadas=200 seed=99 csv=saida.csv

# Parâmetros disponíveis:
#   seed=N          semente do gerador (default: 42)
#   rodadas=N       total de rodadas (default: 100)
#   capacidade=N    capacidade da fila de entrada (default: 20)
#   maxchegada=N    máximo de chegadas por rodada (default: 3)
#   classificadores=N  número de classificadores (default: 2)
#   tempoclassif=N  rodadas por classificação (default: 2)
#   atendentes=N    número de atendentes (default: 3)
#   csv=arquivo     nome do arquivo CSV de saída (default: metricas.csv)

# Gerar gráficos (requer Python 3 + matplotlib + pandas)
python gerar_graficos.py
```

Os gráficos são salvos em `graficos/` e os dados de cada cenário em `dados/`.

### Execução web para demonstração

Também foi disponibilizada uma interface web para executar a simulação durante a apresentação:

```bash
javac src/*.java
java -cp src HelpDeskServer
```

Abra `http://localhost:8080`. A interface permite escolher a semente, o número de rodadas, atendentes, classificadores, capacidade da fila e chegadas máximas. A aplicação pode ser publicada por Docker:

```bash
docker build -t alest-helpdesk .
docker run --rm -p 8080:8080 -e PORT=8080 alest-helpdesk
```
