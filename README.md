# DAA Assignment 4 - Dynamic Programming and String Algorithms (City Search & Spell-Check)

Student: **MAKHAN SABINA, SE-2509**

## What is inside

| Part | Files |
|---|---|
| Edit Distance, three versions (one interface `EditDistance`) | `EditDistanceRecursive`, `EditDistanceTable`, `EditDistanceTwoRows` |
| String search (one interface `StringSearch`) | `NaiveSearch`, `KmpSearch` (public `int[] KmpSearch.lps(String)`) |
| Seeded input generator | `Generator` (fixed seed `20261102`) |
| Experiment runner (writes the CSV) | `Experiment` |
| JUnit 5 tests | `EditDistanceTest`, `LpsTest`, `SearchTest` |
| Plots | `scripts/plot.py` -> `results/plots/*.png` |
| Results | `results/results.csv` (final run), `results/first_run_results.csv` (first run, see REPORT.md section 4) |
| Report | `REPORT.md` |

All sources are in `src/main/java/daa`, all tests in `src/test/java/daa`.

## Requirements

JDK 17 or newer, Maven 3.8+, and for the plots Python 3 with `matplotlib` (`pip install matplotlib`).

## Build and test (one command)

```
mvn test
```

## Run the experiment (writes `results/results.csv`, takes about 1.5 minutes)

```
mvn compile exec:java
```

The generator has a fixed seed, so the counters (`ops`, `mem`, `result`) are identical on every run and every
machine. Only `time_ms` changes with the computer.

## Draw the plots (writes `results/plots/*.png`)

```
python3 scripts/plot.py
```

## `results.csv` columns

`algorithm, input, n, m, time_ms, ops, mem, result, runs`

* `n` - length of the first string (Edit Distance) or of the text (search); `m` - length of the second string / pattern
* `time_ms` - median of `runs` runs (5; 3 for the recursive version; 1 for the recursive runs with n >= 13)
* `ops` - counter filled inside the algorithm: recursive calls / table cells computed / character comparisons
  (KMP: search + LPS build)
* `mem` - int cells allocated for DP storage or for the LPS array (0 for Recursive and Naive)
* `result` - edit distance, or the number of matches (to check that the runs are correct)

