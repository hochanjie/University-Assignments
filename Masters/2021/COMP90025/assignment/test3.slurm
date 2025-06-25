#!/bin/bash
#SBATCH --partition=physical
#SBATCH --constraint=physg5
#SBATCH --time=0:25:00
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=24
#SBATCH --mem=32G 
#SBATCH --job-name=test3
#SBATCH -o test3.out # STDOUT 
#SBATCH -e test3.err # STDERR

module load gcc/8.3.0
g++ -fopenmp -Wall -O3 -o test3 p_guided.cpp
./test3 < mseq-big13-example.dat