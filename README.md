# Eco-Hadoop

Eco-Hadoop is a prototype system for energy-aware Hadoop parameter tuning. The
repository contains the model code, processed data, trained final model weights,
six benchmark results, visualization scripts, and a small Flask API.

## Included Benchmarks

This release contains the original six benchmarks used in the evaluation:

- TeraSort
- PiEstimator
- PageRank
- Sort
- Grep
- NNBench

Large raw Hadoop logs are intentionally excluded. The repository includes compact
processed datasets under:

- `byh/active_power/dataset/`
- `byh/cpu_io/dataset/`
- `hadoop_running_data_process/*/preprocessed_data/`

## Model Components

The system uses three model components:

1. `byh/active_power/`: active power prediction model.
2. `byh/cpu_io/`: CPU/IO runtime distribution model.
3. `byh/param_search/`: parameter search model.

Only `model_final.pth` files are tracked. Intermediate checkpoints such as
`model_01000.pth`, TensorBoard event files, Python caches, and local editor or
agent traces are ignored.

Model weights are stored with Git LFS. After cloning, run:

```bash
git lfs install
git lfs pull
```

## Important Directories

- `byh/`: core Python package and trained final model weights.
- `flask_api/`: demo API for model inference and parameter recommendation.
- `param_search_results/`: summarized optimization results for the six benchmarks.
- `draw_experiments_figures/`: plotting scripts and generated figures.
- `overleaf_parameter_attribution/`: Overleaf-ready files for the parameter-attribution experiment.
- `hadoop_running_data_process/`: preprocessing scripts and compact preprocessed data.

## Reproducing Figures

Use the project Python environment and run:

```bash
python run_draw_all.py
```

On Windows/Conda environments that report an OpenMP duplicate runtime warning,
`run_draw_all.py` and `run_draw_figures.bat` set `KMP_DUPLICATE_LIB_OK=TRUE` for
figure generation.

## Privacy and Large-File Policy

The repository deliberately excludes:

- `.claude/`, `.codex/`, `.agents/`, `.idea/`, `.vscode/`
- `__pycache__/`, `*.pyc`
- raw logs in `hadoop_running_data_process/original_data/`
- TensorBoard event files
- intermediate checkpoints

This keeps the public repository focused on reproducible code, compact processed
data, final model artifacts, and evaluation results.
