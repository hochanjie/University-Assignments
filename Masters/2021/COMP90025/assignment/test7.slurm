#!/bin/bash
#SBATCH --partition=physical
#SBATCH --constraint=physg5
#SBATCH --time=0:25:00
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=24
#SBATCH --mem=32G 
#SBATCH --job-name=test7
#SBATCH -o test7.out # STDOUT 
#SBATCH -e test7.err # STDERR

module load gcc/8.3.0
g++ -fopenmp -Wall -O3 -o test7 p_guided_only.cpp
./test7 < mseq-big13-example.dat