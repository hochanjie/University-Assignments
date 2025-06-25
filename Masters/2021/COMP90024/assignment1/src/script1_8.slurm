#!/bin/bash
#SBATCH --partition=snowy
#SBATCH -o slurm18.out # STDOUT 
#SBATCH -e slurm18.err # STDERR
#SBATCH --nodes=1
#SBATCH --ntasks=8
#SBATCH --ntasks-per-node=8
#SBATCH --cpus-per-task=1
#SBATCH --time=03:00:00
module load gcc/8.3.0
module load openmpi/3.1.4
module load python/3.7.4
mpirun -np 8 python3 main.py melbGrid.json AFINN.txt bigTwitter.json
