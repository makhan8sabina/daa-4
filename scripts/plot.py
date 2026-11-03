#!/usr/bin/env python3
import csv
import os
import textwrap
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_PATH = os.path.join(ROOT, "results", "results.csv")
OUT_DIR = os.path.join(ROOT, "results", "plots")
os.makedirs(OUT_DIR, exist_ok=True)

rows = []
with open(CSV_PATH) as f:
    for r in csv.DictReader(f):
        rows.append({
            "alg": r["algorithm"], "input": r["input"], "n": int(r["n"]), "m": int(r["m"]),
            "t": float(r["time_ms"]), "ops": int(r["ops"]), "mem": int(r["mem"]),
            "result": int(r["result"]), "runs": int(r["runs"]),
        })


def series(alg, inp):
    pts = sorted((r for r in rows if r["alg"] == alg and r["input"] == inp), key=lambda r: r["n"])
    return pts


SEARCH_TICKS = [50000, 100000, 200000, 400000, 800000]
INPUT_COLOR = {"random": "tab:blue", "identical": "tab:green", "disjoint": "tab:red",
               "log": "tab:orange", "worst": "tab:red", "all_match": "tab:purple"}


def finish(fig, ax, name, title, xlabel, ylabel, takeaway):
    ax.set_title(title, fontsize=12, fontweight="bold")
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.grid(True, which="both", alpha=0.3)
    fig.subplots_adjust(bottom=0.24)
    fig.text(0.5, 0.02, "\n".join(textwrap.wrap("What to notice: " + takeaway, 95)),
             ha="center", va="bottom", fontsize=9, style="italic")
    fig.savefig(os.path.join(OUT_DIR, name), dpi=150)
    plt.close(fig)
    print("saved", name)


fig, ax = plt.subplots(figsize=(7.5, 5.6))
for inp in ("disjoint", "random", "identical"):
    pts = series("edit_recursive", inp)
    ax.plot([p["n"] for p in pts], [p["ops"] for p in pts], "o-", color=INPUT_COLOR[inp], label=f"input: {inp}")
dis = series("edit_recursive", "disjoint")
n0, o0 = dis[-1]["n"], dis[-1]["ops"]
ns = [p["n"] for p in dis]
ax.plot(ns, [o0 * 5.83 ** (n - n0) for n in ns], "k--", lw=1, label="reference: growth 5.83 per +1 in n\n(anchored at n = 14)")
ax.set_yscale("log")
ax.set_xticks(ns)
ax.legend(fontsize=8)
finish(fig, ax, "01_recursive_calls.png", "Recursive Edit Distance: number of calls vs n",
       "string length n (|a| = |b|, characters)", "recursive calls (log scale)",
       "On strings with no common letters the calls grow about 5.6x per extra character (1.2e10 calls at n = 14), "
       "random strings are far cheaper, identical strings need only n+1 calls.")

fig, ax = plt.subplots(figsize=(7.5, 5.6))
for alg, ls, lab in (("edit_table", "-", "Table"), ("edit_two_rows", "--", "Two rows")):
    for inp in ("random", "identical", "disjoint"):
        pts = series(alg, inp)
        ax.plot([p["n"] for p in pts], [p["t"] for p in pts], ls, marker="o", ms=4,
                color=INPUT_COLOR[inp], label=f"{lab}, {inp}")
ref = series("edit_two_rows", "random")
ax.plot([p["n"] for p in ref], [ref[0]["t"] * (p["n"] / ref[0]["n"]) ** 2 for p in ref], "k:", lw=1.2,
        label="reference: time ~ n^2 (anchored at n = 250)")
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xticks([250, 500, 1000, 2000, 4000])
ax.set_xticklabels(["250", "500", "1000", "2000", "4000"])
ax.legend(fontsize=7)
finish(fig, ax, "02_dp_time.png", "Table vs Two rows: time vs n (log-log)",
       "string length n (|a| = |b|, characters)", "median time of 5 runs (ms, log scale)",
       "Both versions follow the n^2 line to within a factor of a few (Table bends upward at large n), "
       "Two rows is about 2-2.5x faster than Table at n = 4000, and at n = 4000 the input type changes the time by less than 1.4x.")

fig, ax = plt.subplots(figsize=(7.5, 5.6))
for alg, lab, mk in (("edit_table", "Table: (n+1)(m+1) ints", "o-"), ("edit_two_rows", "Two rows: 2(m+1) ints", "s--")):
    pts = series(alg, "random")
    ax.plot([p["n"] for p in pts], [p["mem"] for p in pts], mk, label=lab)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xticks([250, 500, 1000, 2000, 4000])
ax.set_xticklabels(["250", "500", "1000", "2000", "4000"])
ax.legend(fontsize=9)
finish(fig, ax, "03_dp_memory.png", "Table vs Two rows: DP memory vs n (log-log)",
       "string length n (|a| = |b|, characters)", "int cells allocated for DP storage (log scale)",
       "At n = 4000 the table allocates 16.0 million ints (about 64 MB) and Two rows 8 002 (about 32 KB): "
       "2000x less, and the gap keeps growing like n/2.")

fig, ax = plt.subplots(figsize=(7.5, 5.6))
for alg, ls, lab in (("search_naive", "-", "Naive"), ("search_kmp", "--", "KMP")):
    for inp in ("random", "log", "worst", "all_match"):
        pts = series(alg, inp)
        ax.plot([p["n"] for p in pts], [p["ops"] / p["n"] for p in pts], ls, marker="o", ms=4,
                color=INPUT_COLOR[inp], label=f"{lab}, {inp}" + (" (same line as all_match)" if (alg, inp) == ("search_naive", "worst") else ""))
ax.axhline(2, color="k", lw=1, ls=":")
ax.text(52000, 2.2, "KMP bound: 2 comparisons per text character", fontsize=8)
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xticks(SEARCH_TICKS)
ax.set_xticklabels(["50k", "100k", "200k", "400k", "800k"])
ax.minorticks_off()
ax.set_yticks([1, 2, 5, 10, 100, 500])
ax.set_yticklabels(["1", "2", "5", "10", "100", "500"])
ax.legend(fontsize=7, ncol=2, loc="center right")
finish(fig, ax, "04_search_comparisons_per_char.png", "String search: comparisons per text character",
       "text length n (characters, log scale)", "character comparisons / n (ratio, log scale)",
       "KMP never goes above 2 comparisons per character (about 2 on the worst input), while Naive reaches about 500 "
       "(= pattern length) on the worst and all-match inputs and only 1.0-1.3 on log-like and random text.")

fig, ax = plt.subplots(figsize=(7.5, 5.6))
for alg, ls, lab in (("search_naive", "-", "Naive"), ("search_kmp", "--", "KMP")):
    for inp in ("random", "log", "worst", "all_match"):
        pts = series(alg, inp)
        ax.plot([p["n"] for p in pts], [p["t"] for p in pts], ls, marker="o", ms=4,
                color=INPUT_COLOR[inp], label=f"{lab}, {inp}")
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xticks(SEARCH_TICKS)
ax.set_xticklabels(["50k", "100k", "200k", "400k", "800k"])
ax.minorticks_off()
ax.legend(fontsize=7, ncol=2)
finish(fig, ax, "05_search_time.png", "String search: time vs text length (log-log)",
       "text length n (characters, log scale)", "median time of 5 runs (ms, log scale)",
       "On the worst input Naive needs 1.05 s for 800 000 characters and KMP 3 ms (about 340x); "
       "on random text both take 5-7 ms and on log-like text 2-7 ms.")

fig, ax = plt.subplots(figsize=(7.5, 5.6))
style = {"edit_recursive": ("tab:red", "o"), "edit_table": ("tab:blue", "s"), "edit_two_rows": ("tab:cyan", "D"),
         "search_naive": ("tab:orange", "^"), "search_kmp": ("tab:green", "v")}
lab = {"edit_recursive": "Edit: recursive (calls)", "edit_table": "Edit: Table (cells)",
       "edit_two_rows": "Edit: Two rows (cells)", "search_naive": "Search: Naive (comparisons)",
       "search_kmp": "Search: KMP (comparisons)"}
for alg, (c, mk) in style.items():
    pts = [r for r in rows if r["alg"] == alg and r["t"] > 0 and r["ops"] > 100]
    ax.scatter([p["ops"] for p in pts], [p["t"] for p in pts], color=c, marker=mk, s=22, alpha=0.8, label=lab[alg])
xs = [1e3, 1e11]
for ns_per_op, ls in ((1, ":"), (10, "-.")):
    ax.plot(xs, [x * ns_per_op * 1e-6 for x in xs], "k", ls=ls, lw=1, label=f"reference: {ns_per_op} ns per counted unit")
ax.set_xscale("log")
ax.set_yscale("log")
ax.set_xlim(1e3, 1e11)
ax.set_ylim(1e-2, 1e5)
ax.legend(fontsize=7)
finish(fig, ax, "06_time_vs_counter.png", "Time vs counter for every run (log-log)",
       "counted units of work (calls / cells / comparisons, log scale)", "median time (ms, log scale)",
       "Time rises with the counter for every algorithm and mostly stays between 1 and 10 ns per unit "
       "(a few runs are slower), but the cost per unit differs between algorithms, so counters compare growth, not speed.")
