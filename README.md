# SeasonalClean

SeasonalClean is the research artifact for **"Cleaning Time Series under
Seasonal and Trend Constraints."** It contains the Java implementation of the
proposed seasonal repair method (`SRD`), comparison methods, real-world
clean/dirty time-series pairs, synthetic datasets, and supplementary material.

## Method

SeasonalClean repairs errors in time series with seasonal and trend structure:

1. decompose the series into seasonal, trend, and residual components;
2. detect residuals outside the configured statistical threshold;
3. estimate repairs from observations at the same seasonal phase;
4. repeat until convergence or the maximum iteration count is reached.

The proposed method is implemented in
`code/src/main/java/Algorithm/SRD.java`. Decomposition and median utilities are
under `code/src/main/java/Algorithm/util/`.

## Repository structure

```text
SeasonalClean/
├── code/
│   ├── pom.xml
│   └── src/main/java/
│       ├── Experiment.java       # Experiment modes and parameters
│       ├── AddNoise.java         # Synthetic error injection
│       ├── LabelData.java        # Labeled-point sampling
│       ├── LoadData.java         # CSV loading and normalization
│       ├── Analysis.java         # Accuracy and runtime metrics
│       └── Algorithm/
│           ├── SRD.java          # SeasonalClean
│           ├── SCREEN.java
│           ├── IMR.java
│           ├── EWMA.java
│           ├── Holistic.java
│           ├── ASAPPy/
│           └── HolocleanPy/
├── data/
│   ├── synthetic/
│   ├── real_clean/
│   └── real_dirty/
├── results/                      # Generated at runtime; ignored by Git
└── supplementary.pdf
```

## Requirements

The core Java experiments require:

- JDK 17 or later
- Apache Maven 3.8 or later

All Java dependencies are declared in `code/pom.xml`. The build produces a
self-contained executable JAR.

## Quick start

From the repository root:

```bash
cd code
mvn clean package
java -jar target/seasonal-1.0-SNAPSHOT.jar
```

The default `smoke` mode loads 2,000 points from a committed real-world series,
injects deterministic synthetic errors, runs SRD, SCREEN, LsGreedy, IMR, and
EWMA, prints RMSE and runtime, and writes the SRD repair to:

```text
results/smoke/srd_repaired.csv
```

The JAR can also be launched from the repository root:

```bash
java -jar code/target/seasonal-1.0-SNAPSHOT.jar smoke
```

## Experiment modes

`Experiment` accepts one of four modes:

```bash
java -jar target/seasonal-1.0-SNAPSHOT.jar smoke
java -jar target/seasonal-1.0-SNAPSHOT.jar synthetic
java -jar target/seasonal-1.0-SNAPSHOT.jar real
java -jar target/seasonal-1.0-SNAPSHOT.jar ablation
```

| Mode | Description |
| --- | --- |
| `smoke` | Fast end-to-end validation of SeasonalClean |
| `synthetic` | Error-rate experiments on power and voltage data |
| `real` | Experiments on paired files in `real_clean` and `real_dirty` |
| `ablation` | SeasonalClean iteration-count ablation |

The default synthetic and real experiment loops run the self-contained Java
methods SRD, SCREEN, LsGreedy, IMR, and EWMA. Other baselines remain available
through their classes and wrappers.

## Data

### CSV format

The Java loader expects at least two columns:

```csv
timestamp,value
1604937600000,212.75
1604937900000,151.8
```

Dirty real-world files may contain a third `label` column. The loader uses the
first two columns and applies min-max normalization.

### Synthetic archives

Extract the synthetic datasets before running `synthetic` or `ablation`:

```bash
unzip data/synthetic/power_5241600.csv.zip -d data/synthetic
unzip data/synthetic/voltage_22825440.csv.zip -d data/synthetic
```

The configured periods are 144 for `power` and 1440 for `voltage`.

## Parameters

The main experimental parameters are defined in `Experiment.java`:

| Parameter | Default | Meaning |
| --- | ---: | --- |
| `seed` | 666 | Random seed for error and label sampling |
| `error_rate` | 5.0 | Synthetic error rate |
| `error_range` | 2.0 | Synthetic error magnitude |
| `error_length` | 25 | Maximum consecutive error length |
| `max_iter` | 5 | Maximum SeasonalClean iterations |
| `k` | 6.0 | Synthetic residual threshold multiplier |
| `label_rate` | 0.5 | Labeled fraction used by IMR |

The real-data configuration uses `k = 9.0`.

## Outputs and metrics

All generated files use paths relative to the repository and are written under
`results/`. Depending on the mode, outputs include:

- `expRMSE.txt`: RMSE across synthetic settings;
- `expTime.txt`: runtime in milliseconds;
- `iterRMSE.txt`: RMSE for the iteration ablation;
- `iterTime.txt`: runtime for the iteration ablation;
- `performance.txt`: real-data RMSE records;
- repaired CSV files for smoke and real-data experiments.

`Analysis.java` provides MAE, MAPE, RMSE, and runtime measurements.

## Optional Python baselines

ASAP and HoloClean are optional Python-backed baselines. Java locates their
scripts relative to the repository; no machine-specific paths are required.
Set `PYTHON` when a non-default interpreter should be used:

```bash
export PYTHON=python3
```

Install ASAP dependencies with:

```bash
python3 -m pip install -r \
  code/src/main/java/Algorithm/ASAPPy/requirements.txt
```

Install HoloClean dependencies with:

```bash
python3 -m pip install -r \
  code/src/main/java/Algorithm/HolocleanPy/requirements.txt
```

HoloClean additionally requires a running PostgreSQL instance matching the
database configuration in `HolocleanPy/holoclean.py`.

## Implemented baselines

- SCREEN
- LsGreedy
- IMR
- EWMA
- ASAP
- Sequential
- Holistic
- GARF
- HoloClean

The Holistic linear program is solved with Apache Commons Math. Its objective,
variables, non-negativity conditions, and upper/lower speed constraints match
the original QSopt-backed formulation.

## Reproducibility

For an academic evaluation, report:

- the Git commit hash;
- JDK, Maven, operating system, and hardware;
- dataset and extracted archive checksums;
- seasonal period and all parameters;
- selected comparison methods;
- random seed and number of repeated runs;
- RMSE and runtime, together with raw output files.

Validation performed on June 12, 2026:

- `mvn clean package -DskipTests`: passed;
- executable JAR smoke test from the repository root: passed;
- executable JAR smoke test from `code/`: passed;
- smoke-test RMSE: `0.068`;
- Holistic linear-program example: passed;
- modified Python files passed `py_compile`.

## Citation

If this repository supports your research, cite the associated paper:

```bibtex
@inproceedings{seasonalclean,
  title = {Cleaning Time Series under Seasonal and Trend Constraints},
  note  = {Replace this placeholder with the final author, venue, year,
           page, and DOI metadata from the published paper.}
}
```

The committed supplementary PDF is anonymized and does not contain sufficient
metadata for a complete citation.

## License

No license file is currently included. Add an explicit license before
redistributing or incorporating this code into another project.
