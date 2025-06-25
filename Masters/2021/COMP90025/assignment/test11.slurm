#!/bin/bash
#SBATCH --partition=physical
#SBATCH --constraint=physg5
#SBATCH --time=0:10:00
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=24
#SBATCH --mem=32G 
#SBATCH --job-name=test11
#SBATCH -o test11.out # STDOUT 
#SBATCH -e test11.err # STDERR

module load gcc/8.3.0
g++ -fopenmp -Wall -O3 -o test11 p_guided_matrix.cpp
./test11 < mseq-big13-example.dat