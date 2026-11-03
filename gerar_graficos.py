"""
Gera os graficos do Trabalho 2 - ALEST I
Uso: python gerar_graficos.py
Os graficos sao salvos em graficos/
"""

import os
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

os.makedirs("graficos", exist_ok=True)

# ── carrega os 5 cenarios ──────────────────────────────────────────────────
cenarios = {
    "C1 Referencia\n(3 atend, 2 class, cap=20)":     "dados/cenario1_referencia.csv",
    "C2 +Atendentes\n(5 atend, 2 class, cap=20)":    "dados/cenario2_mais_atendentes.csv",
    "C3 +Classificadores\n(3 atend, 4 class, cap=20)":"dados/cenario3_mais_classificadores.csv",
    "C4 Fila pequena\n(3 atend, 2 class, cap=5)":    "dados/cenario4_fila_pequena.csv",
    "C5 Alta demanda\n(3 atend, 2 class, maxch=5)":  "dados/cenario5_alta_demanda.csv",
}

dfs = {nome: pd.read_csv(arq) for nome, arq in cenarios.items()}

CORES = ["#2196F3", "#4CAF50", "#FF5722", "#9C27B0", "#FF9800"]

# ── GRAFICO 1: Tamanho da fila de entrada ao longo das rodadas ─────────────
fig, ax = plt.subplots(figsize=(10, 5))
for (nome, df), cor in zip(dfs.items(), CORES):
    ax.plot(df["rodada"], df["filaEntrada"], label=nome, color=cor, linewidth=1.5)
ax.set_title("Grafico 1 - Tamanho da Fila de Entrada por Rodada")
ax.set_xlabel("Rodada")
ax.set_ylabel("Chamados na fila de entrada")
ax.legend(fontsize=7, loc="upper left")
ax.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("graficos/g1_fila_entrada.png", dpi=150)
plt.close()
print("Grafico 1 salvo.")

# ── GRAFICO 2: Throughput acumulado (soma de concluidos) ───────────────────
fig, ax = plt.subplots(figsize=(10, 5))
for (nome, df), cor in zip(dfs.items(), CORES):
    ax.plot(df["rodada"], df["concluidosNaRodada"].cumsum(),
            label=nome, color=cor, linewidth=1.5)
ax.set_title("Grafico 2 - Throughput Acumulado (Chamados Concluidos)")
ax.set_xlabel("Rodada")
ax.set_ylabel("Total de chamados concluidos")
ax.legend(fontsize=7)
ax.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("graficos/g2_throughput_acumulado.png", dpi=150)
plt.close()
print("Grafico 2 salvo.")

# ── GRAFICO 3: Composicao das filas de prioridade (apenas C1, media movel) ─
fig, ax = plt.subplots(figsize=(10, 5))
df1 = dfs["C1 Referencia\n(3 atend, 2 class, cap=20)"]
w = 5
ax.plot(df1["rodada"], df1["filaCritico"].rolling(w).mean(),
        label="Critico", color="#F44336", linewidth=1.8)
ax.plot(df1["rodada"], df1["filaAlto"].rolling(w).mean(),
        label="Alto",    color="#FF9800", linewidth=1.8)
ax.plot(df1["rodada"], df1["filaNormal"].rolling(w).mean(),
        label="Normal",  color="#4CAF50", linewidth=1.8)
ax.set_title("Grafico 3 - Tamanho das Filas de Prioridade (Cenario 1, media movel 5 rodadas)")
ax.set_xlabel("Rodada")
ax.set_ylabel("Chamados na fila")
ax.legend()
ax.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("graficos/g3_filas_prioridade_c1.png", dpi=150)
plt.close()
print("Grafico 3 salvo.")

# ── GRAFICO 4: Chamados concluidos por rodada - comparativo C1 vs C3 ──────
fig, ax = plt.subplots(figsize=(10, 5))
df1 = dfs["C1 Referencia\n(3 atend, 2 class, cap=20)"]
df3 = dfs["C3 +Classificadores\n(3 atend, 4 class, cap=20)"]
ax.bar(df1["rodada"] - 0.2, df1["concluidosNaRodada"], width=0.4,
       label="C1 - Referencia (2 class)", color="#2196F3", alpha=0.7)
ax.bar(df3["rodada"] + 0.2, df3["concluidosNaRodada"], width=0.4,
       label="C3 - 4 Classificadores", color="#FF5722", alpha=0.7)
ax.set_title("Grafico 4 - Concluidos por Rodada: C1 vs C3 (+Classificadores)")
ax.set_xlabel("Rodada")
ax.set_ylabel("Chamados concluidos")
ax.legend()
ax.grid(True, alpha=0.3, axis="y")
ax.set_xlim(0, 40)
plt.tight_layout()
plt.savefig("graficos/g4_concluidos_c1_vs_c3.png", dpi=150)
plt.close()
print("Grafico 4 salvo.")

# ── GRAFICO 5: Taxa de recusa e ocupacao - comparativo entre cenarios ──────
resumos = {
    "C1\nReferencia":        (29.8, 87.3),
    "C2\n+Atendentes":       (29.8, 52.4),
    "C3\n+Classificadores":  (0.0,  97.0),
    "C4\nFila pequena":      (38.7, 87.3),
    "C5\nAlta demanda":      (48.5, 84.7),
}
nomes  = list(resumos.keys())
recusa = [v[0] for v in resumos.values()]
ocup   = [v[1] for v in resumos.values()]
x = range(len(nomes))

fig, ax1 = plt.subplots(figsize=(10, 5))
ax2 = ax1.twinx()
bars1 = ax1.bar([i - 0.2 for i in x], recusa, width=0.4,
                label="Taxa de recusa (%)", color="#F44336", alpha=0.8)
bars2 = ax2.bar([i + 0.2 for i in x], ocup, width=0.4,
                label="Ocupacao atendentes (%)", color="#2196F3", alpha=0.8)
ax1.set_xticks(list(x))
ax1.set_xticklabels(nomes, fontsize=9)
ax1.set_ylabel("Taxa de recusa (%)", color="#F44336")
ax2.set_ylabel("Ocupacao dos atendentes (%)", color="#2196F3")
ax1.set_ylim(0, 70)
ax2.set_ylim(0, 120)
ax1.set_title("Grafico 5 - Taxa de Recusa e Ocupacao dos Atendentes por Cenario")
lines1, labels1 = ax1.get_legend_handles_labels()
lines2, labels2 = ax2.get_legend_handles_labels()
ax1.legend(lines1 + lines2, labels1 + labels2, loc="upper left")
ax1.grid(True, alpha=0.3, axis="y")
plt.tight_layout()
plt.savefig("graficos/g5_recusa_ocupacao.png", dpi=150)
plt.close()
print("Grafico 5 salvo.")

print("\nTodos os graficos gerados em graficos/")
