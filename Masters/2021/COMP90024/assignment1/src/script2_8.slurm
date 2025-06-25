#!/bin/bash
#SBATCH --partition=snowy
#SBATCH -o slurm28.out # STDOUT 
#SBATCH -e slurm28.err # STDERR
#SBATCH --nodes=2
#SBATCH --ntasks=8
#SBATCH --ntasks-per-node=4
#SBATCH --cpus-per-task=1
#SBATCH --time=05:05:00
module load gcc/8.3.0
module load openmpi/3.1.4
module load python/3.7.4
mpirun -np 8 python3 main.py melbGrid.json AFINN.txt bigTwitter.json
